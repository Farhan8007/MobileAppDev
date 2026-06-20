package com.example.gymfees.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymfees.R
import com.example.gymfees.data.Customer
import com.example.gymfees.data.GymDatabase
import com.example.gymfees.databinding.FragmentCustomerDetailsBinding
import com.example.gymfees.viewmodel.GymViewModel
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CustomerDetailsFragment : Fragment() {

    private var _binding: FragmentCustomerDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GymViewModel by viewModels()
    private val args: CustomerDetailsFragmentArgs by navArgs()
    private lateinit var paymentAdapter: PaymentAdapter
    private var currentCustomer: Customer? = null

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
        setupObservers()
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

    private fun setupObservers() {
        viewModel.snackbarMessage.observe(viewLifecycleOwner) { message ->
            message?.let { 
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                viewModel.snackbarMessageShown()
            }
        }

        viewModel.lastAction.observe(viewLifecycleOwner) { lastAction ->
            lastAction?.let {
                val messageRes = when (it) {
                    is GymViewModel.LastAction.MarkPaid -> R.string.fee_marked_paid
                    is GymViewModel.LastAction.MarkUnpaid -> R.string.fee_marked_unpaid
                }
                Snackbar.make(binding.root, getString(messageRes), Snackbar.LENGTH_LONG)
                    .setAction(R.string.action_undo) {
                        viewModel.undoLastAction()
                        loadCustomerData() // Refresh UI after undo
                    }
                    .show()
                loadCustomerData() // Refresh UI after action
            }
        }
    }

    private fun loadCustomerData() {
        lifecycleScope.launch {
            val database = GymDatabase.getDatabase(requireContext())
            val customer = database.customerDao().getCustomerById(args.customerId)
            customer?.let {
                currentCustomer = it
                updateUI(it)
            }
        }
    }

    private fun updateUI(customer: Customer) {
        binding.tvDetailName.text = customer.name
        binding.tvDetailMobile.text = customer.mobileNumber
        binding.tvDetailFee.text = getString(R.string.detail_fee, customer.monthlyFee)
        
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        binding.tvDetailJoiningDate.text = getString(R.string.detail_joined, sdf.format(Date(customer.joiningDate)))
        binding.tvDetailNotes.text = if (customer.notes.isNullOrBlank()) "" else getString(R.string.detail_notes, customer.notes)

        // Update Toggle Button
        if (customer.isCurrentMonthFeePaid) {
            binding.btnDetailTogglePayment.text = getString(R.string.action_mark_unpaid)
            binding.btnDetailTogglePayment.setOnClickListener { showMarkUnpaidConfirmation(customer) }
        } else {
            binding.btnDetailTogglePayment.text = getString(R.string.action_mark_paid)
            binding.btnDetailTogglePayment.setOnClickListener { showMarkPaidConfirmation(customer) }
        }
    }

    private fun showMarkPaidConfirmation(customer: Customer) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_payment_title)
            .setMessage(getString(R.string.confirm_mark_paid_msg, customer.monthlyFee, customer.name))
            .setPositiveButton(R.string.action_confirm) { _, _ ->
                viewModel.markCustomerPaid(customer)
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    private fun showMarkUnpaidConfirmation(customer: Customer) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.confirm_revert_title)
            .setMessage(getString(R.string.confirm_mark_unpaid_msg, customer.monthlyFee, customer.name))
            .setPositiveButton(R.string.action_confirm) { _, _ ->
                viewModel.markCustomerUnpaid(customer)
            }
            .setNegativeButton(R.string.action_cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
