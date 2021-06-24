import json
import struct
import time

from smbus2 import SMBus
import paho.mqtt.client as mqtt

config = {
    "i2c":
        {
            "delay_s": 5,
            "address": 0x22,
            "block_size": 20,
            "i2c_bus": 0,
        },

    "mqtt":
        {
            "ip": "192.168.0.15",
            "port": 1883,
            "name": "",
            "transport": "tcp",
            "password": "",
            "out_topic": "air",
        }
}


def _on_connect(self, client, userdata, flags, rc):
    print("Connected with result code " + str(rc))


def _on_publish(self, client, userdata, result):
    print("data published")


if __name__ == '__main__':
    client = mqtt.Client(transport=config["mqtt"]["transport"])
    client.connect(config["mqtt"]['ip'], config["mqtt"]['port'])

    bus = SMBus(0)
    while True:
        block = bus.read_i2c_block_data(config["i2c"]["address"], 0, config["i2c"]["block_size"])

        j = {
            "temperature": struct.unpack('f', bytes(block[0:4]))[0],  # TODO move to config
            "humidity": struct.unpack('f', bytes(block[4:8]))[0],
            "index": struct.unpack('f', bytes(block[8:12]))[0],
            "co2": struct.unpack('f', bytes(block[12:16]))[0],
        }
        client.publish(config["mqtt"]["out_topic"], payload=json.dumps(j))

        time.sleep(float(config["i2c"]["delay_s"]))
