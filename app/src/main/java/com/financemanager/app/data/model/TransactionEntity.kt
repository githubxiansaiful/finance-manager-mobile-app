package com.financemanager.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["userId"])]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val paymentMethod: PaymentMethod,
    val dateMillis: Long,
    val note: String = "",
    val accountId: Long? = null,
    val accountName: String? = null
)
