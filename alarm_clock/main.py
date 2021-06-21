import json
import sys
import datetime
import threading
import time

import numpy as np

from collections import OrderedDict

import paho.mqtt.client as mqtt
from scipy.interpolate import interp1d

'''
{
    day: "",
    month: ""
    year: "",
    hour: "",
    minute: "",
    duration_minutes: "",
    curvature: "", percentage / time
    color: ""
}
'''

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


def send_to_led(brightness: int):
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
    for key, item in alarm_params["curvature"].iteritems():
        in_time_points.append(int(key))
        in_brightnesses.append(int(item))

    # Probably could do it on alarm receive
    interpolation = interp1d(in_time_points, in_brightnesses)
    time_points = np.linspace(0, alarm_params["duration_minutes"],
                              alarm_params["duration_minutes"] * 60 / config["brightness_time_step_sec"])
    brightnesses = interpolation(time_points)

    set_up_led(alarm_params["color"])
    for point, in_brightnesses in zip(time_points, brightnesses):
        send_to_led(in_brightnesses)
        time.sleep(point)


def time_check():
    while True:
        if list(times.items())[0] >= datetime.datetime.now():
            fire(list(times.items())[0])
            times.pop(list(times.items())[0])


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
            day=input_json["day"],
            month=input_json["month"],
            year=input_json["year"],
            minute=input_json["minute"],
            hour=input_json["hour"]
        ) - datetime.timedelta(minutes=input_json["duration_minutes"])] = {input_json["color"], input_json["curvature"],
                                                                           input_json["duration"]}

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
    mqtt = MqttClient(config["mqtt"])
    mqtt.start()

    threading.Thread(target=time_check).join()
