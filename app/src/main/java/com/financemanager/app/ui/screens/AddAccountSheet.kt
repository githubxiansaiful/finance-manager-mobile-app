package com.financemanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.financemanager.app.data.model.AccountType
import com.financemanager.app.data.model.FinancialInstitution
import com.financemanager.app.ui.components.InstitutionLogo
import com.financemanager.app.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountSheet(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val currency by viewModel.currentCurrency.collectAsState()

    var selectedInstitution by remember { mutableStateOf(FinancialInstitution.BKASH) }
    var accountName by remember { mutableStateOf("bKash Personal") }
    var accountNumber by remember { mutableStateOf("") }
    var balanceText by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun onSelectInstitution(institution: FinancialInstitution) {
        selectedInstitution = institution
        accountName = when (institution) {
            FinancialInstitution.BKASH -> "bKash Personal"
            FinancialInstitution.NAGAD -> "Nagad Personal"
            FinancialInstitution.ROCKET -> "Rocket Account"
            FinancialInstitution.DUTCH_BANGLA -> "DBBL Account"
            FinancialInstitution.PUBALI_BANK -> "Pubali Bank"
            FinancialInstitution.ISLAMI_BANK -> "Islami Bank"
            FinancialInstitution.DHAKA_BANK -> "Dhaka Bank"
            FinancialInstitution.BANK_ASIA -> "Bank Asia"
            FinancialInstitution.CASH -> "Cash Wallet"
            FinancialInstitution.OTHER -> "General Account"
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Add Financial Account",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Track Mobile Banking, Banks & Cash",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            // Institution Selector with Bangladeshi Brands & Authentic Logos
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select Institution / Service",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(FinancialInstitution.entries) { inst ->
                        val isSelected = inst == selectedInstitution
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier.clickable { onSelectInstitution(inst) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                                    .width(76.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                InstitutionLogo(institution = inst, size = 36.dp)
                                Text(
                                    text = inst.displayName.substringBefore(" ("),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Account Name
            OutlinedTextField(
                value = accountName,
                onValueChange = { accountName = it },
                label = { Text("Account Name") },
                placeholder = { Text("e.g., Salary Account, Personal bKash") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Account Number / Phone (Hide if Cash)
            if (selectedInstitution != FinancialInstitution.CASH) {
                OutlinedTextField(
                    value = accountNumber,
                    onValueChange = { accountNumber = it },
                    label = {
                        Text(
                            if (selectedInstitution.defaultType == AccountType.MOBILE_BANKING) "Mobile Number (e.g. 017xxxxxxxx)"
                            else "Account Number"
                        )
                    },
                    placeholder = {
                        Text(
                            if (selectedInstitution.defaultType == AccountType.MOBILE_BANKING) "01XXXXXXXXX"
                            else "e.g. 105.120.9845"
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Initial Balance
            OutlinedTextField(
                value = balanceText,
                onValueChange = { balanceText = it },
                label = { Text("Initial Balance (${currency.symbol})") },
                placeholder = { Text("0.00") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Make Default Account Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Set as Primary Account",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Used as default for new transactions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isDefault,
                    onCheckedChange = { isDefault = it }
                )
            }

            // Note (Optional)
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                placeholder = { Text("e.g., Main salary deposit account") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Submit Button
            Button(
                onClick = {
                    if (accountName.isBlank()) {
                        errorMessage = "Please enter an account name."
                        return@Button
                    }
                    val balance = balanceText.toDoubleOrNull() ?: 0.0
                    viewModel.addAccount(
                        name = accountName,
                        type = selectedInstitution.defaultType,
                        institution = selectedInstitution,
                        accountNumber = accountNumber,
                        initialBalance = balance,
                        colorHex = selectedInstitution.brandColorHex,
                        isDefault = isDefault,
                        note = note
                    )
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Save Account",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
