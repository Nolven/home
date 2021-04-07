package com.example.homecontrole

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var mqtt: MqttClient

    // Callback function for mqtt connection establishment
    private fun onConnect()
    {
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        supportFragmentManager.commit {
            replace<Led>(R.id.fragment_host)
            setReorderingAllowed(true) }
    }

    // Callback function for mqtt disconnect
    private fun onDisconnect()
    {
        drawer_layout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        supportFragmentManager.commit {
            replace<Connection>(R.id.fragment_host)
            setReorderingAllowed(true) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup mqtt client
        mqtt = MqttClient(applicationContext)
        mqtt.successCb = this::onConnect
        mqtt.disconnectCb = this::onDisconnect

        // Assume we are disconnected form the server
        onDisconnect()

        navigation_view.setNavigationItemSelectedListener{
            when (it.itemId)
            {
                R.id.nav_led -> {
                    supportFragmentManager.commit {
                        replace<Led>(R.id.fragment_host)
                        setReorderingAllowed(true)
                        addToBackStack(null) }
                }
                R.id.nav_stats -> {
                    supportFragmentManager.commit {
                        replace<statistics>(R.id.fragment_host)
                        setReorderingAllowed(true)
                        addToBackStack(null) }
                }
                R.id.nav_connection -> {
                    supportFragmentManager.commit {
                        replace<Connection>(R.id.fragment_host)
                        setReorderingAllowed(true)
                        addToBackStack(null) }
                }
            }
            true
        }
    }
}