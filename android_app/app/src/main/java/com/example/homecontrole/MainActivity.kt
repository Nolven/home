package com.example.homecontrole

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.homecontrole.alarm.AlarmFragment
import com.example.homecontrole.connection.ConnectionFragment
import com.example.homecontrole.databinding.ActivityMainBinding
import com.example.homecontrole.led.FragmentLed
import com.example.homecontrole.led.GeneralData
import com.example.homecontrole.statistics.Statistics
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.eclipse.paho.client.mqttv3.MqttMessage
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    // TODO move to repository
    lateinit var mqtt: MqttClient

    // Callback function for mqtt connection establishment
    private fun onConnect(ip: String, port: String)
    {
        // Save last successful connection
        with(getPreferences(Context.MODE_PRIVATE).edit())
        {
            putString(getString(R.string.pref_key_ip), ip)
            putString(getString(R.string.pref_key_port), port)
            apply()
        }
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        supportFragmentManager.commit {
            replace<FragmentLed>(R.id.fragment_host)
            setReorderingAllowed(true) }
    }

    // Callback function for mqtt disconnect
    private fun onDisconnect(cause: String)
    {
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        getPreferences(Context.MODE_PRIVATE).also {
            supportFragmentManager.commit {
                replace(R.id.fragment_host, ConnectionFragment.newInstance(
                    it.getString(getString(R.string.pref_key_ip), "")!!,
                    it.getString(getString(R.string.pref_key_port), "")!!,
                    cause))
                setReorderingAllowed(true) }
        }
    }

    public lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Setup mqtt client
        mqtt = MqttClient(applicationContext)
        mqtt.successCb = this::onConnect
        mqtt.disconnectCb = this::onDisconnect

        onDisconnect("")

        binding.navigationView.setNavigationItemSelectedListener{
            when (it.itemId)
            {
                R.id.nav_led -> {
                    supportFragmentManager.commit {
                        replace<FragmentLed>(R.id.fragment_host)
                        setReorderingAllowed(true)
                        addToBackStack(null) }
                }
                R.id.nav_stats -> {
                    supportFragmentManager.commit {
                        replace<Statistics>(R.id.fragment_host)
                        setReorderingAllowed(true)
                        addToBackStack(null) }
                }
                R.id.nav_connection -> {
                    supportFragmentManager.commit {
                        replace<ConnectionFragment>(R.id.fragment_host)
                        setReorderingAllowed(true)
                        addToBackStack(null) }
                }
                R.id.nav_alarm -> {
                    supportFragmentManager.commit {
                        replace<AlarmFragment>(R.id.fragment_host)
                        setReorderingAllowed(true)
                        addToBackStack(null) }
                }
            }
            // TODO hide drawer
            true
        }

        binding.backButton.setOnClickListener{
            supportFragmentManager.popBackStack()
        }

        binding.settingsButton.setOnClickListener {
            supportFragmentManager.commit {
                replace<SettingsFragment>(R.id.fragment_host)
                setReorderingAllowed(true)
                addToBackStack(null) }
        }

        binding.lightsOutButton.setOnClickListener{
            // send brightness 0 to each zone, each room
            val json = JsonObject()
            json.add("general_data", Gson().toJsonTree(GeneralData(0,0,0)))
            thread{
                for (i in 0..10) {
                    json.addProperty("zone", i)
                    mqtt.publish("room/led", json.toString())
                    mqtt.publish("hallway/led", json.toString())
                    Thread.sleep(10)
                }
            }

            // disable air display
            mqtt.publish("air/display", "0")
        }

    }
}