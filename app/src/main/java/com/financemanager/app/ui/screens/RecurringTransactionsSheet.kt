package com.financemanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.financemanager.app.data.model.*
import com.financemanager.app.ui.components.Formatters
import com.financemanager.app.ui.theme.ExpenseRed
import com.financemanager.app.ui.theme.IncomeGreen
import com.financemanager.app.ui.theme.PrimaryGreen
import com.financemanager.app.ui.viewmodel.FinanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTransactionsSheet(
    viewModel: FinanceViewModel,
    onDismiss: () -> Unit
) {
    val recurringList by viewModel.recurringTransactions.collectAsState()
    val currency by viewModel.currentCurrency.collectAsState()

    var showAddForm by remember { mutableStateOf(false) }
    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedCategory by remember(transactionType) {
        mutableStateOf(if (transactionType == TransactionType.EXPENSE) Category.HOUSING else Category.SALARY)
    }
    var selectedInterval by remember { mutableStateOf(RecurrenceInterval.MONTHLY) }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.BANK_TRANSFER) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var recurringToDelete by remember { mutableStateOf<RecurringTransactionEntity?>(null) }

    val availableCategories = remember(transactionType) {
        if (transactionType == TransactionType.EXPENSE) {
            Category.expenseCategories()
        } else {
            Category.incomeCategories()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Recurring Schedules",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Add or Toggle Form Button
            if (!showAddForm) {
                Button(
                    onClick = { showAddForm = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Recurring Schedule", fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Expandable Add Form
            if (showAddForm) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "New Recurring Record",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            TextButton(onClick = { showAddForm = false }) {
                                Text("Cancel")
                            }
                        }

                        // Expense / Income toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                                .padding(3.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (transactionType == TransactionType.EXPENSE) ExpenseRed else Color.Transparent)
                                    .clickable { transactionType = TransactionType.EXPENSE }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Expense",
                                    color = if (transactionType == TransactionType.EXPENSE) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (transactionType == TransactionType.INCOME) IncomeGreen else Color.Transparent)
                                    .clickable { transactionType = TransactionType.INCOME }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Income",
                                    color = if (transactionType == TransactionType.INCOME) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Title
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Title (e.g. Rent, Salary, Internet)") },
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        // Amount
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { input ->
                                if (input.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                                    amountText = input
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Amount (${currency.symbol})") },
                            placeholder = { Text("0.00") },
                            leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        // Interval selector
                        Text("Recurrence Frequency", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RecurrenceInterval.entries.forEach { interval ->
                                val isSelected = interval == selectedInterval
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedInterval = interval },
                                    label = { Text(interval.displayName, style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Category selector
                        Text("Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(availableCategories) { cat ->
                                val isSelected = cat == selectedCategory
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) cat.composeColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.clickable { selectedCategory = cat }
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = cat.icon,
                                            contentDescription = cat.displayName,
                                            tint = if (isSelected) cat.composeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = cat.displayName,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        if (errorMessage != null) {
                            Text(text = errorMessage ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        }

                        // Save Recurring Button
                        Button(
                            onClick = {
                                val amountVal = amountText.toDoubleOrNull()
                                when {
                                    title.isBlank() -> errorMessage = "Please enter a schedule title"
                                    amountVal == null || amountVal <= 0 -> errorMessage = "Please enter a valid amount"
                                    else -> {
                                        viewModel.addRecurring(
                                            title = title,
                                            amount = amountVal,
                                            type = transactionType,
                                            category = selectedCategory,
                                            paymentMethod = selectedPaymentMethod,
                                            interval = selectedInterval,
                                            startDateMillis = System.currentTimeMillis(),
                                            note = "Auto-recurring: ${selectedInterval.displayName}"
                                        )
                                        title = ""
                                        amountText = ""
                                        errorMessage = null
                                        showAddForm = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                        ) {
                            Text("Save Recurring Schedule", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Recurring List
            Text(
                text = "Active Schedules (${recurringList.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (recurringList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recurring transactions set yet.\nAdd rent, bills, or salary to automate them!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    recurringList.forEach { item ->
                        val isIncome = item.type == TransactionType.INCOME
                        val amountColor = if (isIncome) IncomeGreen else ExpenseRed

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(item.category.composeColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = item.category.icon,
                                        contentDescription = item.category.displayName,
                                        tint = item.category.composeColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = PrimaryGreen.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = item.interval.displayName,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PrimaryGreen,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "Next: ${Formatters.formatDate(item.nextDueDateMillis)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = (if (isIncome) "+ " else "- ") + Formatters.formatCurrency(item.amount, currency),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = amountColor
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Switch(
                                            checked = item.isActive,
                                            onCheckedChange = { viewModel.toggleRecurringActive(item) },
                                            modifier = Modifier.size(36.dp)
                                        )
                                        IconButton(
                                            onClick = { recurringToDelete = item },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Delete",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Recurring Confirmation Dialog
    recurringToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { recurringToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Delete Recurring Schedule?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to stop and delete the recurring schedule for '${item.title}' (${item.interval.displayName})?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRecurring(item.id)
                        recurringToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recurringToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
