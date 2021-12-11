package com.example.homecontrole.statistics

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.homecontrole.MainActivity
import com.example.homecontrole.connection.ConnectionDatabase
import com.example.homecontrole.connection.ConnectionRepository
import com.example.homecontrole.connection.ConnectionViewModel
import com.example.homecontrole.connection.ConnectionViewModelFactory
import com.example.homecontrole.databinding.FragmentStatisticsBinding


class Statistics : Fragment() {
    private lateinit var model: StatisticsViewModel
    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        model = StatisticsViewModel((requireActivity() as MainActivity).mqtt)
        model.air.observe(viewLifecycleOwner, {
            binding.co2ValueLabel.text = it["co2"].toString()
            binding.temperatureValueLabel.text = it["temperature"].toString()
            binding.humidityValueLabel.text = it["humidity"].toString()
        })

        binding.climateToggleButton.setOnClickListener {
            (requireActivity() as MainActivity).mqtt.publish("air/display", "")
        }

        return binding.root
    }

}