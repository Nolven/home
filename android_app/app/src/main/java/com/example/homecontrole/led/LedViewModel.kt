package com.example.homecontrole.led

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.JsonObject
import javax.inject.Inject

class LedViewModel @Inject constructor(
    client: com.example.homecontrole.MqttClient
): ViewModel()
{
    val hallway: MediatorLiveData<JsonObject> = MediatorLiveData()
    val room: MediatorLiveData<JsonObject> = MediatorLiveData()
    init {
        room.addSource(client.roomLed) {
            room.value = it
        }
        hallway.addSource(client.hallwayLed) {
            hallway.value = it
        }
    }
}

class LedViewModelFactory(private val mqtt: com.example.homecontrole.MqttClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LedViewModel(mqtt) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}