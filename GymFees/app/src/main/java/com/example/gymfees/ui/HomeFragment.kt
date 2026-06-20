package com.example.gymfees.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gymfees.R
import com.example.gymfees.data.Customer
import com.example.gymfees.databinding.FragmentHomeBinding
import com.example.gymfees.viewmodel.GymViewModel
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GymViewModel by viewModels()
    private lateinit var adapter: CustomerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupMenu()
        setupRecyclerView()
        setupDashboard()
        setupSearch()
        setupFilters()
        setupSnackbarAndUndo()

        binding.fabAdd.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToAddEditCustomerFragment(getString(R.string.add_customer), -1L)
            findNavController().navigate(action)
        }
    }

    private fun setupMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_home, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_settings -> {
                        findNavController().navigate(R.id.action_homeFragment_to_settingsFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupRecyclerView() {
        adapter = CustomerAdapter(
            onTogglePaymentStatusClick = { customer ->
                if (customer.isCurrentMonthFeePaid) {
                    showMarkUnpaidConfirmation(customer)
                } else {
                    showMarkPaidConfirmation(customer)
                }
            },
            onSMSClick = { customer -> sendSMS(customer) },
            onEditClick = { customer ->
                val action = HomeFragmentDirections.actionHomeFragmentToAddEditCustomerFragment(getString(R.string.edit_customer), customer.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { customer -> showDeleteConfirmation(customer) },
            onItemClick = { customer ->
                val action = HomeFragmentDirections.actionHomeFragmentToCustomerDetailsFragment(customer.id)
                findNavController().navigate(action)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.filteredCustomers.observe(viewLifecycleOwner) { customers ->
            adapter.submitList(customers)
            binding.emptyState.visibility = if (customers.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun setupDashboard() {
        viewModel.customerCount.observe(viewLifecycleOwner) { count ->
            binding.tvTotalCustomers.text = getString(R.string.total_customers, count)
        }
        viewModel.paidCount.observe(viewLifecycleOwner) { count ->
            binding.tvPaidCustomers.text = getString(R.string.paid_customers, count)
        }
        viewModel.dueSoonCount.observe(viewLifecycleOwner) { count ->
            binding.tvDueSoonCustomers.text = getString(R.string.due_soon, count)
        }
        viewModel.overdueCount.observe(viewLifecycleOwner) { count ->
            binding.tvOverdueCustomers.text = getString(R.string.overdue, count)
        }
        viewModel.monthCollection.observe(viewLifecycleOwner) { amount ->
            binding.tvMonthCollection.text = getString(R.string.month_collection_label, amount ?: 0.0)
        }
        viewModel.totalCollection.observe(viewLifecycleOwner) { amount ->
            binding.tvTotalCollection.text = getString(R.string.total_collection_label, amount ?: 0.0)
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.setSearchQuery(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFilters() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            val status = when (checkedIds.firstOrNull()) {
                R.id.chipPaid -> "PAID"
                R.id.chipDue -> "DUE_SOON"
                R.id.chipOverdue -> "OVERDUE"
                R.id.chipPending -> "PENDING"
                else -> "ALL"
            }
            viewModel.setFilterStatus(status)
        }
    }

    private fun setupSnackbarAndUndo() {
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
                    }
                    .show()
                viewModel.clearLastAction()
            }
        }
    }

    private fun sendSMS(customer: Customer) {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val dueDateStr = sdf.format(Date(customer.nextDueDate))
        
        val prefs = requireContext().getSharedPreferences("gym_prefs", Context.MODE_PRIVATE)
        val template = prefs.getString("sms_template", getString(R.string.default_sms_template)) ?: getString(R.string.default_sms_template)

        val message = template.replace("{NAME}", customer.name)
                             .replace("{FEE}", customer.monthlyFee.toString())
                             .replace("{DUEDATE}", dueDateStr)
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${customer.mobileNumber}")
            putExtra("sms_body", message)
        }
        startActivity(intent)
    }

    private fun showDeleteConfirmation(customer: Customer) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_confirmation_title)
            .setMessage(getString(R.string.delete_confirmation_msg_customer, customer.name))
            .setPositiveButton(R.string.action_delete) { _, _ ->
                viewModel.deleteCustomer(customer)
                Snackbar.make(binding.root, getString(R.string.customer_deleted, customer.name), Snackbar.LENGTH_LONG).show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
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
