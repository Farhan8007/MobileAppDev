package com.example.gymfees.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val amountPaid: Double,
    val paymentDate: Long,
    val dueDate: Long
)
