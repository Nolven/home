import json
import os
import paho.mqtt.client as mqtt

# TODO configs
# TODO OP
from typing import List

max_zones = 10

general_data_template = {
    "general_data":
    {
        "start": 0,
        "end": 0,
        "brightness": 255
    }
}

hallway = [general_data_template] * 10
room = [general_data_template] * 10

storage_path = "storage/"


def update_data(current, received: json):
    zone = int(received["zone"])
    if "color_mode" in received:
        current[zone]["color_mode"] = received["color_mode"]
        current[zone]["color_data"] = received["color_data"]

    if "display_mode" in received:
        current[zone]["display_mode"] = received["display_mode"]
        current[zone]["display_data"] = received["display_data"]

    # General
    # Ignore if both start and end == 0
    if received["general_data"]["start"] == 0 and received["general_data"]["end"] == 0:
        current[zone]["general_data"]["brightness"] = received["general_data"]["brightness"]
    else:
        current[zone] = received["general_data"]


def on_connect(client, userdata, flags, rc):
    print("Connected with result code " + str(rc))
    client.subscribe("#")


def on_message(client, userdata, msg):
    # store
    if msg.topic == "room/led":
        update_data(room, json.loads(msg.payload))
    elif msg.topic == "hallway/led":
        update_data(hallway, json.loads(msg.payload))

    # requests
    elif msg.topic == "hallway/led/get":
        client.publish("hallway/led", json.dumps({"zone": int(msg.payload), **hallway[int(msg.payload)]}))
    elif msg.topic == "room/led/get":
        client.publish("room/led", json.dumps({"zone": int(msg.payload), **room[int(msg.payload)]}))

    for i, el in enumerate(room):
        with open(storage_path + "room" + str(i) + ".json", 'w') as file:
            json.dump(el, file)

    for i, el in enumerate(hallway):
        with open(storage_path + "hallway" + str(i) + ".json", 'w') as file:
            json.dump(el, file)

    print(msg.topic + " " + str(msg.payload))


def ensure_dir(directory):
    if not os.path.exists(directory):
        os.makedirs(directory)


ensure_dir(storage_path)
client = mqtt.Client()
client.on_connect = on_connect
client.on_message = on_message
client.connect("127.0.0.1", 1883)

client.loop_forever()
