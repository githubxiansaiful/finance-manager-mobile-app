package com.financemanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financemanager.app.data.model.AccountType
import com.financemanager.app.data.model.Category
import com.financemanager.app.data.model.PaymentMethod
import com.financemanager.app.data.model.RecurrenceInterval
import com.financemanager.app.data.model.TransactionEntity
import com.financemanager.app.data.model.TransactionType
import com.financemanager.app.ui.components.InstitutionLogo
import com.financemanager.app.ui.theme.ExpenseRed
import com.financemanager.app.ui.theme.IncomeGreen
import com.financemanager.app.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionSheet(
    viewModel: FinanceViewModel,
    transactionToEdit: TransactionEntity? = null,
    onDismiss: () -> Unit
) {
    val currency by viewModel.currentCurrency.collectAsState()
    val accounts by viewModel.accounts.collectAsState()

    val isEditing = transactionToEdit != null
    var showDeleteConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        var transactionType by remember(transactionToEdit) {
            mutableStateOf(transactionToEdit?.type ?: TransactionType.EXPENSE)
        }
        var title by remember(transactionToEdit) {
            mutableStateOf(transactionToEdit?.title ?: "")
        }
        var amountText by remember(transactionToEdit) {
            mutableStateOf(
                if (transactionToEdit != null) {
                    if (transactionToEdit.amount % 1.0 == 0.0) transactionToEdit.amount.toLong().toString() else transactionToEdit.amount.toString()
                } else ""
            )
        }
        var selectedAccountId by remember(accounts, transactionToEdit) {
            mutableStateOf(
                transactionToEdit?.accountId
                    ?: accounts.firstOrNull { it.isDefault && it.type == AccountType.BANK }?.id
                    ?: accounts.firstOrNull { it.isDefault }?.id
                    ?: accounts.firstOrNull { it.type == AccountType.BANK }?.id
                    ?: accounts.firstOrNull()?.id
            )
        }
        var selectedCategory: Category by remember(transactionType, transactionToEdit) {
            mutableStateOf(
                transactionToEdit?.category
                    ?: if (transactionType == TransactionType.EXPENSE) Category.FOOD else Category.SALARY
            )
        }
        var note by remember(transactionToEdit) {
            mutableStateOf(transactionToEdit?.note ?: "")
        }
        var isRecurring by remember { mutableStateOf(false) }
        var recurrenceInterval by remember { mutableStateOf(RecurrenceInterval.MONTHLY) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        val availableCategories = remember(transactionType) {
            if (transactionType == TransactionType.EXPENSE) {
                Category.expenseCategories()
            } else {
                Category.incomeCategories()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isEditing) "Edit Transaction" else "Add Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isEditing) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Transaction",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Expense / Income Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(4.dp)
            ) {
                // Expense button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (transactionType == TransactionType.EXPENSE) ExpenseRed else Color.Transparent
                        )
                        .clickable { transactionType = TransactionType.EXPENSE }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Expense",
                        color = if (transactionType == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Income button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (transactionType == TransactionType.INCOME) IncomeGreen else Color.Transparent
                        )
                        .clickable { transactionType = TransactionType.INCOME }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Income",
                        color = if (transactionType == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Amount Input Field
            OutlinedTextField(
                value = amountText,
                onValueChange = { input ->
                    if (input.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                        amountText = input
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Amount (${currency.symbol.trim()})") },
                placeholder = { Text("0.00") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.AttachMoney, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Title Input Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title / Description") },
                placeholder = { Text("e.g. Grocery store, Freelance client") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Category Selection
            Text(
                text = "Select Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableCategories) { cat ->
                    val isSelected = cat == selectedCategory
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { selectedCategory = cat }
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) cat.composeColor else cat.composeColor.copy(alpha = 0.15f)
                                )
                                .then(
                                    if (isSelected) Modifier.border(2.dp, Color.White, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = cat.icon,
                                contentDescription = cat.displayName,
                                tint = if (isSelected) Color.White else cat.composeColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = cat.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Account Selector
            if (accounts.isNotEmpty()) {
                Text(
                    text = if (transactionType == TransactionType.EXPENSE) "Paid With Account" else "Deposit To Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(accounts) { acc ->
                        val isSelected = acc.id == selectedAccountId
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier.clickable {
                                selectedAccountId = acc.id
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                InstitutionLogo(institution = acc.institution, size = 24.dp)
                                Column {
                                    Text(
                                        text = acc.name,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = currency.format(acc.balance),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Optional Note
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Note (Optional)") },
                placeholder = { Text("Add any extra notes...") },
                shape = RoundedCornerShape(16.dp),
                maxLines = 2
            )

            // Recurring Schedule Option (Only for new transactions)
            if (!isEditing) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Repeat,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Column {
                                    Text(
                                        text = "Repeat Transaction",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "Auto-schedule future payments",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = isRecurring,
                                onCheckedChange = { isRecurring = it }
                            )
                        }

                        if (isRecurring) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                RecurrenceInterval.entries.forEach { interval ->
                                    val isSelected = interval == recurrenceInterval
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { recurrenceInterval = interval },
                                        label = { Text(interval.displayName, style = MaterialTheme.typography.labelSmall) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save / Update Button
            Button(
                onClick = {
                    val amountVal = amountText.toDoubleOrNull()
                    when {
                        title.isBlank() -> {
                            errorMessage = "Please enter a transaction title"
                        }
                        amountVal == null || amountVal <= 0 -> {
                            errorMessage = "Please enter a valid amount greater than 0"
                        }
                        else -> {
                            val selectedAccount = accounts.firstOrNull { it.id == selectedAccountId }
                                ?: accounts.firstOrNull { it.isDefault && it.type == AccountType.BANK }
                                ?: accounts.firstOrNull { it.isDefault }
                                ?: accounts.firstOrNull { it.type == AccountType.BANK }
                                ?: accounts.firstOrNull()

                            val derivedPaymentMethod = when (selectedAccount?.type) {
                                AccountType.CASH -> PaymentMethod.CASH
                                AccountType.MOBILE_BANKING -> PaymentMethod.MOBILE_WALLET
                                AccountType.BANK -> PaymentMethod.BANK_TRANSFER
                                null -> transactionToEdit?.paymentMethod ?: PaymentMethod.CASH
                            }

                            if (isEditing && transactionToEdit != null) {
                                val updated = transactionToEdit.copy(
                                    title = title.trim(),
                                    amount = amountVal,
                                    type = transactionType,
                                    category = selectedCategory,
                                    paymentMethod = derivedPaymentMethod,
                                    note = note.trim(),
                                    accountId = selectedAccount?.id,
                                    accountName = selectedAccount?.name
                                )
                                viewModel.updateTransaction(updated)
                            } else {
                                viewModel.addTransaction(
                                    title = title.trim(),
                                    amount = amountVal,
                                    type = transactionType,
                                    category = selectedCategory,
                                    paymentMethod = derivedPaymentMethod,
                                    dateMillis = System.currentTimeMillis(),
                                    note = note.trim(),
                                    accountId = selectedAccount?.id,
                                    accountName = selectedAccount?.name
                                )
                                if (isRecurring) {
                                    viewModel.addRecurring(
                                        title = title.trim(),
                                        amount = amountVal,
                                        type = transactionType,
                                        category = selectedCategory,
                                        paymentMethod = derivedPaymentMethod,
                                        interval = recurrenceInterval,
                                        startDateMillis = recurrenceInterval.getNextDueDate(System.currentTimeMillis()),
                                        note = if (note.isBlank()) "Auto-recurring ${recurrenceInterval.displayName}" else note.trim()
                                    )
                                }
                            }
                            onDismiss()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isEditing) "Update Transaction" else "Save Transaction",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            if (isEditing) {
                Spacer(modifier = Modifier.height(10.dp))
                TextButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Delete Transaction",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    if (showDeleteConfirm && transactionToEdit != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Delete Transaction?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${transactionToEdit.title}'? The amount of ${currency.format(transactionToEdit.amount)} will be restored to its account.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(transactionToEdit)
                        showDeleteConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}
