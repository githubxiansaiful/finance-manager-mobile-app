package com.financemanager.app.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class Category(
    val displayName: String,
    val defaultType: TransactionType,
    val color: Long
) {
    // Expense Categories
    FOOD("Food & Dining", TransactionType.EXPENSE, 0xFFD97706),
    SHOPPING("Shopping", TransactionType.EXPENSE, 0xFF7C3AED),
    HOUSING("Housing & Rent", TransactionType.EXPENSE, 0xFF4F46E5),
    TRANSPORT("Transportation", TransactionType.EXPENSE, 0xFF0284C7),
    ENTERTAINMENT("Entertainment", TransactionType.EXPENSE, 0xFFE11D48),
    HEALTH("Health & Medical", TransactionType.EXPENSE, 0xFFDC2626),
    EDUCATION("Education", TransactionType.EXPENSE, 0xFF2563EB),
    BILLS("Bills & Utilities", TransactionType.EXPENSE, 0xFFCA8A04),

    // Income Categories
    SALARY("Salary", TransactionType.INCOME, 0xFF059669),
    INVESTMENT("Investments", TransactionType.INCOME, 0xFF0D9488),
    FREELANCE("Freelance", TransactionType.INCOME, 0xFF10B981),
    GIFT("Gift & Bonus", TransactionType.INCOME, 0xFF9333EA),

    // General / Other
    OTHER("Other", TransactionType.EXPENSE, 0xFF64748B);

    val composeColor: Color
        get() = Color(color)

    val icon: ImageVector
        get() = when (this) {
            FOOD -> Icons.Default.Restaurant
            SHOPPING -> Icons.Default.ShoppingBag
            HOUSING -> Icons.Default.Home
            TRANSPORT -> Icons.Default.DirectionsCar
            ENTERTAINMENT -> Icons.Default.Movie
            HEALTH -> Icons.Default.LocalHospital
            EDUCATION -> Icons.Default.School
            BILLS -> Icons.Default.Receipt
            SALARY -> Icons.Default.Payments
            INVESTMENT -> Icons.AutoMirrored.Filled.TrendingUp
            FREELANCE -> Icons.Default.LaptopMac
            GIFT -> Icons.Default.CardGiftcard
            OTHER -> Icons.Default.Category
        }

    companion object {
        fun expenseCategories(): List<Category> =
            entries.filter { it.defaultType == TransactionType.EXPENSE || it == OTHER }

        fun incomeCategories(): List<Category> =
            entries.filter { it.defaultType == TransactionType.INCOME || it == OTHER }
    }
}
