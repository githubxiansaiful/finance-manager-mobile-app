package com.financemanager.app.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.financemanager.app.ui.screens.*
import com.financemanager.app.ui.theme.PrimaryGreen
import com.financemanager.app.ui.viewmodel.FinanceViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
    viewModel: FinanceViewModel = viewModel()
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val totalNetWorth by viewModel.totalNetWorth.collectAsState()
    val currentCurrency by viewModel.currentCurrency.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()

    Crossfade(targetState = currentUser, label = "AuthGate") { user ->
        if (user == null) {
            LoginScreen(viewModel = viewModel)
        } else {
            var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }
            var showAddTransactionSheet by remember { mutableStateOf(false) }
            var showAddAccountSheet by remember { mutableStateOf(false) }

            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val coroutineScope = rememberCoroutineScope()

            // 4 Core tabs in bottom navigation
            val bottomBarScreens = listOf(
                Screen.DASHBOARD,
                Screen.ACCOUNTS,
                Screen.TRANSACTIONS,
                Screen.BUDGETS
            )

            // Right-to-Left (End) Drawer implementation:
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            ModalDrawerSheet(
                                modifier = Modifier.width(300.dp),
                                drawerContainerColor = MaterialTheme.colorScheme.surface,
                                drawerTonalElevation = 0.dp
                            ) {
                                AppMenuDrawerContent(
                                    currentUser = user,
                                    currentScreen = currentScreen,
                                    totalNetWorth = totalNetWorth,
                                    currency = currentCurrency,
                                    themeMode = themeMode,
                                    onSelectScreen = { screen ->
                                        currentScreen = screen
                                        coroutineScope.launch { drawerState.close() }
                                    },
                                    onAddAccount = {
                                        showAddAccountSheet = true
                                        coroutineScope.launch { drawerState.close() }
                                    },
                                    onExportCsv = {
                                        viewModel.exportToCsv(context)
                                        coroutineScope.launch { drawerState.close() }
                                    },
                                    onExportPdf = {
                                        viewModel.exportToPdf(context)
                                        coroutineScope.launch { drawerState.close() }
                                    },
                                    onSelectTheme = { mode ->
                                        viewModel.setThemeMode(mode)
                                    },
                                    onSignOut = {
                                        coroutineScope.launch {
                                            drawerState.close()
                                            viewModel.signOut()
                                        }
                                    },
                                    onCloseDrawer = {
                                        coroutineScope.launch { drawerState.close() }
                                    }
                                )
                            }
                        }
                    }
                ) {
                    // Restore LTR for main scaffold content
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            topBar = {
                                // Consistent, sleek TopAppBar on EVERY tab and page!
                                TopAppBar(
                                    title = {
                                        Column {
                                            when (currentScreen) {
                                                Screen.DASHBOARD -> {
                                                    Text(
                                                        text = "Finance Flow",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.ExtraBold
                                                    )
                                                    Text(
                                                        text = "Hi, ${user.displayName.split(" ").firstOrNull() ?: "there"}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Screen.ACCOUNTS -> {
                                                    Text(
                                                        text = "Accounts & Wallets",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "Net Worth: ${currentCurrency.format(totalNetWorth)}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                                Screen.TRANSACTIONS -> {
                                                    Text(
                                                        text = "Transactions",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "All Activity & Records",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Screen.BUDGETS -> {
                                                    Text(
                                                        text = "Budget Goals",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "Monthly Spending Targets",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Screen.ANALYTICS -> {
                                                    Text(
                                                        text = "Financial Analytics",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "Monthly Trends & Charts",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Screen.CATEGORIES -> {
                                                    Text(
                                                        text = "Category Builder",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "Custom categories & styles",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Screen.SETTINGS -> {
                                                    Text(
                                                        text = "Settings",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "Preferences & Security",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    navigationIcon = {
                                        if (currentScreen == Screen.SETTINGS || currentScreen == Screen.ANALYTICS || currentScreen == Screen.CATEGORIES) {
                                            IconButton(onClick = { currentScreen = Screen.DASHBOARD }) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                    contentDescription = "Back"
                                                )
                                            }
                                        } else {
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = PrimaryGreen.copy(alpha = 0.15f),
                                                modifier = Modifier
                                                    .padding(start = 12.dp, end = 4.dp)
                                                    .size(36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.AccountBalanceWallet,
                                                        contentDescription = "App Logo",
                                                        tint = PrimaryGreen,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    actions = {
                                        // Menu trigger pill: Hamburger icon + User Avatar + Online status dot
                                        Surface(
                                            shape = RoundedCornerShape(22.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                            modifier = Modifier
                                                .padding(end = 12.dp)
                                                .clip(RoundedCornerShape(22.dp))
                                                .clickable { coroutineScope.launch { drawerState.open() } }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(start = 10.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Menu,
                                                    contentDescription = "Open Drawer Menu",
                                                    tint = MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Box {
                                                    Surface(
                                                        shape = CircleShape,
                                                        color = PrimaryGreen,
                                                        modifier = Modifier.size(30.dp)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = (user.displayName.take(1)).uppercase(),
                                                                color = Color.White,
                                                                style = MaterialTheme.typography.labelMedium,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .size(9.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFF10B981))
                                                            .border(BorderStroke(1.5.dp, MaterialTheme.colorScheme.surface), CircleShape)
                                                            .align(Alignment.BottomEnd)
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    )
                                )
                            },
                            bottomBar = {
                                NavigationBar(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 6.dp
                                ) {
                                    bottomBarScreens.forEach { screen ->
                                        val isSelected = currentScreen == screen
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = { currentScreen = screen },
                                            icon = {
                                                Icon(
                                                    imageVector = screen.icon,
                                                    contentDescription = screen.title
                                                )
                                            },
                                            label = {
                                                Text(
                                                    text = screen.title,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                            )
                                        )
                                    }
                                }
                            },
                            floatingActionButton = {
                                if (currentScreen == Screen.DASHBOARD || currentScreen == Screen.TRANSACTIONS) {
                                    FloatingActionButton(
                                        onClick = { showAddTransactionSheet = true },
                                        shape = CircleShape,
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add Transaction",
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            val screenModifier = Modifier.padding(innerPadding)

                            when (currentScreen) {
                                Screen.DASHBOARD -> HomeScreen(
                                    viewModel = viewModel,
                                    onNavigateToTransactions = { currentScreen = Screen.TRANSACTIONS },
                                    onNavigateToBudgets = { currentScreen = Screen.BUDGETS },
                                    onAddTransactionClick = { showAddTransactionSheet = true },
                                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                                    modifier = screenModifier
                                )
                                Screen.ACCOUNTS -> AccountsScreen(
                                    viewModel = viewModel,
                                    modifier = screenModifier
                                )
                                Screen.TRANSACTIONS -> TransactionsScreen(
                                    viewModel = viewModel,
                                    modifier = screenModifier
                                )
                                Screen.BUDGETS -> BudgetsScreen(
                                    viewModel = viewModel,
                                    modifier = screenModifier
                                )
                                Screen.ANALYTICS -> AnalyticsScreen(
                                    viewModel = viewModel,
                                    modifier = screenModifier
                                )
                                Screen.CATEGORIES -> CategoriesScreen(
                                    viewModel = viewModel,
                                    modifier = screenModifier
                                )
                                Screen.SETTINGS -> SettingsScreen(
                                    viewModel = viewModel,
                                    onNavigateToAccounts = { currentScreen = Screen.ACCOUNTS },
                                    modifier = screenModifier
                                )
                            }

                            if (showAddTransactionSheet) {
                                AddTransactionSheet(
                                    viewModel = viewModel,
                                    onDismiss = { showAddTransactionSheet = false }
                                )
                            }

                            if (showAddAccountSheet) {
                                AddAccountSheet(
                                    viewModel = viewModel,
                                    onDismiss = { showAddAccountSheet = false }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
