package com.example.homecontrole

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_connection.view.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class Connection : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var requestCode = 0
    private val requiredPermissions = arrayOf(
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.INTERNET)

    private fun havePermissions(): Boolean{
        var isOk = true
        requiredPermissions.forEach {
            isOk = ContextCompat.checkSelfPermission(requireContext(), it) == PermissionChecker.PERMISSION_GRANTED
        }
        return isOk
    }

    private fun showExplanation() {
        android.app.AlertDialog.Builder(requireContext())
        .setTitle("Permission needed")
        .setMessage("TO DO at least something")
        .setPositiveButton(android.R.string.ok) { _, _ ->
        requestPermissions(requireActivity(), requiredPermissions, 1) }
                .create()
                .show()
    }

    private fun onConnectionFail(reason: String){
        requireView().error_label.text = reason
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_connection, container, false)

        view.connect_button.setOnClickListener {
            if( !havePermissions() )
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_NETWORK_STATE) &&
                        ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.INTERNET))
                    showExplanation()
                else
                    requestPermissions(requireActivity(), requiredPermissions, requestCode)
            }
            else
            {
                (requireActivity() as MainActivity).mqtt.connect(view.connection_ip.text.toString(),
                        view.connection_port.text.toString(),
                        this::onConnectionFail)
            }
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Connection().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}