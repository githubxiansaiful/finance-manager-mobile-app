package com.financemanager.app.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class AccountType(val displayName: String) {
    MOBILE_BANKING("Mobile Banking"),
    BANK("Bank Account"),
    CASH("Cash / Wallet")
}

enum class FinancialInstitution(
    val displayName: String,
    val shortName: String,
    val defaultType: AccountType,
    val brandColorHex: Long,
    val description: String
) {
    // Mobile Banking (Bangladesh)
    BKASH("bKash", "bKash", AccountType.MOBILE_BANKING, 0xFFE2136E, "bKash Limited Mobile Financial Service"),
    NAGAD("Nagad", "Nagad", AccountType.MOBILE_BANKING, 0xFFF7941D, "Nagad Mobile Financial Service"),
    ROCKET("Rocket", "Rocket", AccountType.MOBILE_BANKING, 0xFF8C3494, "Dutch-Bangla Bank Rocket MFS"),

    // Popular Banks (Bangladesh)
    DUTCH_BANGLA("Dutch Bangla Bank", "DBBL", AccountType.BANK, 0xFF008037, "Dutch-Bangla Bank PLC"),
    PUBALI_BANK("Pubali Bank", "Pubali", AccountType.BANK, 0xFF1C449C, "Pubali Bank PLC"),
    ISLAMI_BANK("Islami Bank", "IBBL", AccountType.BANK, 0xFF006837, "Islami Bank Bangladesh PLC"),
    DHAKA_BANK("Dhaka Bank", "Dhaka Bank", AccountType.BANK, 0xFFD31245, "Dhaka Bank PLC"),
    BANK_ASIA("Bank Asia", "Bank Asia", AccountType.BANK, 0xFF0054A6, "Bank Asia PLC"),

    // Cash & Other
    CASH("Physical Cash", "Cash", AccountType.CASH, 0xFF10B981, "Cash in Wallet / On Hand"),
    OTHER("Other Account", "Other", AccountType.BANK, 0xFF607D8B, "General Account");

    val brandColor: Color
        get() = Color(brandColorHex)
}

@Entity(
    tableName = "accounts",
    indices = [Index(value = ["userId"])]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val name: String,
    val type: AccountType,
    val institution: FinancialInstitution,
    val accountNumber: String = "",
    val balance: Double = 0.0,
    val colorHex: Long = 0xFF10B981,
    val isDefault: Boolean = false,
    val note: String = ""
)
