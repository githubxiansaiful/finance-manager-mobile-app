package com.financemanager.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.financemanager.app.data.model.TransactionEntity
import com.financemanager.app.ui.components.ExportDialog
import com.financemanager.app.ui.components.Formatters
import com.financemanager.app.ui.components.TransactionItem
import com.financemanager.app.ui.viewmodel.FinanceViewModel
import com.financemanager.app.ui.viewmodel.TimeRangeFilter
import com.financemanager.app.ui.viewmodel.TransactionFilterType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.transactionsUiState.collectAsState()
    val currency by viewModel.currentCurrency.collectAsState()
    var transactionToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var showExportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${state.transactions.size} records found",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FilledTonalButton(
                onClick = { showExportDialog = true },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.height(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export CSV/PDF", style = MaterialTheme.typography.labelSmall)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search transactions, notes...") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Type Filter Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = state.filterType == TransactionFilterType.ALL,
                onClick = { viewModel.setFilterType(TransactionFilterType.ALL) },
                label = { Text("All") }
            )
            FilterChip(
                selected = state.filterType == TransactionFilterType.EXPENSE,
                onClick = { viewModel.setFilterType(TransactionFilterType.EXPENSE) },
                label = { Text("Expenses") }
            )
            FilterChip(
                selected = state.filterType == TransactionFilterType.INCOME,
                onClick = { viewModel.setFilterType(TransactionFilterType.INCOME) },
                label = { Text("Income") }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Time Range Chips Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimeRangeFilter.entries.forEach { timeFilter ->
                FilterChip(
                    selected = state.timeFilter == timeFilter,
                    onClick = { viewModel.setTimeFilter(timeFilter) },
                    label = { Text(timeFilter.displayName) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Results count
        Text(
            text = "${state.transactions.size} transactions found",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Transactions List
        if (state.transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No matching transactions found.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            val grouped = remember(state.transactions) {
                state.transactions.groupBy { tx ->
                    Formatters.formatDate(tx.dateMillis)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                grouped.forEach { (dateHeader, txList) ->
                    item(key = "header_$dateHeader") {
                        Text(
                            text = dateHeader,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 6.dp, top = 4.dp, bottom = 2.dp)
                        )
                    }

                    item(key = "group_$dateHeader") {
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 1.dp,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                txList.forEachIndexed { index, tx ->
                                    TransactionItem(
                                        transaction = tx,
                                        currency = currency,
                                        onEdit = { transactionToEdit = tx }
                                    )
                                    if (index < txList.lastIndex) {
                                        HorizontalDivider(
                                            modifier = Modifier.padding(start = 70.dp, end = 16.dp),
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
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

    // Delete Confirmation Dialog
    transactionToDelete?.let { tx ->
        AlertDialog(
            onDismissRequest = { transactionToDelete = null },
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
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Are you sure you want to delete '${tx.title}' (${currency.format(tx.amount)})?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!tx.accountName.isNullOrBlank()) {
                        Text(
                            text = "The balance for '${tx.accountName}' will be automatically restored.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTransaction(tx)
                        transactionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Transaction Sheet
    if (transactionToEdit != null) {
        AddTransactionSheet(
            viewModel = viewModel,
            transactionToEdit = transactionToEdit,
            onDismiss = { transactionToEdit = null }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        val context = LocalContext.current
        ExportDialog(
            transactionCount = state.transactions.size,
            currency = currency,
            onExportCsv = { viewModel.exportToCsv(context) },
            onExportPdf = { viewModel.exportToPdf(context) },
            onDismiss = { showExportDialog = false }
        )
    }
}
