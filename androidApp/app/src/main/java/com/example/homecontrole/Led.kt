package com.example.homecontrole

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.android.synthetic.main.fragment_led.view.*
import kotlinx.android.synthetic.main.led_color_static.view.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Led : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    // Menus
    private lateinit var colorMenu: PopupMenu
    private lateinit var modeMenu: PopupMenu

    private lateinit var gradient: Gradient
    private var staticColor: Int = 0xFFFF00FF.toInt()

    private fun setupMenus()
    {
        //Color menu
        colorMenu.inflate(R.menu.led_color_mode)
        colorMenu.setOnMenuItemClickListener {
            it.isChecked = true
            when( it.itemId )
            {
                R.id.static_color -> {
                    requireView().led_color_static_include.visibility = View.VISIBLE
                    requireView().led_color_gradient_include.visibility = View.GONE
                    requireView().led_color_random_include.visibility = View.GONE
                }
                R.id.random_color -> {
                    requireView().led_color_static_include.visibility = View.GONE
                    requireView().led_color_gradient_include.visibility = View.GONE
                    requireView().led_color_random_include.visibility = View.VISIBLE
                }
                R.id.gradient_color -> {
                    requireView().led_color_static_include.visibility = View.GONE
                    requireView().led_color_gradient_include.visibility = View.VISIBLE
                    requireView().led_color_random_include.visibility = View.GONE
                }
                R.id.none -> {
                    requireView().led_color_static_include.visibility = View.GONE
                    requireView().led_color_gradient_include.visibility = View.GONE
                    requireView().led_color_random_include.visibility = View.GONE
                }
            }
            true
        }

        // State menu
        modeMenu.inflate(R.menu.led_state_mode)
        modeMenu.setOnMenuItemClickListener {
            it.isChecked = true
            when( it.itemId )
            {
                R.id.snake_state -> { requireView().led_mode_snake_include.visibility = View.VISIBLE }
                R.id.none -> { requireView().led_mode_snake_include.visibility = View.GONE } // TODO Fall through probably exists
                R.id.static_state -> { requireView().led_mode_snake_include.visibility = View.GONE }
            }
            true
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_led, container, false)

        // Create menus
        colorMenu = PopupMenu(requireContext(), view.led_color_wrap_button)
        modeMenu = PopupMenu(requireContext(), view.led_mode_wrap_button)
        setupMenus()

        gradient = Gradient(view.led_color_gradient_include, requireContext())

        val colorButton: Button =  view.led_color_static_include.color_button
        colorButton.setBackgroundColor(staticColor)
        colorButton.setOnClickListener {
            view.led_color_static_include.color_button.setOnClickListener{
                ColorPickerDialogBuilder
                    .with(context)
                    .setTitle("Choose color")
                    .initialColor(staticColor)
                    .lightnessSliderOnly()
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .setOnColorSelectedListener { selectedColor ->
                        staticColor = selectedColor
                        colorButton.setBackgroundColor(selectedColor)
                    }
                    .setPositiveButton("Choose") { _, selectedColor, _ ->
                        colorButton.setBackgroundColor(selectedColor)
                    }
                    .setNegativeButton("Cancel") { _, _ -> }
                    .build()
                    .show()
            }
        }

        // Room names spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.room_names,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            view.room_spinner.adapter = adapter }

        // Zones spinner
        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, (0..10).toList())
                .also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            view.zone_spinner.adapter = it }

        setButtons(view)

        return view
    }

    private fun setButtons(v: View)
    {
        v.led_mode_wrap_button.setOnClickListener { modeMenu.show() }
        v.led_color_wrap_button.setOnClickListener { colorMenu.show() }

        v.led_general_wrap_button.setOnClickListener {
            v.led_general_include.visibility = when( v.led_general_include.visibility )
            {
                View.VISIBLE -> View.GONE
                View.GONE -> View.VISIBLE
                else -> throw IllegalStateException()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                Led().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}