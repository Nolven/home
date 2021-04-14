package com.example.homecontrole.led

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.example.homecontrole.R
import com.example.homecontrole.databinding.LedColorGradientBinding
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.gson.Gson
import com.google.gson.JsonElement
import java.lang.Integer.parseInt


class Gradient(private val binding: LedColorGradientBinding, private val context: Context)
{
    private var colorPickerIds: ArrayList<Int> = ArrayList()

    private val data: GradientData = GradientData(0, 0, 0, arrayListOf())

    private fun removeColor()
    {
        binding.ledColorGradientHost.removeView(binding.root.findViewById(colorPickerIds.last()))

        // Remove last id's
        data.colors.removeAt(data.colors.size - 1)
        colorPickerIds.removeAt(colorPickerIds.size - 1)

        // Hide clear button when there is no colors
        if( colorPickerIds.isEmpty() )
            binding.gradientRemoveColor.visibility = View.INVISIBLE
    }

    private fun addColor()
    {
        val colorIndex = data.colors.size
        data.colors.add(arrayOf(255, 0, 255))

        // Create color button
        val colorButton = Button(context)
        val buttonId = View.generateViewId()
        colorButton.id = buttonId
        colorPickerIds.add(buttonId)
        colorButton.setBackgroundColor(0xFFFF00FF.toInt())
        colorButton.setOnClickListener{
            val initialColor = Color.rgb(
                data.colors[colorIndex][0],
                data.colors[colorIndex][1],
                data.colors[colorIndex][2]
            )
            ColorPickerDialogBuilder
                .with(context)
                .setTitle("Choose color")
                .initialColor(initialColor)
                .lightnessSliderOnly()
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                // On select, not on button click for in-flight color update
                .setOnColorSelectedListener {
                    data.colors[colorIndex] = arrayOf(it.red, it.green, it.blue)
                    colorButton.setBackgroundColor(it)
                }
                .setPositiveButton("Choose") { _, _, _ -> }
                .setNegativeButton("Cancel") { _, _ ->
                    data.colors[colorIndex][0] = initialColor.red
                    data.colors[colorIndex][1] = initialColor.green
                    data.colors[colorIndex][2] = initialColor.blue
                    colorButton.setBackgroundColor(initialColor)
                } // TODO return color
                .build()
                .show()
        }

        binding.ledColorGradientHost.addView(colorButton)

        // Show clear button
        if (binding.gradientRemoveColor.visibility == View.INVISIBLE)
            binding.gradientRemoveColor.visibility = View.VISIBLE
    }

    private fun setDataUpdateCb()
    {
        binding.gradientSpeed.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if( s.toString().isNotEmpty() )
                    data.speed = parseInt(binding.gradientSpeed.text.toString())
            }
        })

        binding.gradientSamplerStep.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if( s.toString().isNotEmpty() )
                    data.sampler_step = parseInt(binding.gradientSamplerStep.text.toString())
            }
        })

        binding.gradientBlending.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parentView: AdapterView<*>?,
                    selectedItemView: View?,
                    position: Int,
                    id: Long
                ) {
                    data.blending = position
                }

                override fun onNothingSelected(parentView: AdapterView<*>?) {
                    data.blending = 1
                }
            }

    }

    fun getJson(): JsonElement {
        Log.d("Gradient", Gson().toJson(data))
        return Gson().toJsonTree(data)
    }

    init {
        binding.gradientAddColor.setOnClickListener { addColor() }
        binding.gradientRemoveColor.setOnClickListener{ removeColor() }
        setDataUpdateCb()

       ArrayAdapter.createFromResource(
           context, R.array.blending_modes,
           android.R.layout.simple_spinner_item
       ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.gradientBlending.adapter = adapter
       }
    }
}