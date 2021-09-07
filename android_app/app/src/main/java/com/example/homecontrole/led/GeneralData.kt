package com.example.homecontrole.led

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.example.homecontrole.databinding.LedGeneralBinding
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlin.math.floor

data class GeneralData(var start: Int, var end: Int, var brightness: Int)

class General(private val binding: LedGeneralBinding, private val context: Context)
{
    private val logTag = "LED_general"
    private val data = GeneralData(0, 0, 255)

    fun getJson(): JsonElement {
        Log.d("LED_general", Gson().toJson(data))
        return Gson().toJsonTree(data)
    }

    fun update(json: JsonObject)
    {
        Log.d(logTag,"General update")
        binding.brightnessSlider.value = json["brightness"].asFloat
        binding.zoneStartTextEdit.setText(json["start"].asString)
        binding.zoneEndTextEdit.setText(json["end"].asString)
    }


    init {

        binding.zoneStartTextEdit.addTextChangedListener( object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if( s.toString().isNotEmpty())
                    data.start = Integer.parseInt(s.toString())
            }
        })

        binding.zoneEndTextEdit.addTextChangedListener( object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if( s.toString().isNotEmpty())
                    data.end = Integer.parseInt(s.toString())
            }
        })

        binding.brightnessSlider.addOnChangeListener{ _, value, _ ->
            data.brightness = value.toInt()
        }
        binding.brightnessSlider.value = floor((binding.brightnessSlider.valueTo - binding.brightnessSlider.valueFrom)/2)

    }
}