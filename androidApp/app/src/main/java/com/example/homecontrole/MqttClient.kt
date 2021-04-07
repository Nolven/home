package com.example.homecontrole

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

open class MqttClient(private val context: Context) {
    lateinit var client: MqttAndroidClient
    private val logTag = "MqttClient"
    lateinit var successCb: () -> Unit
    lateinit var disconnectCb: () -> Unit

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

    fun setCallbacks()
    {
        client.setCallback(object : MqttCallbackExtended {
            override fun connectionLost(cause: Throwable?) {
                disconnectCb()
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                TODO("Not yet implemented")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                TODO("Not yet implemented")
            }

            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                Log.d(logTag, "connection completed to $serverURI")
            }
        })
    }

    fun connect(server: String, port: String, failCb: (reason: String) -> Unit)
    {
        //Re-init client
        val uri = "tcp://$server:$port"
        client = MqttAndroidClient(context, uri, "")
        setCallbacks()

        client.connect(MqttConnectOptions(), null,
            object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                // Subscribe on success
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
                Log.w(logTag, exception.toString())
                failCb("Unable to connect")
            }
        })
    }
}