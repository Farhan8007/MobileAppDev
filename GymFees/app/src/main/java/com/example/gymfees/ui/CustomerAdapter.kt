package com.example.gymfees.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymfees.data.Customer
import com.example.gymfees.databinding.ItemCustomerBinding
import java.text.SimpleDateFormat
import java.util.*

class CustomerAdapter(
    private val onPaidClick: (Customer) -> Unit,
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
            binding.tvCustomerName.text = customer.name
            binding.tvMobile.text = customer.mobileNumber
            binding.tvFee.text = "Fee: ₹${customer.monthlyFee}"
            
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.tvDueDate.text = "Next Due: ${sdf.format(Date(customer.nextDueDate))}"
            
            val today = Calendar.getInstance().timeInMillis
            val threeDaysMillis = 3 * 24 * 60 * 60 * 1000L
            
            val (statusText, statusColor) = when {
                customer.nextDueDate < today -> "OVERDUE" to "#F44336" // Red
                customer.nextDueDate <= today + threeDaysMillis -> "DUE SOON" to "#FBC02D" // Yellow/Amber
                else -> "PAID" to "#4CAF50" // Green
            }
            
            binding.statusBadge.text = statusText
            binding.statusBadge.setBackgroundColor(Color.parseColor(statusColor))

            binding.btnPaid.setOnClickListener { onPaidClick(customer) }
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
