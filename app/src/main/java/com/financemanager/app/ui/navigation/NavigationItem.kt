package com.financemanager.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen(val title: String, val icon: ImageVector) {
    DASHBOARD("Home", Icons.Default.Home),
    ACCOUNTS("Accounts", Icons.Default.AccountBalance),
    TRANSACTIONS("Transactions", Icons.AutoMirrored.Filled.ReceiptLong),
    BUDGETS("Budgets", Icons.Default.AccountBalanceWallet),
    ANALYTICS("Analytics", Icons.Default.Analytics),
    CATEGORIES("Category Builder", Icons.Default.Category),
    SETTINGS("Settings", Icons.Default.Settings)
}
