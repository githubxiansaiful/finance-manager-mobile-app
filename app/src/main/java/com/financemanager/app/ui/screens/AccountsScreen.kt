package com.financemanager.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.financemanager.app.data.model.AccountEntity
import com.financemanager.app.data.model.AccountType
import com.financemanager.app.data.model.FinancialInstitution
import com.financemanager.app.ui.components.InstitutionLogo
import com.financemanager.app.ui.components.TransferBetweenAccountsDialog
import com.financemanager.app.ui.theme.PrimaryGreen
import com.financemanager.app.ui.theme.PrimaryGreenDark
import com.financemanager.app.ui.viewmodel.FinanceViewModel

enum class AccountFilter(val displayName: String) {
    ALL("All Accounts"),
    MOBILE_BANKING("Mobile Banking"),
    BANKS("Banks"),
    CASH("Cash")
}

@Composable
fun AccountsScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.accounts.collectAsState()
    val totalNetWorth by viewModel.totalNetWorth.collectAsState()
    val currency by viewModel.currentCurrency.collectAsState()

    var showAddAccountSheet by remember { mutableStateOf(false) }
    var showTransferDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(AccountFilter.ALL) }
    var accountToEditName by remember { mutableStateOf<AccountEntity?>(null) }
    var accountToAdjust by remember { mutableStateOf<AccountEntity?>(null) }
    var accountToDelete by remember { mutableStateOf<AccountEntity?>(null) }

    val filteredAccounts = remember(accounts, selectedFilter) {
        when (selectedFilter) {
            AccountFilter.ALL -> accounts
            AccountFilter.MOBILE_BANKING -> accounts.filter { it.type == AccountType.MOBILE_BANKING }
            AccountFilter.BANKS -> accounts.filter { it.type == AccountType.BANK }
            AccountFilter.CASH -> accounts.filter { it.type == AccountType.CASH }
        }
    }

    // Breakdown
    val mobileTotal = remember(accounts) {
        accounts.filter { it.type == AccountType.MOBILE_BANKING }.sumOf { it.balance }
    }
    val bankTotal = remember(accounts) {
        accounts.filter { it.type == AccountType.BANK }.sumOf { it.balance }
    }
    val cashTotal = remember(accounts) {
        accounts.filter { it.type == AccountType.CASH }.sumOf { it.balance }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddAccountSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Account")
            }
        },
        modifier = modifier.fillMaxSize()
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Net Worth Hero Card (Obsidian Executive Styling)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.45f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF0F172A),
                                        Color(0xFF1E293B)
                                    )
                                )
                            )
                            .padding(22.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TOTAL NET WORTH",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.7f),
                                    letterSpacing = 1.1.sp
                                )
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.18f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        text = "${accounts.size} Accounts",
                                        color = Color(0xFF34D399),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Text(
                                text = currency.format(totalNetWorth),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 34.sp
                            )

                            // 3-way breakdown pill
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF0F172A).copy(alpha = 0.6f))
                                    .border(BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.4f)), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Mobile", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                                    Text(currency.format(mobileTotal), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                }
                                Box(modifier = Modifier.width(1.dp).height(28.dp).background(Color(0xFF334155).copy(alpha = 0.5f)))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Banks", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                                    Text(currency.format(bankTotal), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                }
                                Box(modifier = Modifier.width(1.dp).height(28.dp).background(Color(0xFF334155).copy(alpha = 0.5f)))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Cash", color = Color.White.copy(alpha = 0.6f), style = MaterialTheme.typography.labelSmall)
                                    Text(currency.format(cashTotal), color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            }

            // Quick Actions: Transfer & Add
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { showTransferDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Transfer",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { showAddAccountSheet = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Account", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // Filter Tabs
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(AccountFilter.entries) { filter ->
                        FilterChip(
                            selected = filter == selectedFilter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter.displayName) },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Accounts List
            if (filteredAccounts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (selectedFilter == AccountFilter.ALL) "No Accounts Added Yet" else "No ${selectedFilter.displayName} Added",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Track your bKash, Nagad, Rocket, Dutch Bangla Bank, Pubali, Islami Bank, or Cash wallet in one place.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(0.9f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { showAddAccountSheet = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add First Account")
                            }
                        }
                    }
                }
            } else {
                items(filteredAccounts, key = { it.id }) { account ->
                    AccountCardItem(
                        account = account,
                        currency = currency,
                        onEditName = { accountToEditName = account },
                        onAdjustBalance = { accountToAdjust = account },
                        onDelete = { accountToDelete = account }
                    )
                }
            }
        }
    }

    // Edit Account Name Dialog
    accountToEditName?.let { account ->
        var editedName by remember(account) { mutableStateOf(account.name) }
        AlertDialog(
            onDismissRequest = { accountToEditName = null },
            title = {
                Text(
                    text = "Edit Account Name",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Change the display name for this account:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Account Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedName.isNotBlank()) {
                            viewModel.updateAccount(account.copy(name = editedName.trim()))
                            accountToEditName = null
                        }
                    },
                    enabled = editedName.isNotBlank(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { accountToEditName = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    // Add Account Sheet
    if (showAddAccountSheet) {
        AddAccountSheet(
            viewModel = viewModel,
            onDismiss = { showAddAccountSheet = false }
        )
    }

    // Transfer Between Accounts Dialog
    if (showTransferDialog) {
        TransferBetweenAccountsDialog(
            accounts = accounts,
            currency = currency,
            onDismiss = { showTransferDialog = false },
            onTransfer = { from, to, amount, note ->
                viewModel.transferBetweenAccounts(from, to, amount, note)
            }
        )
    }

    // Adjust Balance Dialog
    accountToAdjust?.let { account ->
        AdjustBalanceDialog(
            account = account,
            currency = currency,
            onDismiss = { accountToAdjust = null },
            onConfirm = { delta ->
                viewModel.adjustAccountBalance(account.id, delta)
                accountToAdjust = null
            }
        )
    }

    // Delete Confirmation Dialog
    accountToDelete?.let { account ->
        AlertDialog(
            onDismissRequest = { accountToDelete = null },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to delete '${account.name}'? Its balance will be removed from your total net worth.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount(account.id)
                        accountToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { accountToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AccountCardItem(
    account: AccountEntity,
    currency: com.financemanager.app.data.model.AppCurrency,
    onEditName: () -> Unit,
    onAdjustBalance: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    InstitutionLogo(institution = account.institution, size = 46.dp)

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.clickable { onEditName() }
                            )
                            IconButton(
                                onClick = onEditName,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Name",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            if (account.isDefault) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Primary",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = if (account.accountNumber.isNotBlank()) {
                                "${account.institution.displayName.substringBefore(" (")} • ${account.accountNumber.takeLast(4).padStart(account.accountNumber.length, '•')}"
                            } else {
                                account.institution.displayName
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Balance
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currency.format(account.balance),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (account.balance >= 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = account.type.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(
                        onClick = onEditName,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit Name", style = MaterialTheme.typography.labelMedium)
                    }

                    TextButton(
                        onClick = onAdjustBalance,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.SwapVert, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adjust Balance", style = MaterialTheme.typography.labelMedium)
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AdjustBalanceDialog(
    account: AccountEntity,
    currency: com.financemanager.app.data.model.AppCurrency,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var mode by remember { mutableStateOf("DEPOSIT") } // DEPOSIT or WITHDRAW
    var amountText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                InstitutionLogo(institution = account.institution, size = 28.dp)
                Text("Adjust ${account.name}")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Current Balance: ${currency.format(account.balance)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp)
                ) {
                    TabButton(
                        text = "Deposit (+)",
                        isSelected = mode == "DEPOSIT",
                        onClick = { mode = "DEPOSIT" },
                        modifier = Modifier.weight(1f)
                    )
                    TabButton(
                        text = "Withdraw (-)",
                        isSelected = mode == "WITHDRAW",
                        onClick = { mode = "WITHDRAW" },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (${currency.symbol})") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (error != null) {
                    Text(text = error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull()
                    if (amt == null || amt <= 0) {
                        error = "Enter a valid amount."
                        return@Button
                    }
                    val delta = if (mode == "DEPOSIT") amt else -amt
                    onConfirm(delta)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
