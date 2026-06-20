package com.example.gymfees.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymfees.R
import com.example.gymfees.data.Customer
import com.example.gymfees.databinding.ItemCustomerBinding
import java.text.SimpleDateFormat
import java.util.*

class CustomerAdapter(
    private val onTogglePaymentStatusClick: (Customer) -> Unit,
    private val onSMSClick: (Customer) -> Unit,
    private val onEditClick: (Customer) -> Unit,
    private val onDeleteClick: (Customer) -> Unit,
    private val onItemClick: (Customer) -> Unit
) : ListAdapter<Customer, CustomerAdapter.CustomerViewHolder>(CustomerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val binding = ItemCustomerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CustomerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CustomerViewHolder(private val binding: ItemCustomerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(customer: Customer) {
            val context = binding.root.context
            binding.tvCustomerName.text = customer.name
            binding.tvMobile.text = customer.mobileNumber
            binding.tvFee.text = "Fee: ₹${customer.monthlyFee}"
            
            val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
            binding.tvDueDate.text = "(Due: ${sdf.format(Date(customer.nextDueDate))})"
            
            val today = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
            val threeDaysMillis = 3 * 24 * 60 * 60 * 1000L
            
            val (statusText, statusColor) = when {
                customer.isCurrentMonthFeePaid -> context.getString(R.string.status_paid) to "#4CAF50" // Green
                customer.nextDueDate < today -> context.getString(R.string.status_overdue) to "#F44336" // Red
                customer.nextDueDate <= today + threeDaysMillis -> context.getString(R.string.status_due_soon) to "#FBC02D" // Yellow/Amber
                else -> context.getString(R.string.status_pending) to "#2196F3" // Blue
            }
            
            binding.statusBadge.text = statusText
            binding.statusBadge.setBackgroundColor(Color.parseColor(statusColor))

            // Set button text and click listener based on payment status
            if (customer.isCurrentMonthFeePaid) {
                binding.btnTogglePaymentStatus.text = context.getString(R.string.action_mark_unpaid)
            } else {
                binding.btnTogglePaymentStatus.text = context.getString(R.string.action_mark_paid)
            }
            binding.btnTogglePaymentStatus.setOnClickListener { onTogglePaymentStatusClick(customer) }

            binding.btnSMS.setOnClickListener { onSMSClick(customer) }
            binding.btnEdit.setOnClickListener { onEditClick(customer) }
            binding.btnDelete.setOnClickListener { onDeleteClick(customer) }
            binding.root.setOnClickListener { onItemClick(customer) }
        }
    }

    class CustomerDiffCallback : DiffUtil.ItemCallback<Customer>() {
        override fun areItemsTheSame(oldItem: Customer, newItem: Customer): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Customer, newItem: Customer): Boolean = oldItem == newItem
    }
}
