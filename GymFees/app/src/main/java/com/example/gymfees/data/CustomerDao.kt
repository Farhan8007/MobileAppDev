package com.example.gymfees.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CustomerDao {
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): LiveData<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): Customer?

    @Query("SELECT * FROM customers WHERE id = :id")
    fun getCustomerByIdLiveData(id: Long): LiveData<Customer?>

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

    @Query("SELECT COUNT(*) FROM customers WHERE isCurrentMonthFeePaid = 1")
    fun getPaidCustomerCount(): LiveData<Int>

    @Query("SELECT COUNT(*) FROM customers WHERE isCurrentMonthFeePaid = 0 AND nextDueDate >= :today AND nextDueDate <= :today + 259200000") // within 3 days
    fun getDueSoonCount(today: Long): LiveData<Int>

    @Query("SELECT COUNT(*) FROM customers WHERE isCurrentMonthFeePaid = 0 AND nextDueDate < :today")
    fun getOverdueCount(today: Long): LiveData<Int>

    // For WorkManager
    @Query("SELECT COUNT(*) FROM customers WHERE isCurrentMonthFeePaid = 0 AND nextDueDate >= :startOfDay AND nextDueDate <= :endOfDay")
    fun getDueTodayCountSync(startOfDay: Long, endOfDay: Long): Int

    @Query("SELECT COUNT(*) FROM customers WHERE isCurrentMonthFeePaid = 0 AND nextDueDate < :today")
    fun getOverdueCountSync(today: Long): Int
}
