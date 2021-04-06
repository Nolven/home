package com.example.homecontrole

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.android.synthetic.main.fragment_led_color_gradient.*
import kotlinx.android.synthetic.main.fragment_led_color_gradient.view.*
import kotlinx.android.synthetic.main.fragment_led_color_static.view.*
import kotlinx.android.synthetic.main.fragment_led_mode.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [led_color_gradient.newInstance] factory method to
 * create an instance of this fragment.
 */
class led_color_gradient : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    // The same size
    private var colors: ArrayList<Int> = ArrayList()
    private var layoutIDs: ArrayList<Int> = ArrayList()
    private var clearIDs: ArrayList<Int> = ArrayList()

    private fun removeColor()
    {
        led_color_gradient_host.removeView(requireView().findViewById(layoutIDs.last()))

        // Remove last id's
        colors.removeAt(colors.size - 1)
        layoutIDs.removeAt(layoutIDs.size - 1)
        clearIDs.removeAt(clearIDs.size - 1)

        // Show clear button
        if( clearIDs.isNotEmpty() )
            requireView().findViewById<Button>(clearIDs.last()).visibility = View.VISIBLE
    }

    private fun addColor()
    {
        val colorIndex = colors.size
        colors.add(0)

        // Create new layout for 2 buttons
        val ll = LinearLayout(requireContext())
        val layoutId = View.generateViewId()
        layoutIDs.add(layoutId)
        ll.id = layoutId
        ll.orientation = LinearLayout.HORIZONTAL

        // Create сolor button
        val colorButton = Button(requireContext())
        colorButton.setOnClickListener{
            ColorPickerDialogBuilder
                .with(context)
                .setTitle("Choose color")
                .initialColor(colorButton.currentTextColor)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setOnColorSelectedListener { selectedColor ->
                    colors[colorIndex] = selectedColor
                }
                .setPositiveButton("Choose") { _, selectedColor, _ ->
                    colorButton.setBackgroundColor(selectedColor)
                    colorButton.setTextColor(selectedColor) // FUCK THIS SHIT
                }
                .setNegativeButton("Cancel") { _, _ -> }
                .build()
                .show()
        }
        ll.addView(colorButton)

        // Hide previous clear button
        if (clearIDs.isNotEmpty())
            requireView().findViewById<Button>(clearIDs.last()).visibility = View.INVISIBLE

        // Create clear button
        // ID is for hiding button add new color addition
        val clearButton = Button(requireContext())
        val clearId = View.generateViewId()
        clearIDs.add(clearId)
        clearButton.id = clearId
        clearButton.text = "Clear"
        clearButton.setOnClickListener{ removeColor() }
        ll.addView(clearButton)

        led_color_gradient_host.addView(ll)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_led_color_gradient, container, false)

        view.gradient_add_color_button.setOnClickListener { addColor() }

        ArrayAdapter.createFromResource(
                requireContext(),
                R.array.blending_modes,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            view.gradient_blending.adapter = adapter }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment led_color_gradient.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            led_color_gradient().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}