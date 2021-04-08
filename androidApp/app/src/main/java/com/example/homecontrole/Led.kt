package com.example.homecontrole

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.PopupMenu
import android.widget.RadioGroup
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import kotlinx.android.synthetic.main.fragment_led.view.*
import kotlinx.android.synthetic.main.led_color_random.view.*
import kotlinx.android.synthetic.main.led_color_static.view.*
import kotlinx.android.synthetic.main.led_general.view.*
import kotlinx.android.synthetic.main.led_mode_snake.*
import kotlinx.android.synthetic.main.led_mode_snake.view.*
import java.lang.Integer.parseInt

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

// TODO split to classes

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

    // Color data
    private lateinit var gradient: Gradient
    private var staticColorData: StaticColorData = StaticColorData(255, 0, 255)
    private var colorDataFunction: () -> JsonElement = {JsonPrimitive("")}
    private var colorJsonName: String = ""

    // Mode data
    private var modeJsonName: String = ""
    private var modeDataFunction: () -> JsonElement = {JsonPrimitive("")}
    private val snakeStateData = SnakeData(0, 0, true, 0)

    private val generalData = GeneralData(0,0,255)

    private fun setupMenus(v: View)
    {
        //Color menu
        colorMenu.inflate(R.menu.led_color_mode)
        colorMenu.setOnMenuItemClickListener {
            it.isChecked = true
            v.led_color_wrap_button.text = it.title
            when( it.itemId )
            {
                R.id.static_color -> {
                    requireView().led_color_static_include.visibility = View.VISIBLE
                    requireView().led_color_gradient_include.visibility = View.GONE
                    requireView().led_color_random_include.visibility = View.GONE

                    colorDataFunction = { Gson().toJsonTree(staticColorData) }
                    colorJsonName = "static_color"
                }
                R.id.random_color -> {
                    requireView().led_color_static_include.visibility = View.GONE
                    requireView().led_color_gradient_include.visibility = View.GONE
                    requireView().led_color_random_include.visibility = View.VISIBLE

                    colorDataFunction = {
                        val o = JsonObject()
                        o.addProperty("delay", v.led_color_random_include.color_random_delay.text.toString())
                        o
                    }
                    colorJsonName = "rnd_color"
                }
                R.id.gradient_color -> {
                    requireView().led_color_static_include.visibility = View.GONE
                    requireView().led_color_gradient_include.visibility = View.VISIBLE
                    requireView().led_color_random_include.visibility = View.GONE

                    colorDataFunction = { gradient.getJson() }
                    colorJsonName = "gradient"
                }
                R.id.none -> {
                    requireView().led_color_static_include.visibility = View.GONE
                    requireView().led_color_gradient_include.visibility = View.GONE
                    requireView().led_color_random_include.visibility = View.GONE
                    colorJsonName = ""
                }
            }
            true
        }

        // State menu
        modeMenu.inflate(R.menu.led_state_mode)
        modeMenu.setOnMenuItemClickListener {
            it.isChecked = true
            v.led_mode_wrap_button.text = it.title
            when( it.itemId )
            {
                R.id.snake_state -> {
                    requireView().led_mode_snake_include.visibility = View.VISIBLE
                    modeJsonName = "snake_state"
                    modeDataFunction = { Gson().toJsonTree(snakeStateData) }
                }
                R.id.static_state -> {
                    requireView().led_mode_snake_include.visibility = View.GONE
                    modeJsonName = "static_state"
                    modeDataFunction = { JsonPrimitive("") }
                }
                R.id.none -> {
                    requireView().led_mode_snake_include.visibility = View.GONE
                    modeJsonName = ""
                }
            }
            true
        }
    }

    // Move to one method
    private fun sendColorData(){
        if( colorJsonName.isNotEmpty() )
        {
            val json = JsonObject()
            json.addProperty("zone", requireView().zone_spinner.selectedItem.toString())
            json.add(colorJsonName, colorDataFunction())
            Log.d("Color", json.toString())
            // TODO change topic name for the room spinner value
            (requireActivity() as MainActivity).mqtt.publish("abc", json.toString())
        }
    }

    private fun sendStateData()
    {
        if ( modeJsonName.isNotEmpty() )
        {
            val json = JsonObject()
            json.addProperty("zone", requireView().zone_spinner.selectedItem.toString())
            json.add(modeJsonName, modeDataFunction())
            Log.d("State", json.toString())
            // TODO change topic name for the room spinner value
            (requireActivity() as MainActivity).mqtt.publish("abc", json.toString())
        }
    }

    private fun sendGeneralData()
    {
        if( requireView().led_general_include.visibility == View.VISIBLE )
        {
            val json = JsonObject()
            json.addProperty("zone", requireView().zone_spinner.selectedItem.toString())
            json.add("general", Gson().toJsonTree(generalData))
            Log.d("General", json.toString())
            // TODO change topic name for the room spinner value
            (requireActivity() as MainActivity).mqtt.publish("abc", json.toString())
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_led, container, false)

        // Create menus
        colorMenu = PopupMenu(requireContext(), view.led_color_wrap_button)
        modeMenu = PopupMenu(requireContext(), view.led_mode_wrap_button)
        setupMenus(view)

        gradient = Gradient(view.led_color_gradient_include, requireContext())

        setSnakeChangeListener(view.led_mode_snake_include)

        setGeneralChangeListener(view.led_general_include)

        val colorButton: Button =  view.led_color_static_include.color_button
        colorButton.setBackgroundColor(Color.rgb(
                staticColorData.R,
                staticColorData.G,
                staticColorData.B))
        colorButton.setOnClickListener {
            val initialColor = Color.rgb(
                    staticColorData.R,
                    staticColorData.G,
                    staticColorData.B)
            view.led_color_static_include.color_button.setOnClickListener{
                ColorPickerDialogBuilder
                    .with(context)
                    .setTitle("Choose color")
                    .initialColor(initialColor)
                    .lightnessSliderOnly()
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .setOnColorSelectedListener { selectedColor ->
                        staticColorData.R = selectedColor.red
                        staticColorData.G = selectedColor.green
                        staticColorData.B = selectedColor.blue
                        colorButton.setBackgroundColor(selectedColor)
                    }
                    .setPositiveButton("Choose") { _, _, _ ->}
                    .setNegativeButton("Cancel") { _, _ ->
                        staticColorData.R = initialColor.red
                        staticColorData.G = initialColor.green
                        staticColorData.B = initialColor.blue
                        colorButton.setBackgroundColor(initialColor)
                    }
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

    private fun setSnakeChangeListener(v: View) {
        v.length.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                snakeStateData.length = parseInt(s.toString())
            }
        })

        v.delay.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                snakeStateData.delay = parseInt(s.toString())
            }
        })

        v.snake_here.setOnClickListener { snakeStateData.direction = 1 }
        v.snake_there.setOnClickListener { snakeStateData.direction = -1 }
        v.checkbox_meat.setOnClickListener { snakeStateData.loop = v.checkbox_meat.isChecked }
    }

    private fun setGeneralChangeListener(v: View)
    {
        v.led_zone_start.addTextChangedListener( object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { generalData.start = parseInt(s.toString()) }
        })

        v.led_zone_end.addTextChangedListener( object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { generalData.end = parseInt(s.toString()) }
        })

        v.led_zone_brightness.addTextChangedListener( object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { generalData.brightness = parseInt(s.toString()) }
        })
    }

    private fun setButtons(v: View)
    {
        v.led_mode_wrap_button.setOnClickListener { modeMenu.show() }
        v.led_color_wrap_button.setOnClickListener { colorMenu.show() }

        v.led_send_button.setOnClickListener{
            sendColorData()
            sendGeneralData()
            sendStateData()
        }

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