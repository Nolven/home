@file:JvmName("ConnectionFragmentKt")

package com.example.homecontrole.led

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import com.example.homecontrole.MainActivity
import com.example.homecontrole.R
import com.example.homecontrole.databinding.FragmentLedBinding
import com.example.homecontrole.databinding.LedGeneralBinding
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

    private lateinit var general: General

    // Color data
    private lateinit var gradientColor: GradientColor
    private lateinit var staticColor: StaticColor
    private var colorDataFunction: () -> JsonElement = { JsonPrimitive("") }
    private var colorJsonName: String = ""

    // Mode data
    private var modeJsonName: String = ""
    private lateinit var snakeMode: SnakeMode
    private var modeDataFunction: () -> JsonElement = { JsonPrimitive("") }

    private fun openColorInclude(id: Int)
    {
        when (id) {
            R.id.static_color -> {
                binding.ledColorStaticInclude.hostLayout.visibility = View.VISIBLE
                binding.ledColorGradientInclude.hostLayout.visibility = View.GONE
                binding.ledColorRandomInclude.hostLayout.visibility = View.GONE

                colorDataFunction = { staticColor.getJson() }
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

                colorDataFunction = { gradientColor.getJson() }
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
                modeDataFunction = { snakeMode.getJson() }
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
        json.add("general_data", general.getJson())

        Log.d(logTag, json.toString())
        sendData(json.toString())
    }

    private var _binding: FragmentLedBinding? = null
    private val binding get() = _binding!!

    private fun updateAll(json: JsonObject)
    {
        Log.d(logTag,"Dynamic update in poggers")
        // Check whether we need to open menus
        val isAutoOpen = PreferenceManager.getDefaultSharedPreferences(activity).getBoolean("led_open", false)

        if( json.has("general_data") )
            general.update(json["general_data"].asJsonObject)

        if( json.has("display_mode") )
        {
            when(json["display_mode"].asString)
            {
                "snake" ->
                {
                    if (isAutoOpen) openModeInclude(R.id.snake_state) // TODO duct tape
                    if (binding.modeSnakeInclude.hostLayout.visibility == View.VISIBLE)
                        snakeMode.update(json["display_data"].asJsonObject)
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
                        staticColor.update(json["color_data"].asJsonObject)
                }
                "gradient" ->
                {
                    if (isAutoOpen) openColorInclude(R.id.gradient_color)
                    if( binding.ledColorGradientInclude.hostLayout.visibility == View.VISIBLE )
                        gradientColor.update(json["color_data"].asJsonObject)
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        _binding = FragmentLedBinding.inflate(inflater, container, false)
        val view = binding.root

        // Create menus
        colorMenu = PopupMenu(requireContext(), binding.colorModeButton)
        modeMenu = PopupMenu(requireContext(), binding.modeButton)
        setupMenus()

        // Support classes
        gradientColor = GradientColor(binding.ledColorGradientInclude, requireContext())
        staticColor = StaticColor(binding.ledColorStaticInclude, requireContext())
        snakeMode = SnakeMode(binding.modeSnakeInclude, requireContext())
        general = General(binding.generalInclude, requireContext())

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