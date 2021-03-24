import json

import serial

from interfaces.IuartClient import IuartClient


def _getBytes(config, data, offset) -> str:
    out = 0
    for i in range(config["size"]):
        out |= data[offset + config["byte"]] << 8 * i
    return str(out)


class UartClient(IuartClient):
    def __init__(self, config):
        self._config: json = config
        self._byte_config: json = json.load(open(config["byte_configs"]["led_state"]))
        self._mqtt = None
        self._port = serial.Serial(port=config["device"], baudrate=config["baud_rate"], timeout=config["timeout"])

    def start(self):
        if not self._port.isOpen():
            self._port.open()
        while self._port.isOpen():
            byte_data = self._port.read(self._config["msg_size"])
            if len(byte_data) > 0:
                print(byte_data)
        pass

    def send(self, data: bytearray):
        self._port.write(data)
        print(data)

    def _u2jsonString(self, data: bytearray) -> str:
        # "zones":{
        #     "0": {"left": "1", "right": "2", "brightness": "3"},
        #     "1": {"left": "1", "right": "2", "brightness": "3"}}
        json_data = {}
        msg_size = self._config["msg_size"]
        for i in range(self._config["zones_per_msg"]):  # foreach zone in msg
            json_data["zones"][_getBytes(self._byte_config["zone_number"], data, i * msg_size)] = \
                {
                   "left": _getBytes(self._byte_config["left_bound"], data, i * msg_size),
                   "right": _getBytes(self._byte_config["right_bound"], data, i * msg_size),
                   "brightness": _getBytes(self._byte_config["brightness"], data, i * msg_size),
                }

        return json.dumps(json_data)

    @property
    def mqtt(self):
        return self._mqtt

    @mqtt.setter
    def mqtt(self, value):
        self._mqtt = value

    @mqtt.deleter
    def mqtt(self):
        del self._mqtt
