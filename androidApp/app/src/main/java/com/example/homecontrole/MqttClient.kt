package com.example.homecontrole

import android.content.Context
import android.util.Log
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.*

open class MqttClient(private val context: Context) {
    lateinit var client: MqttAndroidClient
    private val logTag = "MqttClient"
    lateinit var successCb: (String, String) -> Unit
    lateinit var disconnectCb: (String) -> Unit

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
                disconnectCb(cause.toString())
            }

            override fun messageArrived(topic: String?, message: MqttMessage?) {
                Log.d(logTag, "Message arrived")
            }

            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                Log.d(logTag, "Message delivered")
            }

            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                Log.d(logTag, "connection completed to $serverURI")
            }
        })
    }

    // for uri like server:port
    fun connect(url: String, failCb: (reason: String) -> Unit)
    {
        val index = url.indexOf(":")
        connect(url.substring(0, index), url.substring(index), failCb)
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
                        successCb(server, port)
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        failCb("Unable to subscribe to topics")
                    }
                })
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                failCb(exception.toString())
            }
        })
    }
}