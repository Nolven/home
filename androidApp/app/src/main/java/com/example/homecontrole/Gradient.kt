package com.example.homecontrole

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
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.gson.Gson
import com.google.gson.JsonElement
import kotlinx.android.synthetic.main.led_color_gradient.view.*
import java.lang.Integer.parseInt


class Gradient(private val view: View, private val context: Context)
{
    private var colorPickerIds: ArrayList<Int> = ArrayList()

    private val data: GradientData = GradientData(0, 0, 0, arrayListOf())

    private fun removeColor()
    {
        view.led_color_gradient_host.removeView(view.findViewById(colorPickerIds.last()))

        // Remove last id's
        data.colors.removeAt(data.colors.size - 1)
        colorPickerIds.removeAt(colorPickerIds.size - 1)

        // Hide clear button when there is no colors
        if( colorPickerIds.isEmpty() )
            view.gradient_remove_color.visibility = View.INVISIBLE
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

        view.led_color_gradient_host.addView(colorButton)

        // Show clear button
        if (view.gradient_remove_color.visibility == View.INVISIBLE)
            view.gradient_remove_color.visibility = View.VISIBLE
    }

    private fun setDataUpdateCb()
    {
        view.gradient_speed.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if( s.toString().isNotEmpty() )
                    data.speed = parseInt(view.gradient_speed.text.toString())
            }
        })

        view.gradient_sampler_step.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if( s.toString().isNotEmpty() )
                    data.sampler_step = parseInt(view.gradient_sampler_step.text.toString())
            }
        })

        view.gradient_blending.onItemSelectedListener =
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
        view.gradient_add_color.setOnClickListener { addColor() }
        view.gradient_remove_color.setOnClickListener{ removeColor() }
        setDataUpdateCb()

       ArrayAdapter.createFromResource(
           context, R.array.blending_modes,
           android.R.layout.simple_spinner_item
       ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            view.gradient_blending.adapter = adapter
       }
    }
}