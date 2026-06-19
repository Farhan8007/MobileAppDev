package com.example.gymfees.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.gymfees.R
import com.example.gymfees.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("gym_prefs", Context.MODE_PRIVATE)
        val defaultTemplate = getString(R.string.default_sms_template)
        val savedTemplate = prefs.getString("sms_template", defaultTemplate)

        binding.etSmsTemplate.setText(savedTemplate)

        binding.btnSaveSettings.setOnClickListener {
            val newTemplate = binding.etSmsTemplate.text.toString().trim()
            if (newTemplate.isNotEmpty()) {
                prefs.edit().putString("sms_template", newTemplate).apply()
                Toast.makeText(requireContext(), "Template saved successfully", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
