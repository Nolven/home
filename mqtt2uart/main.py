import json
import argparse

from mqttClient import MqttClient
from uartClient import UartClient

config: json = {
  "mqtt":
  {
    "ip": "192.168.0.15",
    "port": 1883,
    "name": "",
    "transport": "tcp",
    "password": "",
    "in_topic": "abc",
    "out_topic": "cba",
    "byte_config_paths":
    {
      "color_modes": "uartProtocolConfigs/toUColorModes.json",
      "general": "uartProtocolConfigs/toUGeneral.json",
      "header": "uartProtocolConfigs/toUHeader.json",
      "state_modes": "uartProtocolConfigs/toUStateModes.json"
    }
  },
  "uart":
  {
    "device": "/dev/ttyS1",
    "baud_rate": 2400,
    "timeout": 0,
    "msg_size": 6,
    "zones_per_msg": 10,
    "byte_configs":
    {
      "led_state": "uartProtocolConfigs/fromULedState.json"
    }
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

    mqtt = MqttClient(config['mqtt'])
    uart = UartClient(config['uart'])

    mqtt.uart = uart
    uart.mqtt = mqtt

    mqtt.start()
    uart.start()

    while True:
        pass