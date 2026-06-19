package com.example.gymfees.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mobileNumber: String,
    val joiningDate: Long,
    val monthlyFee: Double,
    val nextDueDate: Long,
    val notes: String? = null,
    val status: String = "PAID" // PAID, DUE_SOON, OVERDUE
)
