import json
import sys

import paho.mqtt.client as mqtt

from globdebug import debug
from interfaces.ImqttClient import ImqttClinet


def _set_bytes(config, data, destination: list, offset):
    number = data.to_bytes(config["size"], 'big', signed=(True if data < 0 else False))
    for i in range(config["size"]):
        destination[config["byte"] + i + offset] = number[i]


def _calc_msg_size(byte_conf) -> int:
    return sum([byte_conf[key]["size"] for key in byte_conf if key != "msg_num" and key != "mode_num"])


class MqttClient(ImqttClinet):
    def __init__(self, config: json):

        self._config = config

        # Load msg configs on init
        self._color_conf = json.load(open(config["byte_config_paths"]["color_modes"]))
        self._general_conf = json.load(open(config["byte_config_paths"]["general"]))
        self._header_conf = json.load(open(config["byte_config_paths"]["header"]))
        self._state_conf = json.load(open(config["byte_config_paths"]["state_modes"]))

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

        self._uart = None
        self._client = mqtt.Client(transport=config['transport'])

        # Set callbacks
        self._client.on_connect = self._on_connect
        self._client.on_message = self._on_message
        self._client.on_subscribe = self._on_subscribe
        self._client.on_publish = self._on_publish

    def _on_connect(self, client, userdata, flags, rc):
        print("Connected with result code " + str(rc))
        client.subscribe(self._config['in_topic'])

    def _send_hmsg_size(self, msg_size):
        self._uart.send(bytearray([self._header_size + msg_size]))

    def _on_message(self, client, userdata, msg):
        input_json = json.loads(msg.payload)

        if "general" in input_json:
            self._sendJson2Uart(self._general_conf, input_json["general"],
                                self._general_size + self._header_size, "general",
                                input_json["zone"], {})

        # Color segment
        if "static_color" in input_json:
            self._sendJson2Uart(self._color_conf, input_json["static_color"], self._color_static_size + self._header_size, "static_color",
                                input_json["zone"], {})

        if "rnd_color" in input_json:
            self._sendJson2Uart(self._color_conf, input_json["rnd_color"],
                                self._color_rnd_size + self._header_size, "rnd_color",
                                input_json["zone"], {})

        if "grad_color" in input_json:  # Currently unusable
            self._sendJson2Uart(self._color_conf, input_json["grad_color"],
                                self._color_grad_size + self._header_size, "grad_color",
                                input_json["zone"], {"colors": "color_number"})

        # State section
        if "static_state" in input_json:
            self._sendJson2Uart(self._state_conf, input_json["static_state"],
                                self._state_static_size + self._header_size, "static_state",
                                input_json["zone"], {})

        if "snake_state" in input_json:  # Currently unusable
            self._sendJson2Uart(self._state_conf, input_json["snake_state"],
                                self._state_snake_size + self._header_size, "snake_state",
                                input_json["zone"], {})

    def _on_subscribe(self, mosq, obj, mid, granted_qos):
        if granted_qos == 128:
            sys.exit("Unable to subscribe to topic")  # bad

        print("Subscribed: " + str(mid))
        print("Granted qos: " + str(granted_qos))

    def _on_publish(self, client, userdata, result):
        debug("data published")

    # msg_size = body + header
    # byte_conf - top-level conf (for example toUColorModes.json), cause we need to take mode conf somewhere
    # array_dict - dictionary like (array_field:array_length_field)
    def _sendJson2Uart(self, byte_conf, input_json, msg_size, msg_name, zone_num, array_dict: dict):
        add_size = 0

        for key, value in array_dict.items():
            add_size += (len(input_json[key]) - 1) * byte_conf[msg_name][key]["size"]

        self._uart.send((msg_size + add_size).to_bytes(1, 'big'))

        data: list = [None] * (msg_size + add_size)

        # Fill header
        _set_bytes(self._header_conf["zone"], zone_num, data, 0)
        _set_bytes(self._header_conf["msg_type"], byte_conf["msg_num"], data, 0)

        # Fill body
        # There is no mode_num - it's general msg (hello there)
        if "mode_num" in byte_conf[msg_name]:
            _set_bytes(byte_conf["mode"], byte_conf[msg_name]["mode_num"], data, self._header_size)

        for key in input_json:
            if key not in array_dict:  # second condition is for the keys like array size
                _set_bytes(byte_conf[msg_name][key], input_json[key], data, self._header_size)

        # Well it basically works only for colors
        for array_field in array_dict:
            _set_bytes(byte_conf[msg_name][array_dict[array_field]], len(input_json[array_field]), data, self._header_size)
            for num, el in enumerate(input_json[array_field]):
                color = [el[0], el[1], el[2]]
                _set_bytes(byte_conf[msg_name][array_field], int.from_bytes(color, byteorder='big'), data, self._header_size + num * byte_conf[msg_name][array_field]["size"]) # this should be illegal

        print(msg_name + " was sent to uart")
        self._uart.send(bytearray(data))

    def start(self):
        self._client.connect(self._config['ip'], self._config['port'])
        self._client.loop_start()
        pass

    def send(self, data: str):
        self._client.publish(self._config['out_topic'], data)

    @property
    def uart(self):
        return self._uart

    @uart.setter
    def uart(self, value):
        self._uart = value

    @uart.deleter
    def uart(self):
        del self._uart
