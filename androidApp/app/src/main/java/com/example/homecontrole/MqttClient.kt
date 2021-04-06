package com.example.homecontrole
import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

open class MqttClient(context: Context, server: String, port: String) {

    fun publish(topic: String,
                msg: String,
                qos: Int = 1,
                retained: Boolean = false) {
        try {
            val message = MqttMessage()
            message.payload = msg.toByteArray()
            message.qos = qos
            message.isRetained = retained
            client.publish(topic, message, null, null)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    open fun connect(successCb: () -> Unit, failCb: (reason: String) -> Unit)
    {
        val options = MqttConnectOptions()
        options.isCleanSession = true
        client.connect(options, null, object : IMqttActionListener
        {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                client.subscribe(R.string.room_led_input_topic.toString(), 1, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        successCb()
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        failCb("Unable to subscribe to topics")
                    }
                })
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.w(TAG, exception.toString())
                failCb("Unable to connect")
            }
        })
    }

    var client: MqttAndroidClient;
    private val TAG = "MqttClient"

    init {
        val uri = "tcp://$server:$port"
        Log.d(TAG, "Client inited with uri $uri")
        client = MqttAndroidClient(context, uri, "a")
        client.setCallback(object : MqttCallbackExtended {
            override fun connectionLost(cause: Throwable?) {
                TODO("Not yet implemented")
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                TODO("Not yet implemented")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                TODO("Not yet implemented")
            }

            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                Log.d(TAG, "connection completed to $serverURI")
            }
        })
    }
}