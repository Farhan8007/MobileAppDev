package com.example.gymfees.repository

import androidx.lifecycle.LiveData
import androidx.room.withTransaction
import com.example.gymfees.data.Customer
import com.example.gymfees.data.GymDatabase
import com.example.gymfees.data.Payment

class GymRepository(private val database: GymDatabase) {

    private val customerDao = database.customerDao()
    private val paymentDao = database.paymentDao()

    val allCustomers: LiveData<List<Customer>> = customerDao.getAllCustomers()
    val customerCount: LiveData<Int> = customerDao.getCustomerCount()
    val paidCustomerCount: LiveData<Int> = customerDao.getPaidCustomerCount()

    suspend fun insertCustomer(customer: Customer): Long = customerDao.insertCustomer(customer)

    suspend fun updateCustomer(customer: Customer) = customerDao.updateCustomer(customer)

    suspend fun deleteCustomer(customer: Customer) = customerDao.deleteCustomer(customer)

    suspend fun getCustomerById(id: Long): Customer? = customerDao.getCustomerById(id)

    fun getCustomerByIdLiveData(id: Long): LiveData<Customer?> = customerDao.getCustomerByIdLiveData(id)

    fun getPaymentsForCustomer(customerId: Long): LiveData<List<Payment>> = paymentDao.getPaymentsForCustomer(customerId)

    suspend fun insertPayment(payment: Payment) = paymentDao.insertPayment(payment)

    suspend fun deletePayment(payment: Payment) = paymentDao.deletePayment(payment)

    suspend fun getLatestPaymentForCustomer(customerId: Long): Payment? = paymentDao.getLatestPaymentForCustomer(customerId)

    suspend fun deletePaymentsByCustomerId(customerId: Long) = paymentDao.deletePaymentsByCustomerId(customerId)

    fun getTotalCollectionForMonth(startOfMonth: Long): LiveData<Double?> = paymentDao.getTotalCollectionForMonth(startOfMonth)

    fun getTotalCollection(): LiveData<Double?> = paymentDao.getTotalCollection()

    fun getDueSoonCount(today: Long): LiveData<Int> = customerDao.getDueSoonCount(today)

    fun getOverdueCount(today: Long): LiveData<Int> = customerDao.getOverdueCount(today)

    /**
     * Requirement 10: Ensure Room Database records remain consistent using transactions.
     */
    suspend fun markPaidTransaction(customer: Customer, payment: Payment, updatedCustomer: Customer) {
        database.withTransaction {
            paymentDao.insertPayment(payment)
            customerDao.updateCustomer(updatedCustomer)
        }
    }

    suspend fun markUnpaidTransaction(payment: Payment, updatedCustomer: Customer) {
        database.withTransaction {
            paymentDao.deletePayment(payment)
            customerDao.updateCustomer(updatedCustomer)
        }
    }

    suspend fun undoActionTransaction(
        customerToRevert: Customer,
        paymentToDelete: Payment?,
        paymentToRestore: Payment?
    ) {
        database.withTransaction {
            customerDao.updateCustomer(customerToRevert)
            paymentToDelete?.let { paymentDao.deletePayment(it) }
            paymentToRestore?.let { paymentDao.insertPayment(it) }
        }
    }
}
