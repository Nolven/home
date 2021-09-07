package com.example.homecontrole.led

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import com.example.homecontrole.R
import com.example.homecontrole.databinding.LedModeSnakeBinding
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject

data class SnakeData(var direction: Int, var length: Int, var loop: Boolean, var delay: Int)

class SnakeMode(private val binding: LedModeSnakeBinding, private val context: Context)
{
    private val data = SnakeData(0, 0, true, 0)

    fun update(json: JsonObject){
        binding.delay.setText(json["delay"].asString)
        binding.length.setText(json["length"].asString)

        binding.checkboxMeat.isChecked = json["loop"].asBoolean

        when(json["direction"].asInt) {
            -1 -> binding.directionGroup.check(R.id.snake_there)
            1 -> binding.directionGroup.check(R.id.snake_here)
        }
    }

    fun getJson(): JsonElement {
        Log.d("Gradient", Gson().toJson(data))
        return Gson().toJsonTree(data)
    }

    init {
        binding.length.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if( s.toString().isNotEmpty() )
                    data.length = Integer.parseInt(s.toString())
            }
        })

        binding.delay.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if( s.toString().isNotEmpty() )
                    data.delay = Integer.parseInt(s.toString())
            }
        })

        binding.snakeHere.setOnClickListener { data.direction = 1 }
        binding.snakeThere.setOnClickListener { data.direction = -1 }
        binding.checkboxMeat.setOnClickListener { data.loop = binding.checkboxMeat.isChecked }
    }

}