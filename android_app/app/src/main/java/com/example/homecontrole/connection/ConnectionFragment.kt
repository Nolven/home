package com.example.homecontrole.connection

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import com.example.homecontrole.MainActivity
import com.example.homecontrole.databinding.FragmentConnectionBinding

private const val PARAM_LABEL = "label"
private const val PARAM_IP = "ip"
private const val PARAM_PORT = "port"

class ConnectionFragment : Fragment() {
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

    private fun connect()
    {
        binding.statusLabel.text = "Loading"
        (requireActivity() as MainActivity).mqtt.connect(binding.ipTextEdit.text.toString(),
                binding.portTextEdit.text.toString(),
                this::onConnectionFail)
    }

    private fun onConnectionFail(reason: String){
        binding.statusLabel.text = reason
    }

    private fun showConnection(connection: ConnectionEntity)
    {
        val ll = LinearLayout(requireContext())
        ll.orientation = LinearLayout.HORIZONTAL

        val text = TextView(requireContext())
        text.text = "${connection.url}:${connection.port}"
        text.setOnClickListener{
            binding.ipTextEdit.setText(connection.url)
            binding.portTextEdit.setText(connection.port)

            connect()
        }

        ll.addView(text)

        val closeButton = Button(requireContext())
        closeButton.text = "Remove" // change to icon
        closeButton.setOnClickListener {
            model.remove(connection)
            binding.connectionsHolder.removeView(ll)
        }

        ll.addView(closeButton)

        binding.connectionsHolder.addView(ll)
    }

    private lateinit var model: ConnectionViewModel
    private var _binding: FragmentConnectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        _binding = FragmentConnectionBinding.inflate(inflater, container, false)

        arguments?.let {
            binding.portTextEdit.setText(it.getString(PARAM_PORT))
            binding.ipTextEdit.setText(it.getString(PARAM_IP))
            binding.statusLabel.text = it.getString(PARAM_LABEL)
        }

        if( !binding.portTextEdit.text.isNullOrEmpty() && !binding.ipTextEdit.text.isNullOrEmpty() )
            connect()

        // TODO change to injection
        model = ConnectionViewModelFactory(
            ConnectionRepository(
                ConnectionDatabase.getInstance(requireContext()).connectionDao())
        ).create(ConnectionViewModel::class.java)

        model.connections.observe(viewLifecycleOwner, { notIt ->
            binding.connectionsHolder.removeAllViews()
            notIt.forEach{ showConnection(it) }
        })

        binding.connectButton.setOnClickListener {
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
                model.insert(ConnectionEntity(binding.ipTextEdit.text.toString(), binding.portTextEdit.text.toString()))
                // TODO add loading wheel or something
                connect()
            }
        }

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(ip: String, port: String, label: String) =
            ConnectionFragment().apply {
                arguments = Bundle().apply {
                    putString(PARAM_IP, ip)
                    putString(PARAM_PORT, port)
                    putString(PARAM_LABEL, label)
                }
            }
    }
}