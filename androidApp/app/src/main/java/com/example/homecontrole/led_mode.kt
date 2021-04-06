package com.example.homecontrole

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.PopupMenu
import androidx.annotation.MenuRes
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_led.view.*
import kotlinx.android.synthetic.main.fragment_led_color.view.*
import kotlinx.android.synthetic.main.fragment_led_mode.view.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [led_mode.newInstance] factory method to
 * create an instance of this fragment.
 */
class led_mode : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), v)
        popup.inflate(menuRes)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            menuItem.isChecked = true
            v.led_mode_button.text = menuItem.title.toString()

            when(menuItem.itemId)
            {
                R.id.snake_state -> {
                    requireActivity().supportFragmentManager.commit {
                        replace<led_mode_snake>(R.id.led_mode_fragment_host)
                        setReorderingAllowed(true)
                        addToBackStack(null) }
                }
                R.id.static_state -> {
//                    requireActivity().supportFragmentManager.commit {
//                        replace<led_color_random>(R.id.led_color_fragment_host)
//                        setReorderingAllowed(true)
//                        addToBackStack(null) }
                }
            }
            true
        }

        popup.setOnDismissListener {
            true
        }

        // Show the popup menu.
        popup.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_led_mode, container, false)

        view.led_mode_button.setOnClickListener { showMenu(requireView(), R.menu.led_state_modes) }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment led_mode.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            led_mode().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}