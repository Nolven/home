package com.example.homecontrole

import android.os.Bundle
import android.text.TextUtils.replace
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import androidx.annotation.MenuRes
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import kotlinx.android.synthetic.main.fragment_led_color.*
import kotlinx.android.synthetic.main.fragment_led_color.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [led_color.newInstance] factory method to
 * create an instance of this fragment.
 */
class led_color : Fragment() {
    private fun showMenu(v: View, @MenuRes menuRes: Int) {
        val popup = PopupMenu(requireContext(), v)
        popup.inflate(menuRes)

        popup.setOnMenuItemClickListener { menuItem: MenuItem ->
            menuItem.isChecked = true
            v.menu_button.text = menuItem.title.toString() // Change

            when(menuItem.itemId)
            {
                R.id.static_color -> {
                    requireActivity().supportFragmentManager.commit {
                        replace<led_color_static>(R.id.led_color_fragment_host)
                        setReorderingAllowed(true)
                        addToBackStack(null) }
                }
                R.id.random_color -> {
                    requireActivity().supportFragmentManager.commit {
                        replace<led_color_random>(R.id.led_color_fragment_host)
                        setReorderingAllowed(true)
                        addToBackStack(null) }
                }
                R.id.gradient_color -> {
                    requireActivity().supportFragmentManager.commit {
                        replace<led_color_gradient>(R.id.led_color_fragment_host)
                        setReorderingAllowed(true)
                        addToBackStack(null) }
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
        val view = inflater.inflate(R.layout.fragment_led_color, container, false)

        view.menu_button.setOnClickListener { v: View ->
            showMenu(v, R.menu.led_color_mode)
        }

        // Inflate the layout for this fragment
        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment color.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            led_color().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}