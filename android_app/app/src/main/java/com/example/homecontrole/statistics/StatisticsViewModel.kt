package com.example.homecontrole.statistics

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.homecontrole.connection.ConnectionRepository
import com.example.homecontrole.connection.ConnectionViewModel
import com.google.gson.JsonObject
import org.eclipse.paho.client.mqttv3.MqttClient
import javax.inject.Inject

class StatisticsViewModel @Inject constructor(
    client: com.example.homecontrole.MqttClient
): ViewModel()
{
    val air: MediatorLiveData<JsonObject> = MediatorLiveData()
    init {
        air.addSource(client.airStats) {
            air.value = it
        }
    }
}

class ConnectionViewModelFactory(private val mqtt: com.example.homecontrole.MqttClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StatisticsViewModel(mqtt) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}