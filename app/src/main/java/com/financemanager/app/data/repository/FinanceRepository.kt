package com.financemanager.app.data.repository

import com.financemanager.app.data.cloud.FirestoreSyncManager
import com.financemanager.app.data.local.AccountDao
import com.financemanager.app.data.local.BudgetDao
import com.financemanager.app.data.local.CategoryDao
import com.financemanager.app.data.local.RecurringDao
import com.financemanager.app.data.local.TransactionDao
import com.financemanager.app.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar

data class CategoryExpenseSummary(
    val category: Category,
    val totalAmount: Double,
    val transactionCount: Int,
    val percentage: Float
)

data class BudgetProgress(
    val category: Category,
    val limit: Double,
    val spent: Double,
    val remaining: Double,
    val progressFraction: Float,
    val isExceeded: Boolean
)

data class FinancialStats(
    val totalIncome: Double,
    val totalExpense: Double,
    val netBalance: Double,
    val savingsRate: Float
)

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val categoryDao: CategoryDao,
    private val recurringDao: RecurringDao,
    private val accountDao: AccountDao,
    val firestoreSyncManager: FirestoreSyncManager = FirestoreSyncManager()
) {
    fun getAllTransactions(userId: String): Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions(userId)

    fun getRecentTransactions(userId: String, limit: Int = 8): Flow<List<TransactionEntity>> =
        transactionDao.getRecentTransactions(userId, limit)

    fun getAllBudgets(userId: String): Flow<List<BudgetEntity>> =
        budgetDao.getAllBudgets(userId)

    fun getMonthRangeMillis(monthOffset: Int = 0): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, monthOffset)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val end = cal.timeInMillis

        return Pair(start, end)
    }

    fun getCurrentMonthTransactions(userId: String): Flow<List<TransactionEntity>> {
        val (start, end) = getMonthRangeMillis(0)
        return transactionDao.getTransactionsBetween(userId, start, end)
    }

    fun getFinancialStats(userId: String): Flow<FinancialStats> =
        getAllTransactions(userId).combine(getAllBudgets(userId)) { transactions, _ ->
            var income = 0.0
            var expense = 0.0
            for (tx in transactions) {
                if (tx.type == TransactionType.INCOME) {
                    income += tx.amount
                } else {
                    expense += tx.amount
                }
            }
            val balance = income - expense
            val savingsRate = if (income > 0) (((income - expense) / income) * 100).toFloat().coerceIn(0f, 100f) else 0f
            FinancialStats(
                totalIncome = income,
                totalExpense = expense,
                netBalance = balance,
                savingsRate = savingsRate
            )
        }

    fun getCategoryExpenseSummaries(userId: String): Flow<List<CategoryExpenseSummary>> =
        getCurrentMonthTransactions(userId).combine(getAllTransactions(userId)) { monthTxs, _ ->
            val expenseTxs = monthTxs.filter { it.type == TransactionType.EXPENSE }
            val totalExpense = expenseTxs.sumOf { it.amount }

            if (totalExpense == 0.0) {
                emptyList()
            } else {
                expenseTxs
                    .groupBy { it.category }
                    .map { (cat, txs) ->
                        val amount = txs.sumOf { it.amount }
                        CategoryExpenseSummary(
                            category = cat,
                            totalAmount = amount,
                            transactionCount = txs.size,
                            percentage = (amount / totalExpense).toFloat()
                        )
                    }
                    .sortedByDescending { it.totalAmount }
            }
        }

    fun getBudgetProgressList(userId: String): Flow<List<BudgetProgress>> =
        combine(getAllBudgets(userId), getCurrentMonthTransactions(userId)) { budgets, monthTxs ->
            val expenseByCat = monthTxs
                .filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.category }
                .mapValues { (_, txs) -> txs.sumOf { it.amount } }

            budgets.map { budget ->
                val spent = expenseByCat[budget.category] ?: 0.0
                val remaining = budget.monthlyLimit - spent
                val progressFraction = if (budget.monthlyLimit > 0) (spent / budget.monthlyLimit).toFloat() else 0f
                BudgetProgress(
                    category = budget.category,
                    limit = budget.monthlyLimit,
                    spent = spent,
                    remaining = remaining,
                    progressFraction = progressFraction,
                    isExceeded = spent > budget.monthlyLimit
                )
            }.sortedByDescending { it.progressFraction }
        }

    fun startRealtimeSync(userId: String, scope: CoroutineScope) {
        firestoreSyncManager.startRealtimeSync(userId, transactionDao, budgetDao, categoryDao, recurringDao, accountDao, scope)
    }

    fun stopRealtimeSync() {
        firestoreSyncManager.stopRealtimeSync()
    }

    suspend fun addTransaction(transaction: TransactionEntity): Long {
        val insertedId = transactionDao.insertTransaction(transaction)
        val entityWithId = if (transaction.id == 0L) transaction.copy(id = insertedId) else transaction
        firestoreSyncManager.saveTransaction(entityWithId)

        // Auto-adjust linked account balance
        transaction.accountId?.let { accId ->
            val delta = if (transaction.type == TransactionType.EXPENSE) -transaction.amount else transaction.amount
            adjustAccountBalance(transaction.userId, accId, delta)
        }
        return insertedId
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        val oldTx = transactionDao.getTransactionById(transaction.userId, transaction.id)
        transactionDao.updateTransaction(transaction)
        firestoreSyncManager.saveTransaction(transaction)

        if (oldTx != null) {
            oldTx.accountId?.let { oldAccId ->
                val revertDelta = if (oldTx.type == TransactionType.EXPENSE) oldTx.amount else -oldTx.amount
                adjustAccountBalance(oldTx.userId, oldAccId, revertDelta)
            }
        }
        transaction.accountId?.let { newAccId ->
            val newDelta = if (transaction.type == TransactionType.EXPENSE) -transaction.amount else transaction.amount
            adjustAccountBalance(transaction.userId, newAccId, newDelta)
        }
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
        firestoreSyncManager.deleteTransaction(transaction.userId, transaction.id)

        // Revert linked account balance
        transaction.accountId?.let { accId ->
            val reverseDelta = if (transaction.type == TransactionType.EXPENSE) transaction.amount else -transaction.amount
            adjustAccountBalance(transaction.userId, accId, reverseDelta)
        }
    }

    suspend fun deleteTransactionById(userId: String, id: Long) {
        val tx = transactionDao.getTransactionById(userId, id)
        if (tx != null) {
            deleteTransaction(tx)
        } else {
            transactionDao.deleteTransactionById(userId, id)
            firestoreSyncManager.deleteTransaction(userId, id)
        }
    }

    suspend fun transferBetweenAccounts(
        userId: String,
        fromAccount: AccountEntity,
        toAccount: AccountEntity,
        amount: Double,
        note: String = ""
    ) {
        val expenseTx = TransactionEntity(
            userId = userId,
            title = "Transfer to ${toAccount.name}",
            amount = amount,
            type = TransactionType.EXPENSE,
            category = Category.OTHER,
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            dateMillis = System.currentTimeMillis(),
            note = note.ifBlank { "Transfer to ${toAccount.name}" },
            accountId = fromAccount.id,
            accountName = fromAccount.name
        )
        val incomeTx = TransactionEntity(
            userId = userId,
            title = "Transfer from ${fromAccount.name}",
            amount = amount,
            type = TransactionType.INCOME,
            category = Category.OTHER,
            paymentMethod = PaymentMethod.BANK_TRANSFER,
            dateMillis = System.currentTimeMillis() + 1,
            note = note.ifBlank { "Transfer from ${fromAccount.name}" },
            accountId = toAccount.id,
            accountName = toAccount.name
        )
        addTransaction(expenseTx)
        addTransaction(incomeTx)
    }

    suspend fun upsertBudget(userId: String, category: Category, limit: Double) {
        val budget = BudgetEntity(userId = userId, category = category, monthlyLimit = limit)
        budgetDao.upsertBudget(budget)
        firestoreSyncManager.saveBudget(budget)
    }

    suspend fun deleteBudget(userId: String, category: Category) {
        budgetDao.deleteBudgetByCategory(userId, category)
        firestoreSyncManager.deleteBudget(userId, category)
    }

    // Custom Categories
    fun getAllCustomCategories(userId: String): Flow<List<CustomCategoryEntity>> =
        categoryDao.getAllCategories(userId)

    suspend fun addCustomCategory(category: CustomCategoryEntity) {
        categoryDao.insertCategory(category)
        firestoreSyncManager.saveCustomCategory(category)
    }

    suspend fun deleteCustomCategory(userId: String, name: String) {
        categoryDao.deleteCategoryByName(userId, name)
        firestoreSyncManager.deleteCustomCategory(userId, name)
    }

    // Recurring Transactions
    fun getAllRecurring(userId: String): Flow<List<RecurringTransactionEntity>> =
        recurringDao.getAllRecurring(userId)

    suspend fun addRecurring(recurring: RecurringTransactionEntity): Long {
        val id = recurringDao.insertRecurring(recurring)
        val withId = if (recurring.id == 0L) recurring.copy(id = id) else recurring
        firestoreSyncManager.saveRecurring(withId)
        return id
    }

    suspend fun updateRecurring(recurring: RecurringTransactionEntity) {
        recurringDao.updateRecurring(recurring)
        firestoreSyncManager.saveRecurring(recurring)
    }

    suspend fun deleteRecurring(userId: String, id: Long) {
        recurringDao.deleteRecurringById(userId, id)
        firestoreSyncManager.deleteRecurring(userId, id)
    }

    suspend fun processDueRecurringTransactions(userId: String) {
        val activeList = recurringDao.getActiveRecurring(userId)
        val now = System.currentTimeMillis()
        for (item in activeList) {
            if (item.nextDueDateMillis <= now) {
                // Auto-generate transaction for due recurring item
                addTransaction(
                    TransactionEntity(
                        userId = userId,
                        title = item.title,
                        amount = item.amount,
                        type = item.type,
                        category = item.category,
                        paymentMethod = item.paymentMethod,
                        dateMillis = item.nextDueDateMillis,
                        note = "Recurring (${item.interval.displayName}): ${item.note}".trim()
                    )
                )
                // Calculate next interval due date
                val nextDate = item.interval.getNextDueDate(item.nextDueDateMillis)
                val updated = item.copy(
                    lastProcessedDateMillis = item.nextDueDateMillis,
                    nextDueDateMillis = nextDate
                )
                updateRecurring(updated)
            }
        }
    }

    suspend fun updateUserCurrency(userId: String, currency: AppCurrency) {
        firestoreSyncManager.saveUserCurrency(userId, currency)
    }

    // Accounts Management
    fun getAllAccounts(userId: String): Flow<List<AccountEntity>> =
        accountDao.getAllAccounts(userId)

    suspend fun getAccountById(userId: String, id: Long): AccountEntity? =
        accountDao.getAccountById(userId, id)

    suspend fun addAccount(account: AccountEntity): Long {
        val insertedId = accountDao.insertAccount(account)
        val entityWithId = if (account.id == 0L) account.copy(id = insertedId) else account
        firestoreSyncManager.saveAccount(entityWithId)
        return insertedId
    }

    suspend fun updateAccount(account: AccountEntity) {
        accountDao.updateAccount(account)
        firestoreSyncManager.saveAccount(account)
    }

    suspend fun deleteAccount(userId: String, id: Long) {
        accountDao.deleteAccount(userId, id)
        firestoreSyncManager.deleteAccount(userId, id)
    }

    suspend fun adjustAccountBalance(userId: String, id: Long, amountDelta: Double) {
        accountDao.adjustBalance(userId, id, amountDelta)
        val updated = accountDao.getAccountById(userId, id)
        if (updated != null) {
            firestoreSyncManager.saveAccount(updated)
        }
    }

    fun getTotalNetWorth(userId: String): Flow<Double?> =
        accountDao.getTotalNetWorth(userId)

    suspend fun backfillMissingAccountNames(userId: String, accounts: List<AccountEntity>) {
        if (accounts.isEmpty()) return
        val allTx = transactionDao.getAllTransactionsSync(userId)
        val defaultAccount = accounts.firstOrNull { it.isDefault && it.type == AccountType.BANK }
            ?: accounts.firstOrNull { it.isDefault }
            ?: accounts.firstOrNull { it.type == AccountType.BANK }
            ?: accounts.firstOrNull() ?: return

        for (tx in allTx) {
            if (tx.accountName.isNullOrBlank()) {
                val matchedAccount = if (tx.accountId != null) {
                    accounts.firstOrNull { it.id == tx.accountId } ?: defaultAccount
                } else {
                    when (tx.paymentMethod) {
                        PaymentMethod.MOBILE_WALLET -> accounts.firstOrNull { it.type == AccountType.MOBILE_BANKING } ?: defaultAccount
                        PaymentMethod.CASH -> accounts.firstOrNull { it.type == AccountType.CASH } ?: defaultAccount
                        PaymentMethod.BANK_TRANSFER -> accounts.firstOrNull { it.type == AccountType.BANK } ?: defaultAccount
                        else -> defaultAccount
                    }
                }
                val updated = tx.copy(accountId = matchedAccount.id, accountName = matchedAccount.name)
                transactionDao.updateTransaction(updated)
                firestoreSyncManager.saveTransaction(updated)
            }
        }
    }
}
