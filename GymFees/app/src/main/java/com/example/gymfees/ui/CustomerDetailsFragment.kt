package com.example.gymfees.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymfees.data.GymDatabase
import com.example.gymfees.databinding.FragmentCustomerDetailsBinding
import com.example.gymfees.viewmodel.GymViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CustomerDetailsFragment : Fragment() {

    private var _binding: FragmentCustomerDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GymViewModel by viewModels()
    private val args: CustomerDetailsFragmentArgs by navArgs()
    private lateinit var paymentAdapter: PaymentAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomerDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        setupRecyclerView()
        loadCustomerData()
    }

    private fun setupRecyclerView() {
        paymentAdapter = PaymentAdapter()
        binding.rvPaymentHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPaymentHistory.adapter = paymentAdapter

        viewModel.getPaymentsForCustomer(args.customerId).observe(viewLifecycleOwner) { payments ->
            paymentAdapter.submitList(payments)
            binding.tvNoPayments.visibility = if (payments.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun loadCustomerData() {
        lifecycleScope.launch {
            val database = GymDatabase.getDatabase(requireContext())
            val customer = database.customerDao().getCustomerById(args.customerId)
            customer?.let {
                binding.tvDetailName.text = it.name
                binding.tvDetailMobile.text = it.mobileNumber
                binding.tvDetailFee.text = "Monthly Fee: ₹${it.monthlyFee}"
                
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                binding.tvDetailJoiningDate.text = "Joined: ${sdf.format(Date(it.joiningDate))}"
                binding.tvDetailNotes.text = if (it.notes.isNullOrBlank()) "" else "Notes: ${it.notes}"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
