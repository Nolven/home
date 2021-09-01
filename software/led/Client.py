import json
import sys

import paho.mqtt.client as mqtt
from smbus2 import SMBus


def _set_bytes(config, data, destination: list, offset):
    number = data.to_bytes(config["size"], 'big', signed=(True if data < 0 else False))
    for i in range(config["size"]):
        destination[config["byte"] + i + offset] = number[i]


def _calc_msg_size(byte_conf) -> int:
    return sum([byte_conf[key]["size"] for key in byte_conf if key != "msg_num" and key != "mode_num"])


class Client:
    def __init__(self, config: json):

        self._config = config
        self._bus = SMBus(config["i2c_interface"])

        # Load msg configs on init
        self._conf_bytes(config["byte_config_paths"])

        self._header_size = _calc_msg_size(self._header_conf)
        self._general_size = _calc_msg_size(self._general_conf["general"])

        # Colors
        mode_size = int(self._color_conf["mode"]["size"])
        self._color_static_size = _calc_msg_size(self._color_conf["static_color"]) + mode_size
        self._color_grad_size = _calc_msg_size(self._color_conf["grad_color"]) + mode_size
        self._color_rnd_size = _calc_msg_size(self._color_conf["rnd_color"]) + mode_size

        # States
        mode_size = int(self._state_conf["mode"]["size"])
        self._state_static_size = _calc_msg_size(self._state_conf["static_state"]) + mode_size
        self._state_snake_size = _calc_msg_size(self._state_conf["snake_state"]) + mode_size

        # Mqtt
        self._client = mqtt.Client(transport=config['transport'])

        # Set callbacks
        self._client.on_connect = self._on_connect
        self._client.on_message = self._on_message
        self._client.on_subscribe = self._on_subscribe
        self._client.on_publish = self._on_publish

    def _conf_bytes(self, byte_config_paths: json):
        self._color_conf = json.load(open(byte_config_paths["color_modes"]))
        self._general_conf = json.load(open(byte_config_paths["general"]))
        self._header_conf = json.load(open(byte_config_paths["header"]))
        self._state_conf = json.load(open(byte_config_paths["state_modes"]))
        pass

    def _on_connect(self, client, userdata, flags, rc):
        print("Connected with result code " + str(rc))
        client.subscribe([(t, self._config["default_qos"]) for t in self._config["topic_address_map"].keys()])

    def _on_message(self, client, userdata, msg):
        self._send_i2c(json.loads(msg.payload), self._config["topic_address_map"][msg.topic])

    def _on_subscribe(self, mosq, obj, mid, granted_qos):
        if granted_qos == 128:
            sys.exit("Unable to subscribe to topic")  # bad

        print("Subscribed: " + str(mid))
        print("Granted qos: " + str(granted_qos))

    def _on_publish(self, client, userdata, result):
        print("data published")

    def _send_i2c(self, input_json, address):
        out_data: list = []
        if "general_data" in input_json:
            out_data = self._compose_i2c_mgs(self._general_conf, input_json["general_data"],
                                             self._general_size + self._header_size, "general",
                                             input_json["zone"], {})
            self._bus.write_block_data(address, 0, out_data)

        color_topic = "color_mode"
        if color_topic in input_json:

            if input_json[color_topic] == "static":
                out_data = self._compose_i2c_mgs(self._color_conf, input_json["color_data"],
                                                 self._color_static_size + self._header_size, "static_color",
                                                 input_json["zone"], {})
            elif input_json[color_topic] == "random":
                out_data = self._compose_i2c_mgs(self._color_conf, input_json["color_data"],
                                                 self._color_rnd_size + self._header_size, "rnd_color",
                                                 input_json["zone"], {})
            elif input_json[color_topic] == "gradient":
                out_data = self._compose_i2c_mgs(self._color_conf, input_json["color_data"],
                                                 self._color_grad_size + self._header_size, "grad_color",
                                                 input_json["zone"], {"colors": "color_number"})

            self._bus.write_block_data(address, 0, out_data)

        display_topic = "display_mode"
        if display_topic in input_json:

            if input_json[display_topic] == "static":
                out_data = self._compose_i2c_mgs(self._state_conf, {},
                                                 self._state_static_size + self._header_size, "static_state",
                                                 input_json["zone"], {})
            elif input_json[display_topic] == "snake":
                out_data = self._compose_i2c_mgs(self._state_conf, input_json["display_data"],
                                                 self._state_snake_size + self._header_size, "snake_state",
                                                 input_json["zone"], {})

            self._bus.write_block_data(address, 0, out_data)

    # msg_size = body + header
    # byte_conf - top-level conf (for example toUColorModes.json), cause we need to take mode conf somewhere
    # array_dict - dictionary like (array_field:array_length_field)
    def _compose_i2c_mgs(self, byte_conf, input_json, msg_size, msg_name, zone_num, array_dict: dict):

        for key, value in array_dict.items():
            msg_size += (len(input_json[key]) - 1) * byte_conf[msg_name][key]["size"]

        out_data: list = [None] * msg_size

        # Fill header
        _set_bytes(self._header_conf["zone"], zone_num, out_data, 0)
        _set_bytes(self._header_conf["msg_type"], byte_conf["msg_num"], out_data, 0)

        # Fill body
        # There is no mode_num - it's general msg (hello there)
        if "mode_num" in byte_conf[msg_name]:
            _set_bytes(byte_conf["mode"], byte_conf[msg_name]["mode_num"], out_data, self._header_size)

        for key in input_json:
            if key not in array_dict:  # second condition is for the keys like array size
                _set_bytes(byte_conf[msg_name][key], input_json[key], out_data, self._header_size)

        # Well it basically works only for colors
        for array_field in array_dict:
            _set_bytes(byte_conf[msg_name][array_dict[array_field]], len(input_json[array_field]), out_data,
                       self._header_size)
            for num, el in enumerate(input_json[array_field]):
                color = [el[0], el[1], el[2]]
                _set_bytes(byte_conf[msg_name][array_field], int.from_bytes(color, byteorder='big'), out_data,
                           self._header_size + num * byte_conf[msg_name][array_field]["size"])  # this should be illegal

        print(msg_name + str(out_data))

        return out_data

    def start(self):
        self._client.connect(self._config['ip'], self._config['port'])
        self._client.loop_start()
