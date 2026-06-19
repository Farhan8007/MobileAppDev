package com.example.gymfees.repository

import androidx.lifecycle.LiveData
import com.example.gymfees.data.Customer
import com.example.gymfees.data.CustomerDao
import com.example.gymfees.data.Payment
import com.example.gymfees.data.PaymentDao

class GymRepository(private val customerDao: CustomerDao, private val paymentDao: PaymentDao) {

    val allCustomers: LiveData<List<Customer>> = customerDao.getAllCustomers()
    val customerCount: LiveData<Int> = customerDao.getCustomerCount()

    suspend fun insertCustomer(customer: Customer): Long {
        return customerDao.insertCustomer(customer)
    }

    suspend fun updateCustomer(customer: Customer) {
        customerDao.updateCustomer(customer)
    }

    suspend fun deleteCustomer(customer: Customer) {
        customerDao.deleteCustomer(customer)
    }

    fun searchCustomers(query: String): LiveData<List<Customer>> {
        return customerDao.searchCustomers(query)
    }

    suspend fun getCustomerById(id: Long): Customer? {
        return customerDao.getCustomerById(id)
    }

    fun getPaymentsForCustomer(customerId: Long): LiveData<List<Payment>> {
        return paymentDao.getPaymentsForCustomer(customerId)
    }

    suspend fun insertPayment(payment: Payment) {
        paymentDao.insertPayment(payment)
    }

    fun getTotalCollectionForMonth(startOfMonth: Long): LiveData<Double> {
        return paymentDao.getTotalCollectionForMonth(startOfMonth)
    }
}
