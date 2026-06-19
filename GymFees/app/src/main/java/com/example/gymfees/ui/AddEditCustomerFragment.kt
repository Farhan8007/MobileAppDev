package com.example.gymfees.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.gymfees.R
import com.example.gymfees.data.Customer
import com.example.gymfees.data.GymDatabase
import com.example.gymfees.databinding.FragmentAddEditCustomerBinding
import com.example.gymfees.viewmodel.GymViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddEditCustomerFragment : Fragment() {

    private var _binding: FragmentAddEditCustomerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GymViewModel by viewModels()
    private val args: AddEditCustomerFragmentArgs by navArgs()

    private var selectedDate: Long = System.currentTimeMillis()
    private var existingCustomer: Customer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditCustomerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (args.customerId != -1L) {
            loadCustomerData(args.customerId)
            binding.btnSave.text = getString(R.string.update_customer)
        } else {
            updateDateLabel()
        }

        binding.etJoiningDate.setOnClickListener {
            showDatePicker()
        }

        binding.btnSave.setOnClickListener {
            saveCustomer()
        }
    }

    private fun loadCustomerData(id: Long) {
        lifecycleScope.launch {
            val database = GymDatabase.getDatabase(requireContext())
            val customer = database.customerDao().getCustomerById(id)
            customer?.let {
                existingCustomer = it
                binding.etName.setText(it.name)
                binding.etMobile.setText(it.mobileNumber)
                binding.etFee.setText(it.monthlyFee.toString())
                binding.etNotes.setText(it.notes)
                selectedDate = it.joiningDate
                updateDateLabel()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate
        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDate = calendar.timeInMillis
                updateDateLabel()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDateLabel() {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        binding.etJoiningDate.setText(sdf.format(Date(selectedDate)))
    }

    private fun saveCustomer() {
        val name = binding.etName.text.toString().trim()
        val mobile = binding.etMobile.text.toString().trim()
        val feeStr = binding.etFee.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        if (name.isEmpty() || mobile.isEmpty() || feeStr.isEmpty()) {
            Toast.makeText(requireContext(), R.string.fill_required, Toast.LENGTH_SHORT).show()
            return
        }

        val fee = feeStr.toDoubleOrNull() ?: 0.0

        if (existingCustomer != null) {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDate
            calendar.add(Calendar.MONTH, 1)
            val nextDueDate = calendar.timeInMillis

            val updatedCustomer = existingCustomer!!.copy(
                name = name,
                mobileNumber = mobile,
                joiningDate = selectedDate,
                monthlyFee = fee,
                nextDueDate = nextDueDate,
                notes = notes
            )
            viewModel.updateCustomer(updatedCustomer)
            Toast.makeText(requireContext(), R.string.customer_updated, Toast.LENGTH_SHORT).show()
        } else {
            viewModel.insertCustomer(name, mobile, selectedDate, fee, notes)
            Toast.makeText(requireContext(), R.string.customer_added, Toast.LENGTH_SHORT).show()
        }
        findNavController().navigateUp()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
