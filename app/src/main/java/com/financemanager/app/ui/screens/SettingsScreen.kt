package com.financemanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financemanager.app.data.local.AppThemeMode
import com.financemanager.app.ui.components.CurrencySelectionDialog
import com.financemanager.app.ui.components.ExportDialog
import com.financemanager.app.ui.viewmodel.FinanceViewModel

@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel,
    onNavigateToAccounts: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val currency by viewModel.currentCurrency.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showCustomCategorySheet by remember { mutableStateOf(false) }
    var showRecurringSheet by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // 1. APPEARANCE (Dark / Light Mode)
        item {
            SettingsSectionHeader(title = "Appearance")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "App Theme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Choose your preferred theme mode. Light and dark modes update instantaneously.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ThemeOptionButton(
                            title = "System",
                            icon = Icons.Default.BrightnessAuto,
                            isSelected = themeMode == AppThemeMode.SYSTEM,
                            onClick = { viewModel.setThemeMode(AppThemeMode.SYSTEM) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionButton(
                            title = "Light",
                            icon = Icons.Default.LightMode,
                            isSelected = themeMode == AppThemeMode.LIGHT,
                            onClick = { viewModel.setThemeMode(AppThemeMode.LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionButton(
                            title = "Dark",
                            icon = Icons.Default.DarkMode,
                            isSelected = themeMode == AppThemeMode.DARK,
                            onClick = { viewModel.setThemeMode(AppThemeMode.DARK) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 2. PREFERENCES & CURRENCY
        item {
            SettingsSectionHeader(title = "Preferences")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SettingsItemRow(
                        icon = Icons.Default.MonetizationOn,
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Currency",
                        subtitle = "${currency.code} (${currency.symbol}) - ${currency.displayName}",
                        onClick = { showCurrencyDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    SettingsItemRow(
                        icon = Icons.Default.AccountBalance,
                        iconTint = Color(0xFF006837),
                        title = "Manage Accounts",
                        subtitle = "${accounts.size} active accounts (bKash, Banks, Cash)",
                        onClick = onNavigateToAccounts
                    )
                }
            }
        }

        // 3. TOOLS & EXPEDITION
        item {
            SettingsSectionHeader(title = "Financial Tools")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    SettingsItemRow(
                        icon = Icons.Default.Repeat,
                        iconTint = Color(0xFF8C3494),
                        title = "Recurring Transactions",
                        subtitle = "Automate bills, salary, subscriptions",
                        onClick = { showRecurringSheet = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    SettingsItemRow(
                        icon = Icons.Default.Category,
                        iconTint = Color(0xFFF37023),
                        title = "Custom Category Builder",
                        subtitle = "Add personalized categories with custom colors",
                        onClick = { showCustomCategorySheet = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    SettingsItemRow(
                        icon = Icons.Default.FileDownload,
                        iconTint = MaterialTheme.colorScheme.secondary,
                        title = "Export Data (CSV / PDF)",
                        subtitle = "Download statements or spreadsheet reports",
                        onClick = { showExportDialog = true }
                    )
                }
            }
        }

        // 4. CLOUD SYNC & ACCOUNT
        item {
            SettingsSectionHeader(title = "Cloud & Security")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Firebase Cloud Sync",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Connected as ${currentUser?.email ?: "Guest"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF10B981)))
                                Text(
                                    text = "Synced",
                                    color = Color(0xFF10B981),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Database Security", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("AES-256 Cloud & Local Room encryption", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Application Version", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("v2.5.0 Production Release", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text("Build 2026.09", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        // 5. SIGN OUT
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showSignOutConfirm = true },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Sign Out",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sign Out",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Log out from ${currentUser?.email ?: "account"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    // Dialogs
    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            currentCurrency = currency,
            onCurrencySelected = { selected ->
                viewModel.setCurrency(selected)
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }

    if (showExportDialog) {
        ExportDialog(
            transactionCount = 0,
            currency = currency,
            onExportCsv = {
                viewModel.exportToCsv(context)
                showExportDialog = false
            },
            onExportPdf = {
                viewModel.exportToPdf(context)
                showExportDialog = false
            },
            onDismiss = { showExportDialog = false }
        )
    }

    if (showCustomCategorySheet) {
        CustomCategorySheet(
            viewModel = viewModel,
            onDismiss = { showCustomCategorySheet = false }
        )
    }

    if (showRecurringSheet) {
        RecurringTransactionsSheet(
            viewModel = viewModel,
            onDismiss = { showRecurringSheet = false }
        )
    }

    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text("Sign Out Confirmation") },
            text = { Text("Are you sure you want to sign out? Your financial data will remain securely saved in Firebase Cloud.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirm = false
                        viewModel.signOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSignOutConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp, top = 6.dp)
    )
}

@Composable
private fun SettingsItemRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun ThemeOptionButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(18.dp),
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
