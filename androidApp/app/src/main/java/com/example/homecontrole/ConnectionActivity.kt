package com.example.homecontrole

import android.Manifest.permission.ACCESS_NETWORK_STATE
import android.Manifest.permission.INTERNET
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import kotlinx.android.synthetic.main.activity_connection.*


class ConnectionActivity : Activity() {

    // TODO add textWatcher for ip input

    fun onSuccess(){
        startActivity(Intent(this, MainActivity::class.java))
    }

    fun onFail(reason: String){
        error_label.text = reason
    }

    private fun showExplanation(title: String,
                                message: String,
                                permission: String,
                                permissionRequestCode: Int) {
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok) { _, id ->  ActivityCompat.requestPermissions(this, arrayOf(ACCESS_NETWORK_STATE, INTERNET), 1) }
        Log.d("TAG", "Builder shown")
        builder.create().show()
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode)
        {
            0 -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                                grantResults[0] == PERMISSION_GRANTED)) {
                    (application as MqttApplication).mqtt = MqttClient(applicationContext, connection_ip.text.toString(), connection_port.text.toString())
                    (application as MqttApplication).mqtt.connect(this::onSuccess, this::onFail)
                    TODO("CODE DUP")
                } else {
                    error_label.text = "fuck you"
                }
                return
            }
        }
    }

    val TAG = "A"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)

        connect_button.setOnClickListener {
            val code = 0
            error_label.text = ""
            if( ContextCompat.checkSelfPermission(applicationContext, ACCESS_NETWORK_STATE) != PERMISSION_GRANTED )
            {
                Log.d(TAG, "Permission in not yet granted")

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_NETWORK_STATE))
                {
                    showExplanation("Permission Needed", "Rationale", ACCESS_NETWORK_STATE, code)
                    Log.d(TAG, "Explanation shown")
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(ACCESS_NETWORK_STATE, INTERNET), code)
                    Log.d(TAG, "Explanation NOT shown")
                }

                Log.d("A", "Permission should've been requested")
            }
            else
            {

                (application as MqttApplication).mqtt = MqttClient(applicationContext, connection_ip.text.toString(), connection_port.text.toString())
                (application as MqttApplication).mqtt.connect(this::onSuccess, this::onFail);
            }
        }
    }
}

