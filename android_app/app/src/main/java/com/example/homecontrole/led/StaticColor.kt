package com.example.homecontrole.led

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.widget.Button
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.example.homecontrole.databinding.LedColorStaticBinding
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject

data class StaticColorData(var R: Int, var G: Int, var B: Int)

class StaticColor(private val binding: LedColorStaticBinding, private val context: Context)
{
    private val logTag = "LED_staticColor"
    private val data = StaticColorData(255, 0, 255)

    fun update(json: JsonObject){
        Log.d(logTag, "update")
        data.R = json["R"].asInt
        data.G = json["G"].asInt
        data.B = json["B"].asInt

        binding.colorButton.setBackgroundColor(Color.rgb(data.R, data.G, data.B))
    }

    fun getJson(): JsonElement{
        Log.d("Static color", Gson().toJson(data))
        return Gson().toJsonTree(data)
    }

    init{
        val colorButton: Button =  binding.colorButton
        colorButton.setBackgroundColor(Color.rgb(data.R, data.G, data.B))
        colorButton.setOnClickListener {
            val initialColor = Color.rgb(data.R, data.G, data.B)
            ColorPickerDialogBuilder
                .with(context)
                .setTitle("Choose color")
                .initialColor(initialColor)
                .lightnessSliderOnly()
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener { selectedColor ->
                    data.R = selectedColor.red
                    data.G = selectedColor.green
                    data.B = selectedColor.blue
                    colorButton.setBackgroundColor(selectedColor)
                }
                .setPositiveButton("Choose") { _, _, _ ->}
                .setNegativeButton("Cancel") { _, _ ->
                    data.R = initialColor.red
                    data.G = initialColor.green
                    data.B = initialColor.blue
                    colorButton.setBackgroundColor(initialColor)
                }
                .build()
                .show()
        }
    }
}