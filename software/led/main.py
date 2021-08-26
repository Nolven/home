import argparse
import json

from Client import client

config: json = {
    "mqtt":
        {
            "ip": "192.168.0.15",
            "port": 1883,
            "name": "",
            "transport": "tcp",
            "password": "",
            "in_topic": "room/led",
            "out_topic": "cba",
            "byte_config_paths":
            {
                "color_modes": "byte_configs/toUColorModes.json",
                "general": "byte_configs/toUGeneral.json",
                "header": "byte_configs/toUHeader.json",
                "state_modes": "byte_configs/toUStateModes.json"
            }
        },
    "i2c":
        {
            "device": 0,
            "address": 0x69
        }
}


def print_conf():
    print(json.dumps(config, indent=4))
    pass


def parse_args():
    parser = argparse.ArgumentParser(description='Gateway between MQTT broker and serial port for LED controller')

    parser.add_argument('-d', '--debug', dest='isDebug', action='store_true', default=False,
                        help='Enable debug logging')  # TODO implement
    parser.add_argument('-c', '--config', dest='config', help='JSON configuration file for gateway')
    parser.add_argument('-g', '--gen-config', const=print_conf, action='store_const', dest='gen',
                        help='Generate default config')

    args = parser.parse_args()
    # global isDebug
    # isDebug = args.isDebug

    if args.gen:
        print_conf()
        exit(0)  # not good

    if args.config:
        global config
        with open(args.config) as json_file:
            config = json.load(json_file)
        pass


if __name__ == '__main__':
    parse_args()

    mqtt = client(config)
    mqtt.start()

    while True:
        pass
