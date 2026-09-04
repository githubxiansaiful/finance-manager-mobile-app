package com.financemanager.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Calendar

enum class RecurrenceInterval(val displayName: String) {
    DAILY("Daily"),
    WEEKLY("Weekly"),
    MONTHLY("Monthly"),
    YEARLY("Yearly");

    fun getNextDueDate(fromMillis: Long): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = fromMillis }
        when (this) {
            DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
            WEEKLY -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            MONTHLY -> cal.add(Calendar.MONTH, 1)
            YEARLY -> cal.add(Calendar.YEAR, 1)
        }
        return cal.timeInMillis
    }
}

@Entity(
    tableName = "recurring_transactions",
    indices = [Index(value = ["userId"])]
)
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val paymentMethod: PaymentMethod,
    val interval: RecurrenceInterval,
    val nextDueDateMillis: Long,
    val lastProcessedDateMillis: Long = 0,
    val isActive: Boolean = true,
    val note: String = ""
)
