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
import android.widget.LinearLayout
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
    private var layoutIDs: ArrayList<Int> = ArrayList()
    private var clearIDs: ArrayList<Int> = ArrayList()

    private val data: GradientData = GradientData(0, 0, 0, arrayListOf())

    private fun removeColor()
    {
        view.led_color_gradient_host.removeView(view.findViewById(layoutIDs.last()))

        // Remove last id's
        data.colors.removeAt(data.colors.size - 1)
        layoutIDs.removeAt(layoutIDs.size - 1)
        clearIDs.removeAt(clearIDs.size - 1)

        // Show clear button
        if( clearIDs.isNotEmpty() )
            view.findViewById<Button>(clearIDs.last()).visibility = View.VISIBLE
    }

    private fun addColor()
    {
        val colorIndex = data.colors.size
        data.colors.add(arrayOf(255, 0, 255))

        // Create new layout for 2 buttons
        val ll = LinearLayout(context)
        val layoutId = View.generateViewId()
        layoutIDs.add(layoutId)
        ll.id = layoutId
        ll.orientation = LinearLayout.HORIZONTAL

        // Create color button
        val colorButton = Button(context)
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
        ll.addView(colorButton)

        // Hide previous clear button
        if (clearIDs.isNotEmpty())
            view.findViewById<Button>(clearIDs.last()).visibility = View.INVISIBLE

        // Create clear button
        // ID is for hiding button add new color addition
        val clearButton = Button(context)
        val clearId = View.generateViewId()
        clearIDs.add(clearId)
        clearButton.id = clearId
        clearButton.text = context.resources.getText(R.string.led_gradient_clear)
        clearButton.setOnClickListener{ removeColor() }
        ll.addView(clearButton)

        view.led_color_gradient_host.addView(ll)
    }

    private fun setDataUpdateCb()
    {
        view.gradient_speed.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                data.speed = parseInt(view.gradient_speed.text.toString())
            }
        })

        view.gradient_sampler_step.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
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
        view.gradient_add_color_button.setOnClickListener { addColor() }
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