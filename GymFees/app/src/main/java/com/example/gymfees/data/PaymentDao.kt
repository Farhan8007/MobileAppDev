package com.example.gymfees.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PaymentDao {
    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY paymentDate DESC")
    fun getPaymentsForCustomer(customerId: Long): LiveData<List<Payment>>

    @Query("SELECT * FROM payments WHERE customerId = :customerId ORDER BY paymentDate DESC LIMIT 1")
    suspend fun getLatestPaymentForCustomer(customerId: Long): Payment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment)

    @Delete
    suspend fun deletePayment(payment: Payment) // Added for deleting a specific payment

    @Query("DELETE FROM payments WHERE customerId = :customerId")
    suspend fun deletePaymentsByCustomerId(customerId: Long)

    @Query("SELECT SUM(amountPaid) FROM payments WHERE paymentDate >= :startOfMonth")
    fun getTotalCollectionForMonth(startOfMonth: Long): LiveData<Double?>

    @Query("SELECT SUM(amountPaid) FROM payments")
    fun getTotalCollection(): LiveData<Double?>
}
