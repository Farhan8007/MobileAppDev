package com.example.gymfees.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): LiveData<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("SELECT * FROM customers WHERE name LIKE '%' || :searchQuery || '%' OR mobileNumber LIKE '%' || :searchQuery || '%'")
    fun searchCustomers(searchQuery: String): LiveData<List<Customer>>

    @Query("SELECT COUNT(*) FROM customers")
    fun getCustomerCount(): LiveData<Int>

    // Dynamic counts based on date
    @Query("SELECT COUNT(*) FROM customers WHERE nextDueDate > :today + 259200000") // More than 3 days away
    fun getPaidCount(today: Long): LiveData<Int>

    @Query("SELECT COUNT(*) FROM customers WHERE nextDueDate >= :today AND nextDueDate <= :today + 259200000") // within 3 days
    fun getDueSoonCount(today: Long): LiveData<Int>

    @Query("SELECT COUNT(*) FROM customers WHERE nextDueDate < :today")
    fun getOverdueCount(today: Long): LiveData<Int>

    @Query("SELECT SUM(monthlyFee) FROM customers")
    fun getTotalMonthlyCollection(): LiveData<Double>

    // For WorkManager
    @Query("SELECT COUNT(*) FROM customers WHERE nextDueDate >= :startOfDay AND nextDueDate <= :endOfDay")
    fun getDueTodayCountSync(startOfDay: Long, endOfDay: Long): Int

    @Query("SELECT COUNT(*) FROM customers WHERE nextDueDate < :today")
    fun getOverdueCountSync(today: Long): Int
}
