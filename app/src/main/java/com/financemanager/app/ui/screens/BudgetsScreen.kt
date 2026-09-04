package com.financemanager.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.financemanager.app.data.model.Category
import com.financemanager.app.ui.components.BudgetCard
import com.financemanager.app.ui.components.Formatters
import com.financemanager.app.ui.theme.ExpenseRed
import com.financemanager.app.ui.theme.IncomeGreen
import com.financemanager.app.ui.viewmodel.FinanceViewModel

@Composable
fun BudgetsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.budgetsUiState.collectAsState()
    val currency by viewModel.currentCurrency.collectAsState()
    var showSetBudgetDialog by remember { mutableStateOf(false) }
    var selectedCategoryForEdit by remember { mutableStateOf(Category.FOOD) }
    var currentLimitText by remember { mutableStateOf("") }

    val remainingTotal = state.totalBudgetLimit - state.totalBudgetSpent
    val overallFraction = if (state.totalBudgetLimit > 0) {
        (state.totalBudgetSpent / state.totalBudgetLimit).toFloat().coerceIn(0f, 1f)
    } else 0f

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedCategoryForEdit = Category.FOOD
                    currentLimitText = ""
                    showSetBudgetDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Budget")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Overview Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total Monthly Limit",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = Formatters.formatCurrency(state.totalBudgetLimit, currency),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (remainingTotal >= 0) "Safe to Spend" else "Over Budget",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (remainingTotal >= 0) IncomeGreen else ExpenseRed
                                )
                                Text(
                                    text = Formatters.formatCurrency(if (remainingTotal >= 0) remainingTotal else -remainingTotal, currency),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (remainingTotal >= 0) IncomeGreen else ExpenseRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = { overallFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            color = if (remainingTotal >= 0) MaterialTheme.colorScheme.primary else ExpenseRed,
                            trackColor = MaterialTheme.colorScheme.surface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Spent ${Formatters.formatCurrency(state.totalBudgetSpent, currency)} of ${Formatters.formatCurrency(state.totalBudgetLimit, currency)} (${String.format("%.0f", overallFraction * 100)}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Categories list header
            item {
                Text(
                    text = "Category Limits (${state.budgets.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (state.budgets.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No category budgets created yet.\nTap '+' to create your first budget target!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(state.budgets) { budgetProgress ->
                    BudgetCard(
                        budget = budgetProgress,
                        currency = currency,
                        onEdit = {
                            selectedCategoryForEdit = budgetProgress.category
                            currentLimitText = budgetProgress.limit.toString()
                            showSetBudgetDialog = true
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Edit / Create Budget Dialog
    if (showSetBudgetDialog) {
        AlertDialog(
            onDismissRequest = { showSetBudgetDialog = false },
            title = { Text("Set Monthly Budget") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Category: ${selectedCategoryForEdit.displayName}",
                        fontWeight = FontWeight.SemiBold
                    )

                    OutlinedTextField(
                        value = currentLimitText,
                        onValueChange = { input ->
                            if (input.matches(Regex("^\\d*(\\.\\d{0,2})?$"))) {
                                currentLimitText = input
                            }
                        },
                        label = { Text("Monthly Limit Amount (${currency.symbol.trim()})") },
                        placeholder = { Text("500.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val limit = currentLimitText.toDoubleOrNull()
                        if (limit != null && limit > 0) {
                            viewModel.upsertBudget(selectedCategoryForEdit, limit)
                            showSetBudgetDialog = false
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSetBudgetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
