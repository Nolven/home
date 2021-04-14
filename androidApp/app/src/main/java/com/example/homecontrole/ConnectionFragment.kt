package com.example.homecontrole

import android.Manifest
import android.os.AsyncTask
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
import com.example.homecontrole.databinding.FragmentConnectionBinding

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

    private fun loadConnectionsFromDb(v: View)
    {
        binding.connectionsHolder.removeAllViews()
        val connections = (requireActivity() as MainActivity).connectionDao.getAll()
        connections.forEach{ showConnection(it) }
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
        closeButton.setOnClickListener { removeConnection(connection, ll) }

        ll.addView(closeButton)

        binding.connectionsHolder.addView(ll)
    }

    private fun addConnection(connection: ConnectionEntity)
    {
        var exisits = true
        AsyncTask.execute {
            if( !(requireActivity() as MainActivity).connectionDao.exists(connection.url, connection.port) )
            {
                exisits = false
                (requireActivity() as MainActivity).connectionDao.insert(connection)
            }
        }
        if( !exisits ) // TODO shitty but i'm tired
            showConnection(connection)
    }

    // from db as well
    private fun removeConnection(connection: ConnectionEntity, ll: LinearLayout)
    {
        AsyncTask.execute { (requireActivity() as MainActivity).connectionDao.delete(connection) }
        binding.connectionsHolder.removeView(ll)
    }

    private var _binding: FragmentConnectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View {

        _binding = FragmentConnectionBinding.inflate(inflater, container, false)
        val view = binding.root

        AsyncTask.execute{loadConnectionsFromDb(view)}

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
                // TODO change to modelView as big boiiiiiiiiiiiiiiiiiiiiii
                val con = ConnectionEntity(binding.ipTextEdit.text.toString(), binding.portTextEdit.text.toString())
                addConnection(con)
                // TODO add loading wheel or something
                connect()
            }
        }

        return view
    }
}