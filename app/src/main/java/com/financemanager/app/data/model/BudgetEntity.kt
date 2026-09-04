package com.financemanager.app.data.model

import androidx.room.Entity

@Entity(
    tableName = "budgets",
    primaryKeys = ["userId", "category"]
)
data class BudgetEntity(
    val userId: String,
    val category: Category,
    val monthlyLimit: Double
)
