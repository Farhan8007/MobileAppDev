package com.example.gymfees.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gymfees.data.Payment
import com.example.gymfees.databinding.ItemPaymentBinding
import java.text.SimpleDateFormat
import java.util.*

class PaymentAdapter : ListAdapter<Payment, PaymentAdapter.PaymentViewHolder>(PaymentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val binding = ItemPaymentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PaymentViewHolder(private val binding: ItemPaymentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(payment: Payment) {
            val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val dateSdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            
            binding.tvPaymentDate.text = sdf.format(Date(payment.dueDate))
            binding.tvPaymentStatus.text = "Paid on ${dateSdf.format(Date(payment.paymentDate))}"
            binding.tvPaymentAmount.text = "₹${payment.amountPaid}"
        }
    }

    class PaymentDiffCallback : DiffUtil.ItemCallback<Payment>() {
        override fun areItemsTheSame(oldItem: Payment, newItem: Payment): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Payment, newItem: Payment): Boolean = oldItem == newItem
    }
}
