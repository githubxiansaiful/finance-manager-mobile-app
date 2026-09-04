package com.financemanager.app.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.ui.graphics.vector.ImageVector

enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    BANK_TRANSFER("Bank Transfer"),
    MOBILE_WALLET("Mobile Wallet");

    val icon: ImageVector
        get() = when (this) {
            CASH -> Icons.Default.Money
            CREDIT_CARD -> Icons.Default.CreditCard
            DEBIT_CARD -> Icons.Default.Payments
            BANK_TRANSFER -> Icons.Default.AccountBalance
            MOBILE_WALLET -> Icons.Default.PhoneAndroid
        }
}
