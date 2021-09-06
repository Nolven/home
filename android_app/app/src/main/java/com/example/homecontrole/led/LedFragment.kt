@file:JvmName("ConnectionFragmentKt")

package com.example.homecontrole.led

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
import com.example.homecontrole.MainActivity
import com.example.homecontrole.R
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
import kotlin.math.floor
import androidx.preference.PreferenceManager
import android.widget.AdapterView




class FragmentLed : Fragment() {
    private lateinit var model: LedViewModel
    private val logTag = "LED"

    // I'm shitcoding rn, feature me, pls forgive me
    private var isRoomUpdate: Boolean = false
    private var isHallwayUpdate: Boolean = false

    // Menus
    private lateinit var colorMenu: PopupMenu
    private lateinit var modeMenu: PopupMenu

    // Color data
    private lateinit var gradient: Gradient
    private var staticColorData: StaticColorData = StaticColorData(255, 0, 255)
    private var colorDataFunction: () -> JsonElement = { JsonPrimitive("") }
    private var colorJsonName: String = ""

    // Mode data
    private var modeJsonName: String = ""
    private var modeDataFunction: () -> JsonElement = { JsonPrimitive("") }
    private val snakeStateData = SnakeData(0, 0, true, 0)

    private val generalData = GeneralData(0, 0, 255)

    private fun openColorInclude(id: Int)
    {
        when (id) {
            R.id.static_color -> {
                binding.ledColorStaticInclude.hostLayout.visibility = View.VISIBLE
                binding.ledColorGradientInclude.hostLayout.visibility = View.GONE
                binding.ledColorRandomInclude.hostLayout.visibility = View.GONE

                colorDataFunction = { Gson().toJsonTree(staticColorData) }
                colorJsonName = "static"
            }
            R.id.random_color -> {
                binding.ledColorStaticInclude.hostLayout.visibility = View.GONE
                binding.ledColorGradientInclude.hostLayout.visibility = View.GONE
                binding.ledColorRandomInclude.hostLayout.visibility = View.VISIBLE

                colorDataFunction = {
                    val o = JsonObject()
                    val t = binding.ledColorRandomInclude.colorRandomDelay.text.toString()
                    o.addProperty("delay", if(t.isNotEmpty()) parseInt(t) else 0)
                    o
                }
                colorJsonName = "random"
            }
            R.id.gradient_color -> {
                binding.ledColorStaticInclude.hostLayout.visibility = View.GONE
                binding.ledColorGradientInclude.hostLayout.visibility = View.VISIBLE
                binding.ledColorRandomInclude.hostLayout.visibility = View.GONE

                colorDataFunction = { gradient.getJson() }
                colorJsonName = "gradient"
            }
            R.id.none -> {
                binding.ledColorStaticInclude.hostLayout.visibility = View.GONE
                binding.ledColorGradientInclude.hostLayout.visibility = View.GONE
                binding.ledColorRandomInclude.hostLayout.visibility = View.GONE
                colorJsonName = ""
            }
        }

    }

    private fun openModeInclude(id: Int)
    {
        when (id) {
            R.id.snake_state -> {
                binding.modeSnakeInclude.hostLayout.visibility = View.VISIBLE
                modeJsonName = "snake"
                modeDataFunction = { Gson().toJsonTree(snakeStateData) }
            }
            R.id.static_state -> {
                binding.modeSnakeInclude.hostLayout.visibility = View.GONE
                modeJsonName = "static"
                modeDataFunction = { JsonPrimitive("") }
            }
            R.id.none -> {
                binding.modeSnakeInclude.hostLayout.visibility = View.GONE
                modeJsonName = ""
            }
        }
    }

    private fun setupMenus() {
        //Color menu
        colorMenu.inflate(R.menu.led_color_mode)
        colorMenu.setOnMenuItemClickListener {
            it.isChecked = true
            binding.colorModeButton.text = it.title
            openColorInclude(it.itemId)
            true
        }

        // State menu
        modeMenu.inflate(R.menu.led_state_mode)
        modeMenu.setOnMenuItemClickListener {
            it.isChecked = true
            binding.modeButton.text = it.title
            openModeInclude(it.itemId)
            true
        }
    }

    private fun sendData(json: String)
    {
        (requireActivity() as MainActivity).mqtt.publish(binding.roomSpinner.selectedItem.toString() + "/" + getString(R.string.led_topic), json)
    }

    // Move to one method
    private fun sendColorData()
    {
        if( colorJsonName.isNotEmpty() )
        {
            val json = JsonObject()
            json.addProperty("zone", parseInt(binding.zoneSpinner.selectedItem.toString()))
            json.addProperty("color_mode", colorJsonName)
            json.add("color_data", colorDataFunction())

            Log.d(logTag, json.toString())
            sendData(json.toString())
        }
    }

    private fun sendStateData()
    {
        if ( modeJsonName.isNotEmpty() )
        {
            val json = JsonObject()
            json.addProperty("zone", parseInt(binding.zoneSpinner.selectedItem.toString()))
            json.addProperty("display_mode", modeJsonName)
            json.add("display_data", modeDataFunction())

            Log.d(logTag, json.toString())
            sendData(json.toString())
        }
    }

    private fun sendGeneralData()
    {
        val json = JsonObject()
        json.addProperty("zone", parseInt(binding.zoneSpinner.selectedItem.toString()))
        json.add("general_data", Gson().toJsonTree(generalData))

        Log.d(logTag, json.toString())
        sendData(json.toString())
    }

    private var _binding: FragmentLedBinding? = null
    private val binding get() = _binding!!

    private fun updateStaticColor(json: JsonObject)
    {
        staticColorData.R = json["R"].asInt
        staticColorData.G = json["G"].asInt
        staticColorData.B = json["B"].asInt

        binding.ledColorStaticInclude.colorButton.setBackgroundColor(Color.rgb(staticColorData.R,
                                                                                staticColorData.G,
                                                                                staticColorData.B))
    }

    private fun updateSnake(json: JsonObject)
    {
        binding.modeSnakeInclude.delay.setText(json["delay"].asString)
        binding.modeSnakeInclude.length.setText(json["length"].asString)

        binding.modeSnakeInclude.checkboxMeat.isChecked = json["loop"].asBoolean

        when(json["direction"].asInt) {
            -1 -> binding.modeSnakeInclude.directionGroup.check(R.id.snake_there)
            1 -> binding.modeSnakeInclude.directionGroup.check(R.id.snake_here)
        }

    }

    private fun updateAll(json: JsonObject)
    {
        Log.d(logTag,"Dynamic update in poggers")
        // Check whether we need to open menus
        val isAutoOpen = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("led_open", false)

        if( json.has("general_data") )
        {
            Log.d(logTag,"General update")
            binding.brightnessSlider.value = json["general_data"].asJsonObject["brightness"].asFloat
            if( binding.generalInclude.hostLayout.visibility == View.VISIBLE )
                updateGeneral(json["general_data"].asJsonObject)
        }

        if( json.has("display_mode") )
        {
            Log.d(logTag,"Mode update")
            when(json["display_mode"].asString)
            {
                "snake" ->
                {
                    if (isAutoOpen) openModeInclude(R.id.snake_state) // TODO duct tape
                    if (binding.modeSnakeInclude.hostLayout.visibility == View.VISIBLE)
                        updateSnake(json["display_data"].asJsonObject)
                }
            }
        }

        if( json.has("color_mode") )
        {
            Log.d(logTag, "${json["color_mode"].asString} color update")
            when(json["color_mode"].asString)
            {
                "static" -> {
                    if (isAutoOpen) openColorInclude(R.id.static_color)
                    if (binding.ledColorStaticInclude.hostLayout.visibility == View.VISIBLE)
                        updateStaticColor(json["color_data"].asJsonObject)
                }
                "gradient" ->
                {
                    if (isAutoOpen) openColorInclude(R.id.gradient_color)
                    if( binding.ledColorGradientInclude.hostLayout.visibility == View.VISIBLE )
                        gradient.update(json["color_data"].asJsonObject)
                }
                "random" ->
                {
                    if (isAutoOpen) openColorInclude(R.id.random_color)
                    if( binding.ledColorRandomInclude.hostLayout.visibility == View.VISIBLE )
                        binding.ledColorRandomInclude.colorRandomDelay.setText(json["color_data"].asJsonObject["delay"].asString)
                }
            }
        }
    }

    private fun updateGeneral(json: JsonObject)
    {
        binding.generalInclude.zoneStartTextEdit.setText(json["start"].asString)
        binding.generalInclude.zoneEndTextEdit.setText(json["end"].asString)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentLedBinding.inflate(inflater, container, false)
        val view = binding.root

        // Create menus
        colorMenu = PopupMenu(requireContext(), binding.colorModeButton)
        modeMenu = PopupMenu(requireContext(), binding.modeButton)
        setupMenus()

        gradient = Gradient(binding.ledColorGradientInclude, requireContext())

        setSnakeChangeListener(binding.modeSnakeInclude) // TODO non-loop snake with "here" option doesn't work
        setGeneralChangeListener(binding.generalInclude)
        binding.brightnessSlider.addOnChangeListener{ _, value, _ ->
            generalData.brightness = value.toInt()
        }
        binding.brightnessSlider.value = floor((binding.brightnessSlider.valueTo - binding.brightnessSlider.valueFrom)/2)

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

        val spinnerListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                isRoomUpdate = false
                isHallwayUpdate = false
                if (PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("led_change_update", false))
                    requestLedUpdate()
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        // Room names spinner
        ArrayAdapter.createFromResource(
                requireContext(),
            R.array.room_names,
                android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.roomSpinner.adapter = adapter }
        binding.roomSpinner.onItemSelectedListener = spinnerListener

        // Zones spinner
        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, (0..10).toList())
                .also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.zoneSpinner.adapter = it }
        binding.zoneSpinner.onItemSelectedListener = spinnerListener

        setButtons()

        // Automatic view update from server ack
        model = LedViewModel((requireActivity() as MainActivity).mqtt)
        model.hallway.observe(viewLifecycleOwner, {
            if (binding.roomSpinner.selectedItem.toString() == "hallway"
                && it["zone"].toString() == binding.zoneSpinner.selectedItem.toString())
            {
                if( PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("led_auto_update", false) )
                {
                    updateAll(it)
                    binding.ledUpdateButton.setBackgroundColor(resources.getColor(R.color.light_gray))
                    isHallwayUpdate = false
                }
                else
                {
                    binding.ledUpdateButton.setBackgroundColor(resources.getColor(R.color.dark_red))
                    isHallwayUpdate = true
                }
            }
        })
        model.room.observe(viewLifecycleOwner, {
            if (binding.roomSpinner.selectedItem.toString() == "room"
                && it["zone"].toString() == binding.zoneSpinner.selectedItem.toString())
            {
                if( PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("led_auto_update", false) )
                {
                    updateAll(it)
                    binding.ledUpdateButton.setBackgroundColor(resources.getColor(R.color.light_gray))
                    isRoomUpdate = false
                }
                else
                {
                    binding.ledUpdateButton.setBackgroundColor(resources.getColor(R.color.dark_red))
                    isRoomUpdate = true
                }
            }
        })

        return view
    }

    private fun setSnakeChangeListener(binding: LedModeSnakeBinding) {
        binding.length.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if( s.toString().isNotEmpty() )
                    snakeStateData.length = parseInt(s.toString())
            }
        })

        binding.delay.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if( s.toString().isNotEmpty() )
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
    }

    private fun requestLedUpdate()
    {
        (requireActivity() as MainActivity).mqtt.publish(binding.roomSpinner.selectedItem.toString() + "/" + getString(R.string.led_topic) + "/" + getString(R.string.led_get_topic), binding.zoneSpinner.selectedItem.toString())
    }

    private fun setButtons()
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
                View.VISIBLE -> { generalData.start = 0; generalData.end = 0; View.GONE }
                View.GONE -> View.VISIBLE
                else -> throw IllegalStateException()
            }
        }

        binding.ledUpdateButton.setOnClickListener {
            if( isHallwayUpdate && binding.roomSpinner.selectedItem.toString() == "hallway" )
            {
                model.hallway.value?.let { it1 -> updateAll(it1) }
                isHallwayUpdate = false
                binding.ledUpdateButton.setBackgroundColor(resources.getColor(R.color.light_gray))
            }
            else if( isRoomUpdate && binding.roomSpinner.selectedItem.toString() == "room" )
            {
                model.room.value?.let { it1 -> updateAll(it1) }
                isRoomUpdate = false
                binding.ledUpdateButton.setBackgroundColor(resources.getColor(R.color.light_gray))
            }
            else
                requestLedUpdate()
        }
    }
}