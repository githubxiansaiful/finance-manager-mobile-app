package com.financemanager.app.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.financemanager.app.data.auth.FirebaseAuthManager
import com.financemanager.app.data.export.ExportManager
import com.financemanager.app.data.local.AppDatabase
import com.financemanager.app.data.local.AppThemeMode
import com.financemanager.app.data.local.UserSessionManager
import com.financemanager.app.data.model.*
import com.financemanager.app.data.repository.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TransactionFilterType {
    ALL,
    EXPENSE,
    INCOME
}

enum class TimeRangeFilter(val displayName: String) {
    ALL("All Time"),
    THIS_MONTH("This Month"),
    THIS_WEEK("This Week"),
    TODAY("Today")
}

data class DailySpending(
    val dayLabel: String,
    val totalExpense: Double,
    val isToday: Boolean = false
)

data class DashboardUiState(
    val stats: FinancialStats = FinancialStats(0.0, 0.0, 0.0, 0f),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val categorySummaries: List<CategoryExpenseSummary> = emptyList(),
    val topBudgets: List<BudgetProgress> = emptyList(),
    val isLoading: Boolean = false
)

data class TransactionsUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val searchQuery: String = "",
    val filterType: TransactionFilterType = TransactionFilterType.ALL,
    val selectedCategory: Category? = null,
    val timeFilter: TimeRangeFilter = TimeRangeFilter.ALL,
    val isLoading: Boolean = false
)

data class BudgetsUiState(
    val budgets: List<BudgetProgress> = emptyList(),
    val totalBudgetLimit: Double = 0.0,
    val totalBudgetSpent: Double = 0.0,
    val isLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    val firebaseAuthManager = FirebaseAuthManager(application)
    val userSessionManager = UserSessionManager(application)
    private val database = AppDatabase.getDatabase(application)
    private val repository = FinanceRepository(
        database.transactionDao(),
        database.budgetDao(),
        database.categoryDao(),
        database.recurringDao(),
        database.accountDao()
    )

    private val prefs = application.getSharedPreferences("finance_user_prefs", Context.MODE_PRIVATE)

    // Current logged-in user directly backed by Firebase Authentication
    val currentUser: StateFlow<UserAccount?> = firebaseAuthManager.currentUser

    // App Theme Setting
    val themeMode: StateFlow<AppThemeMode> = userSessionManager.themeMode

    fun setThemeMode(mode: AppThemeMode) {
        userSessionManager.setThemeMode(mode)
    }

    // Currency Setting
    private val _currentCurrency = MutableStateFlow(loadSavedCurrency())
    val currentCurrency: StateFlow<AppCurrency> = _currentCurrency.asStateFlow()

    private fun loadSavedCurrency(): AppCurrency {
        val code = prefs.getString("selected_currency", AppCurrency.USD.code)
        return AppCurrency.fromCode(code)
    }

    fun setCurrency(currency: AppCurrency) {
        _currentCurrency.value = currency
        prefs.edit().putString("selected_currency", currency.code).apply()
        val user = currentUser.value
        if (user != null) {
            viewModelScope.launch {
                repository.updateUserCurrency(user.id, currency)
            }
        }
    }

    init {
        // Start real-time Firestore sync & process recurring transactions
        viewModelScope.launch {
            currentUser.collect { user ->
                if (user != null) {
                    repository.startRealtimeSync(user.id, viewModelScope)
                    repository.processDueRecurringTransactions(user.id)
                } else {
                    repository.stopRealtimeSync()
                }
            }
        }
    }

    // Auth state for Login Screen
    private val _isAuthenticating = MutableStateFlow(false)
    val isAuthenticating: StateFlow<Boolean> = _isAuthenticating.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    // Search and filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterType = MutableStateFlow(TransactionFilterType.ALL)
    val filterType: StateFlow<TransactionFilterType> = _filterType.asStateFlow()

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    val selectedCategory: StateFlow<Category?> = _selectedCategory.asStateFlow()

    private val _timeFilter = MutableStateFlow(TimeRangeFilter.ALL)
    val timeFilter: StateFlow<TimeRangeFilter> = _timeFilter.asStateFlow()

    // Dashboard UI State dynamically scoped to currentUser.id
    val dashboardUiState: StateFlow<DashboardUiState> = currentUser.flatMapLatest { user ->
        if (user == null) {
            flowOf(DashboardUiState())
        } else {
            combine(
                repository.getFinancialStats(user.id),
                repository.getRecentTransactions(user.id),
                repository.getCategoryExpenseSummaries(user.id),
                repository.getBudgetProgressList(user.id)
            ) { stats, recent, catSummaries, budgets ->
                DashboardUiState(
                    stats = stats,
                    recentTransactions = recent,
                    categorySummaries = catSummaries,
                    topBudgets = budgets.take(3),
                    isLoading = false
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    // 7-day daily spending trend for analytics
    val dailySpendingTrends: StateFlow<List<DailySpending>> = currentUser.flatMapLatest { user ->
        if (user == null) {
            flowOf(emptyList())
        } else {
            repository.getAllTransactions(user.id).map { allTxs ->
                val dateFormat = SimpleDateFormat("EEE", Locale.getDefault())
                val expenses = allTxs.filter { it.type == TransactionType.EXPENSE }
                val results = mutableListOf<DailySpending>()

                for (daysAgo in 6 downTo 0) {
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val start = cal.timeInMillis

                    cal.set(Calendar.HOUR_OF_DAY, 23)
                    cal.set(Calendar.MINUTE, 59)
                    cal.set(Calendar.SECOND, 59)
                    cal.set(Calendar.MILLISECOND, 999)
                    val end = cal.timeInMillis

                    val dayExpense = expenses
                        .filter { it.dateMillis in start..end }
                        .sumOf { it.amount }

                    val label = if (daysAgo == 0) "Today" else dateFormat.format(Date(start))
                    results.add(
                        DailySpending(
                            dayLabel = label,
                            totalExpense = dayExpense,
                            isToday = daysAgo == 0
                        )
                    )
                }
                results
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Transactions UI State dynamically scoped to currentUser.id
    val transactionsUiState: StateFlow<TransactionsUiState> = currentUser.flatMapLatest { user ->
        if (user == null) {
            flowOf(TransactionsUiState())
        } else {
            combine(
                repository.getAllTransactions(user.id),
                _searchQuery,
                _filterType,
                _selectedCategory,
                _timeFilter
            ) { txs, query, typeFilter, catFilter, timeFilter ->
                val now = System.currentTimeMillis()
                val oneDayMillis = 86400000L

                val filtered = txs.filter { tx ->
                    val matchesQuery = query.isBlank() ||
                            tx.title.contains(query, ignoreCase = true) ||
                            tx.note.contains(query, ignoreCase = true) ||
                            tx.category.displayName.contains(query, ignoreCase = true)

                    val matchesType = when (typeFilter) {
                        TransactionFilterType.ALL -> true
                        TransactionFilterType.EXPENSE -> tx.type == TransactionType.EXPENSE
                        TransactionFilterType.INCOME -> tx.type == TransactionType.INCOME
                    }

                    val matchesCategory = catFilter == null || tx.category == catFilter

                    val matchesTime = when (timeFilter) {
                        TimeRangeFilter.ALL -> true
                        TimeRangeFilter.TODAY -> tx.dateMillis >= (now - oneDayMillis)
                        TimeRangeFilter.THIS_WEEK -> tx.dateMillis >= (now - oneDayMillis * 7)
                        TimeRangeFilter.THIS_MONTH -> {
                            val (start, end) = repository.getMonthRangeMillis(0)
                            tx.dateMillis in start..end
                        }
                    }

                    matchesQuery && matchesType && matchesCategory && matchesTime
                }

                TransactionsUiState(
                    transactions = filtered,
                    searchQuery = query,
                    filterType = typeFilter,
                    selectedCategory = catFilter,
                    timeFilter = timeFilter,
                    isLoading = false
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionsUiState(isLoading = true)
    )

    // Budgets UI State dynamically scoped to currentUser.id
    val budgetsUiState: StateFlow<BudgetsUiState> = currentUser.flatMapLatest { user ->
        if (user == null) {
            flowOf(BudgetsUiState())
        } else {
            repository.getBudgetProgressList(user.id).map { budgets ->
                val totalLimit = budgets.sumOf { it.limit }
                val totalSpent = budgets.sumOf { it.spent }
                BudgetsUiState(
                    budgets = budgets,
                    totalBudgetLimit = totalLimit,
                    totalBudgetSpent = totalSpent,
                    isLoading = false
                )
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetsUiState(isLoading = true)
    )

    // Filter mutations
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterType(type: TransactionFilterType) {
        _filterType.value = type
    }

    fun setSelectedCategory(category: Category?) {
        _selectedCategory.value = category
    }

    fun setTimeFilter(filter: TimeRangeFilter) {
        _timeFilter.value = filter
    }

    // Real Firebase Authentication actions
    fun signInWithGoogle(webClientId: String) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authErrorMessage.value = null
            val result = firebaseAuthManager.signInWithGoogle(webClientId)
            result.onSuccess {
                _isAuthenticating.value = false
            }.onFailure { error ->
                _authErrorMessage.value = error.localizedMessage ?: "Google Sign-In failed"
                _isAuthenticating.value = false
            }
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authErrorMessage.value = null
            val result = firebaseAuthManager.signInWithEmail(email, pass)
            result.onSuccess {
                _isAuthenticating.value = false
            }.onFailure { error ->
                _authErrorMessage.value = error.localizedMessage ?: "Sign-in failed"
                _isAuthenticating.value = false
            }
        }
    }

    fun signUpWithEmail(email: String, pass: String, name: String) {
        viewModelScope.launch {
            _isAuthenticating.value = true
            _authErrorMessage.value = null
            val result = firebaseAuthManager.signUpWithEmail(email, pass, name)
            result.onSuccess {
                _isAuthenticating.value = false
            }.onFailure { error ->
                _authErrorMessage.value = error.localizedMessage ?: "Sign-up failed"
                _isAuthenticating.value = false
            }
        }
    }

    fun clearAuthError() {
        _authErrorMessage.value = null
    }

    fun getStoredWebClientId(): String = firebaseAuthManager.getStoredWebClientId()

    fun saveStoredWebClientId(clientId: String) = firebaseAuthManager.saveStoredWebClientId(clientId)

    fun signOut() {
        viewModelScope.launch {
            firebaseAuthManager.signOut()
            _searchQuery.value = ""
            _filterType.value = TransactionFilterType.ALL
            _selectedCategory.value = null
            _timeFilter.value = TimeRangeFilter.ALL
        }
    }

    // CRUD actions scoped to currentUser
    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: Category,
        paymentMethod: PaymentMethod,
        dateMillis: Long,
        note: String,
        accountId: Long? = null,
        accountName: String? = null
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.addTransaction(
                TransactionEntity(
                    userId = user.id,
                    title = title.trim(),
                    amount = amount,
                    type = type,
                    category = category,
                    paymentMethod = paymentMethod,
                    dateMillis = dateMillis,
                    note = note.trim(),
                    accountId = accountId,
                    accountName = accountName
                )
            )
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun upsertBudget(category: Category, limit: Double) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.upsertBudget(user.id, category, limit)
        }
    }

    fun deleteBudget(category: Category) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteBudget(user.id, category)
        }
    }

    // Custom Categories
    val customCategories: StateFlow<List<CustomCategoryEntity>> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else repository.getAllCustomCategories(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCustomCategory(name: String, iconKey: String, colorHex: Long, isExpense: Boolean) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.addCustomCategory(
                CustomCategoryEntity(
                    userId = user.id,
                    name = name.trim(),
                    iconKey = iconKey,
                    colorHex = colorHex,
                    isExpense = isExpense
                )
            )
        }
    }

    fun deleteCustomCategory(name: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteCustomCategory(user.id, name)
        }
    }

    // Recurring Transactions
    val recurringTransactions: StateFlow<List<RecurringTransactionEntity>> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else repository.getAllRecurring(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addRecurring(
        title: String,
        amount: Double,
        type: TransactionType,
        category: Category,
        paymentMethod: PaymentMethod,
        interval: RecurrenceInterval,
        startDateMillis: Long,
        note: String
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.addRecurring(
                RecurringTransactionEntity(
                    userId = user.id,
                    title = title.trim(),
                    amount = amount,
                    type = type,
                    category = category,
                    paymentMethod = paymentMethod,
                    interval = interval,
                    nextDueDateMillis = startDateMillis,
                    note = note.trim()
                )
            )
        }
    }

    fun toggleRecurringActive(item: RecurringTransactionEntity) {
        viewModelScope.launch {
            repository.updateRecurring(item.copy(isActive = !item.isActive))
        }
    }

    fun deleteRecurring(id: Long) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteRecurring(user.id, id)
        }
    }

    // Export Actions (CSV & PDF)
    fun exportToCsv(context: Context) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val allTxs = repository.getAllTransactions(user.id).first()
            val file = ExportManager.exportToCsv(context, allTxs, currentCurrency.value)
            ExportManager.shareFile(context, file, "text/csv", "Share Financial Transactions (CSV)")
        }
    }

    fun exportToPdf(context: Context) {
        val user = currentUser.value ?: return
        val userName = user.displayName
        viewModelScope.launch {
            val allTxs = repository.getAllTransactions(user.id).first()
            val stats = repository.getFinancialStats(user.id).first()
            val periodName = "This Month"
            val file = ExportManager.exportToPdf(context, userName, periodName, stats, allTxs, currentCurrency.value)
            ExportManager.shareFile(context, file, "application/pdf", "Share Monthly Financial Statement (PDF)")
        }
    }

    // Accounts (Cash, Bank Accounts, Mobile Banking: bKash, Nagad, Rocket, DBBL, etc.)
    val accounts: StateFlow<List<AccountEntity>> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(emptyList()) else repository.getAllAccounts(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalNetWorth: StateFlow<Double> = currentUser.flatMapLatest { user ->
        if (user == null) flowOf(0.0) else repository.getTotalNetWorth(user.id).map { it ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun addAccount(
        name: String,
        type: AccountType,
        institution: FinancialInstitution,
        accountNumber: String,
        initialBalance: Double,
        colorHex: Long = institution.brandColorHex,
        isDefault: Boolean = false,
        note: String = ""
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.addAccount(
                AccountEntity(
                    userId = user.id,
                    name = name.trim(),
                    type = type,
                    institution = institution,
                    accountNumber = accountNumber.trim(),
                    balance = initialBalance,
                    colorHex = colorHex,
                    isDefault = isDefault,
                    note = note.trim()
                )
            )
        }
    }

    fun updateAccount(account: AccountEntity) {
        viewModelScope.launch {
            repository.updateAccount(account)
        }
    }

    fun deleteAccount(id: Long) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteAccount(user.id, id)
        }
    }

    fun adjustAccountBalance(id: Long, amountDelta: Double) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.adjustAccountBalance(user.id, id, amountDelta)
        }
    }

    fun transferBetweenAccounts(
        fromAccount: AccountEntity,
        toAccount: AccountEntity,
        amount: Double,
        note: String = ""
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.transferBetweenAccounts(user.id, fromAccount, toAccount, amount, note)
        }
    }
}

