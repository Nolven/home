package com.example.homecontrole

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_led.*
import kotlinx.android.synthetic.main.fragment_led.view.*
import kotlinx.android.synthetic.main.fragment_led_mode.view.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class PagerAdapter(fr: Fragment) : FragmentStateAdapter(fr) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> led_mode()
            1 -> led_color()
            else -> led_mode()
        }
    }
}

/**
 * A simple [Fragment] subclass.
 * Use the [led.newInstance] factory method to
 * create an instance of this fragment.
 */
class led : Fragment() {
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_led, container, false)

        view.led_send_button.setOnClickListener {
              pager.currentItem
        }

        // Room names spinner
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.room_names,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            view.room_spinner.adapter = adapter }

        // Zones spinner
        ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, (0..10).toList())
                .also {
            it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            view.zone_spinner.adapter = it
                }

        view.pager.adapter = PagerAdapter(this)

        TabLayoutMediator(view.tab_layout, view.pager) { tab, position ->
            when(position)
            {
                0 -> tab.text = "State"
                1 -> tab.text = "Color"
                else -> tab.text = "Undef"
            }
        }.attach()

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment led.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                led().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}