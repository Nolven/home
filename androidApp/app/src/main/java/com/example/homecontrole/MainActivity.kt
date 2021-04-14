package com.example.homecontrole

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.room.Room
import com.example.homecontrole.databinding.ActivityMainBinding
import com.example.homecontrole.led.FragmentLed

class MainActivity : AppCompatActivity() {

    // TODO move modelView
    lateinit var mqtt: MqttClient
    lateinit var connectionDb: ConnectionDatabase
    lateinit var connectionDao: ConnectionDao

    // Callback function for mqtt connection establishment
    private fun onConnect()
    {
        Log.d("TAG", "CONNECTED")
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        supportFragmentManager.commit {
            replace<FragmentLed>(R.id.fragment_host)
            setReorderingAllowed(true) }
    }

    // Callback function for mqtt disconnect
    private fun onDisconnect()
    {
        Log.d("TAG", "DISCONNECTED")
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        supportFragmentManager.commit {
            replace<ConnectionFragment>(R.id.fragment_host)
            setReorderingAllowed(true) }
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Load db
        connectionDb = Room.databaseBuilder(
            applicationContext,
            ConnectionDatabase::class.java, "connections").build()
        connectionDao = connectionDb.connectionDao()

        // Setup mqtt client
        mqtt = MqttClient(applicationContext)
        mqtt.successCb = this::onConnect
        mqtt.disconnectCb = this::onDisconnect

        // Assume we are disconnected form the server
        onDisconnect()

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
            }
            // TODO hide drawer
            true
        }
    }
}