package com.example.homecontrole

import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.android.synthetic.main.led_color_gradient.view.*

class Gradient(private val view: View, val context: Context)
{
    private var colors: ArrayList<Int> = ArrayList()
    private var layoutIDs: ArrayList<Int> = ArrayList()
    private var clearIDs: ArrayList<Int> = ArrayList()

    private fun removeColor()
    {
        view.led_color_gradient_host.removeView(view.findViewById(layoutIDs.last()))

        // Remove last id's
        colors.removeAt(colors.size - 1)
        layoutIDs.removeAt(layoutIDs.size - 1)
        clearIDs.removeAt(clearIDs.size - 1)

        // Show clear button
        if( clearIDs.isNotEmpty() )
            view.findViewById<Button>(clearIDs.last()).visibility = View.VISIBLE
    }

    private fun addColor()
    {
        val colorIndex = colors.size
        colors.add(0xffffffff.toInt())

        // Create new layout for 2 buttons
        val ll = LinearLayout(context)
        val layoutId = View.generateViewId()
        layoutIDs.add(layoutId)
        ll.id = layoutId
        ll.orientation = LinearLayout.HORIZONTAL

        // Create color button
        val colorButton = Button(context)
        colorButton.setOnClickListener{
            ColorPickerDialogBuilder
                .with(context)
                .setTitle("Choose color")
                .initialColor(colors[colorIndex])
                .lightnessSliderOnly()
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener { selectedColor ->
                    colors[colorIndex] = selectedColor
                }
                .setPositiveButton("Choose") { _, selectedColor, _ ->
                    colorButton.setBackgroundColor(selectedColor)
                }
                .setNegativeButton("Cancel") { _, _ -> }
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

    public fun getJson()
    {

    }

    init {
        view.gradient_add_color_button.setOnClickListener { addColor() }

       ArrayAdapter.createFromResource(
            context,
            R.array.blending_modes,
            android.R.layout.simple_spinner_item
       ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            view.gradient_blending.adapter = adapter }
    }
}