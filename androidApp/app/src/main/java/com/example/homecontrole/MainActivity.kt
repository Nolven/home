package com.example.homecontrole

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigation_view.setNavigationItemSelectedListener{ it: MenuItem ->
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
                else -> {}
            }
            true
        }
    }
}