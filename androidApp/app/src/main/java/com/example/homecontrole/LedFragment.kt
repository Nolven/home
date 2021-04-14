@file:JvmName("ConnectionFragmentKt")

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
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.fragment.app.Fragment
import com.example.homecontrole.databinding.FragmentLedBinding
import com.example.homecontrole.databinding.LedGeneralBinding
import com.example.homecontrole.databinding.LedModeSnakeBinding
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.lang.Integer.parseInt

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

// TODO split to classes

class FragmentLed : Fragment() {
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
            binding.colorModeButton.text = it.title
            when( it.itemId )
            {
                R.id.static_color -> {
                    binding.ledColorStaticInclude.hostLayout.visibility = View.VISIBLE
                    binding.ledColorGradientInclude.hostLayout.visibility = View.GONE
                    binding.ledColorRandomInclude.hostLayout.visibility = View.GONE

                    colorDataFunction = { Gson().toJsonTree(staticColorData) }
                    colorJsonName = "static_color"
                }
                R.id.random_color -> {
                    binding.ledColorStaticInclude.hostLayout.visibility = View.GONE
                    binding.ledColorGradientInclude.hostLayout.visibility = View.GONE
                    binding.ledColorRandomInclude.hostLayout.visibility = View.VISIBLE

                    colorDataFunction = {
                        val o = JsonObject()
                        o.addProperty("delay", parseInt(binding.ledColorRandomInclude.colorRandomDelay.text.toString()))
                        o
                    }
                    colorJsonName = "rnd_color"
                }
                R.id.gradient_color -> {
                    binding.ledColorStaticInclude.hostLayout.visibility = View.GONE
                    binding.ledColorGradientInclude.hostLayout.visibility = View.VISIBLE
                    binding.ledColorRandomInclude.hostLayout.visibility = View.GONE

                    colorDataFunction = { gradient.getJson() }
                    colorJsonName = "grad_color"
                }
                R.id.none -> {
                    binding.ledColorStaticInclude.hostLayout.visibility = View.GONE
                    binding.ledColorGradientInclude.hostLayout.visibility = View.GONE
                    binding.ledColorRandomInclude.hostLayout.visibility = View.GONE
                    colorJsonName = ""
                }
            }
            true
        }

        // State menu
        modeMenu.inflate(R.menu.led_state_mode)
        modeMenu.setOnMenuItemClickListener {
            it.isChecked = true
            binding.modeButton.text = it.title
            when( it.itemId )
            {
                R.id.snake_state -> {
                    binding.modeSnakeInclude.hostLayout.visibility = View.VISIBLE
                    modeJsonName = "snake_state"
                    modeDataFunction = { Gson().toJsonTree(snakeStateData) }
                }
                R.id.static_state -> {
                    binding.modeSnakeInclude.hostLayout.visibility = View.GONE
                    modeJsonName = "static_state"
                    modeDataFunction = { JsonPrimitive("") }
                }
                R.id.none -> {
                    binding.modeSnakeInclude.hostLayout.visibility = View.GONE
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
            json.addProperty("zone", parseInt(binding.zoneSpinner.selectedItem.toString()))
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
            json.addProperty("zone", parseInt(binding.zoneSpinner.selectedItem.toString()))
            json.add(modeJsonName, modeDataFunction())
            Log.d("State", json.toString())
            // TODO change topic name for the room spinner value
            (requireActivity() as MainActivity).mqtt.publish("abc", json.toString())
        }
    }

    private fun sendGeneralData()
    {
        if( colorJsonName.isNotEmpty() )
        {
            val json = JsonObject()
            json.addProperty("zone", parseInt(binding.zoneSpinner.selectedItem.toString()))
            json.add("general", Gson().toJsonTree(generalData))
            Log.d("General", json.toString())
            // TODO change topic name for the room spinner value
            (requireActivity() as MainActivity).mqtt.publish("abc", json.toString())
        }
    }

    private var _binding: FragmentLedBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = FragmentLedBinding.inflate(inflater, container, false)
        val view = binding.root

        // Create menus
        colorMenu = PopupMenu(requireContext(), binding.colorModeButton)
        modeMenu = PopupMenu(requireContext(), binding.modeButton)
        setupMenus(view)

        gradient = Gradient(binding.ledColorGradientInclude, requireContext())

        setSnakeChangeListener(binding.modeSnakeInclude)
        setGeneralChangeListener(binding.generalInclude)

        val colorButton: Button =  binding.ledColorStaticInclude.colorButton
        colorButton.setBackgroundColor(Color.rgb(
                staticColorData.R,
                staticColorData.G,
                staticColorData.B))
        colorButton.setOnClickListener {
            val initialColor = Color.rgb(
                    staticColorData.R,
                    staticColorData.G,
                    staticColorData.B)
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

        // Room names spinner
        ArrayAdapter.createFromResource(
                requireContext(),
                R.array.room_names,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.roomSpinner.adapter = adapter }

        // Zones spinner
        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, (0..10).toList())
                .also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.zoneSpinner.adapter = it }

        setButtons(view)

        return view
    }

    private fun setSnakeChangeListener(binding: LedModeSnakeBinding) {
        binding.length.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                snakeStateData.length = parseInt(s.toString())
            }
        })

        binding.delay.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                snakeStateData.delay = parseInt(s.toString())
            }
        })

        binding.snakeHere.setOnClickListener { snakeStateData.direction = 1 }
        binding.snakeThere.setOnClickListener { snakeStateData.direction = -1 }
        binding.checkboxMeat.setOnClickListener { snakeStateData.loop = binding.checkboxMeat.isChecked }
    }

    private fun setGeneralChangeListener(binding: LedGeneralBinding)
    {
        binding.zoneStartTextEdit.addTextChangedListener( object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if( s.toString().isNotEmpty())
                    generalData.start = parseInt(s.toString()) }
        })

        binding.zoneEndTextEdit.addTextChangedListener( object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if( s.toString().isNotEmpty())
                    generalData.end = parseInt(s.toString()) }
        })

        binding.brightnessTextEdit.addTextChangedListener( object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if( s.toString().isNotEmpty())
                    generalData.brightness = parseInt(s.toString()) }
        })
    }

    private fun setButtons(v: View)
    {
        binding.modeButton.setOnClickListener { modeMenu.show() }
        binding.colorModeButton.setOnClickListener { colorMenu.show() }

        binding.ledSendButton.setOnClickListener{
            sendColorData()
            sendGeneralData()
            sendStateData()
        }

        binding.generalButton.setOnClickListener {
            binding.generalInclude.hostLayout.visibility = when( binding.generalInclude.hostLayout.visibility )
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
                FragmentLed().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}