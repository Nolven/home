import json
import math
import sys
import datetime
import threading
import time

import numpy as np

from collections import OrderedDict

import paho.mqtt.client as mqtt
from scipy.interpolate import interp1d

json_example = {
  "hour": 10,
  "minute": 8,
  "duration_minutes": 30,
  "curvature": {
      "0": 0,
      "5": 15,
      "15": 40,
      "39": 90
  },
  "repeat":[1,2,3,4,5,6,7]
  "color": [255, 255, 255]
}

config: json = {
    "ip": "192.168.0.15",
    "port": 1883,
    "name": "",
    "transport": "tcp",
    "password": "",
    "in_topic": "abc",
    "out_topic": "cba",
    "brightness_time_step_sec": 1
}

times: OrderedDict = OrderedDict()
mutex: threading.Lock


# Set's up zone
# Color as [R,G,B]
def set_up_led(color: []):
    mqtt.send(json.dumps({
        "zone": 0,
        "general_data":
            {
                "start": 0,
                "end": 255,
                "brightness": 0
            },
        "color_mode": "static",
        "color_data":
            {
                "R": color[0],
                "G": color[1],
                "B": color[2]
            }
    }))


def update_brightness(brightness: int):
    mqtt.send(json.dumps({
        "zone": 0,
        "general_data":
            {
                "start": 0,
                "end": 0,
                "brightness": brightness
            }
    }))


def fire(alarm_params: json):
    in_time_points = []
    in_brightnesses = []
    for key, item in alarm_params["curvature"].items():
        in_time_points.append(int(key) * 60)
        in_brightnesses.append(int(item))

    # Probably could do it on alarm receive
    interpolation = interp1d(in_time_points, in_brightnesses, fill_value="extrapolate")
    time_points = np.linspace(start=0, stop=int(alarm_params["duration_minutes"]) * 60,
                              num=math.ceil(int(alarm_params["duration_minutes"]) * 60 / int(config["brightness_time_step_sec"])))
    brightnesses = interpolation(time_points)

    set_up_led(alarm_params["color"])
    for point, brightness in zip(time_points, brightnesses):
        update_brightness(math.ceil(brightness))
        time.sleep(point)


def time_check():
    while True:
        if bool(times):
            if list(times.items())[0][0] <= datetime.datetime.now():
                fire(list(times.items())[0][1])
                times.pop(list(times.items())[0][0])


class MqttClient:
    def __init__(self, le_config: json):
        self._client = mqtt.Client()
        self._config = le_config

        # Set callbacks
        self._client.on_connect = self._on_connect
        self._client.on_message = self._on_message
        self._client.on_subscribe = self._on_subscribe
        self._client.on_publish = self._on_publish

    def _on_connect(self, client, userdata, flags, rc):
        print("Connected with result code " + str(rc))
        client.subscribe(self._config['in_topic'])

    def _on_message(self, client, userdata, msg):
        input_json = json.loads(msg.payload)
        times[datetime.datetime(
            day=int(input_json["day"]),
            month=int(input_json["month"]),
            year=int(input_json["year"]),
            minute=int(input_json["minute"]),
            hour=int(input_json["hour"])
        ) - datetime.timedelta(minutes=int(input_json["duration_minutes"]))] = {"color": input_json["color"],
                                                                                "curvature": input_json["curvature"],
                                                                                "duration_minutes": input_json[
                                                                                    "duration_minutes"]
                                                                                }

    def _on_subscribe(self, mosq, obj, mid, granted_qos):
        if granted_qos == 128:
            sys.exit("Unable to subscribe to topic")  # bad

        print("Subscribed: " + str(mid))
        print("Granted qos: " + str(granted_qos))

    def _on_publish(self, client, userdata, result):
        print("data published")

    def start(self):
        self._client.connect(self._config['ip'], self._config['port'])
        self._client.loop_start()
        pass

    def send(self, data: str):
        self._client.publish(self._config['out_topic'], data)


if __name__ == '__main__':
    mqtt: MqttClient = MqttClient(config)
    mqtt.start()

    checker = threading.Thread(target=time_check)
    checker.start()
    checker.join()
