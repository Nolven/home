import json
import os

import paho.mqtt.client as mqtt

# TODO configs
# TODO OP

max_zones = 11

stored_led = {
    "hallway": [{} for i in range(max_zones)],
    "room": [{} for j in range(max_zones)]
}

storage_path = "storage/"
led_storage_name = "led"


def load_storage(path):
    with open(path + led_storage_name + '.json') as led_json:
        global stored_led
        stored_led = json.load(led_json)


def update_led(room, received: json):
    destination = stored_led[room][int(received["zone"])]

    if "color_mode" in received:
        destination["color_mode"] = received["color_mode"]
        destination["color_data"] = received["color_data"]

    if "display_mode" in received:
        destination["display_mode"] = received["display_mode"]
        destination["display_data"] = received["display_data"]

    # General
    # Ignore if both start and end == 0
    if "general_data" in received:
        if received["general_data"]["start"] == 0 and received["general_data"]["end"] == 0:
            if "general_date" not in destination:
                destination["general_data"] = {"start": 0, "end": 0}
            destination["general_data"]["brightness"] = received["general_data"]["brightness"]
        else:
            destination["general_data"] = received["general_data"]


def on_connect(client, userdata, flags, rc):
    print("Connected with result code: " + str(rc))
    client.subscribe("#")


def on_message(_, userdata, msg):
    if "led" in msg.topic:
        if "get" in msg.topic:  # Request
            prefix = msg.topic[:msg.topic.find('/')]
            client.publish(prefix + "/led",
                           json.dumps({"zone": int(msg.payload), **stored_led[prefix][int(msg.payload)]}))
        else:  # Storage
            update_led(msg.topic[:msg.topic.find('/')], json.loads(msg.payload))

    with open(storage_path + led_storage_name + ".json", 'w') as file:
        json.dump(stored_led, file, indent=4)

    print(msg.topic + " " + str(msg.payload))


def ensure_dir(directory):
    if not os.path.exists(directory):
        os.makedirs(directory)


if __name__ == "__main__":
    ensure_dir(storage_path)

    print(stored_led)
    load_storage(storage_path)
    print(stored_led)

    client = mqtt.Client()
    client.on_connect = on_connect
    client.on_message = on_message
    client.connect("192.168.0.15", 1883)

    client.loop_forever()
