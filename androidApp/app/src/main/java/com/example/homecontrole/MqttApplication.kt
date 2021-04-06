package com.example.homecontrole

import android.app.Application
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import org.eclipse.paho.android.service.MqttAndroidClient

open class MqttApplication: Application() {
    lateinit var mqtt: MqttClient
}