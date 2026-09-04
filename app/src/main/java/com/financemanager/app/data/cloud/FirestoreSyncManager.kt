package com.financemanager.app.data.cloud

import android.util.Log
import com.financemanager.app.data.local.AccountDao
import com.financemanager.app.data.local.BudgetDao
import com.financemanager.app.data.local.CategoryDao
import com.financemanager.app.data.local.RecurringDao
import com.financemanager.app.data.local.TransactionDao
import com.financemanager.app.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirestoreSyncManager(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private var transactionListener: ListenerRegistration? = null
    private var budgetListener: ListenerRegistration? = null
    private var categoryListener: ListenerRegistration? = null
    private var recurringListener: ListenerRegistration? = null
    private var accountListener: ListenerRegistration? = null

    suspend fun saveTransaction(transaction: TransactionEntity): Result<Unit> {
        return try {
            val data = hashMapOf(
                "id" to transaction.id,
                "userId" to transaction.userId,
                "title" to transaction.title,
                "amount" to transaction.amount,
                "type" to transaction.type.name,
                "category" to transaction.category.name,
                "paymentMethod" to transaction.paymentMethod.name,
                "dateMillis" to transaction.dateMillis,
                "note" to transaction.note,
                "accountId" to transaction.accountId,
                "accountName" to transaction.accountName,
                "syncedAt" to System.currentTimeMillis()
            )
            firestore.collection("users")
                .document(transaction.userId)
                .collection("transactions")
                .document(transaction.id.toString())
                .set(data, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreSync", "Notice: Transaction write pending or failed: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun deleteTransaction(userId: String, transactionId: Long): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("transactions")
                .document(transactionId.toString())
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreSync", "Notice: Transaction delete pending or failed: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun saveBudget(budget: BudgetEntity): Result<Unit> {
        return try {
            val data = hashMapOf(
                "userId" to budget.userId,
                "category" to budget.category.name,
                "monthlyLimit" to budget.monthlyLimit,
                "syncedAt" to System.currentTimeMillis()
            )
            firestore.collection("users")
                .document(budget.userId)
                .collection("budgets")
                .document(budget.category.name)
                .set(data, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreSync", "Notice: Budget write pending or failed: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun deleteBudget(userId: String, category: Category): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("budgets")
                .document(category.name)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreSync", "Notice: Budget delete pending or failed: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun saveCustomCategory(category: CustomCategoryEntity): Result<Unit> {
        return try {
            val data = hashMapOf(
                "userId" to category.userId,
                "name" to category.name,
                "iconKey" to category.iconKey,
                "colorHex" to category.colorHex,
                "isExpense" to category.isExpense,
                "syncedAt" to System.currentTimeMillis()
            )
            firestore.collection("users")
                .document(category.userId)
                .collection("categories")
                .document(category.name)
                .set(data, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreSync", "Notice: Category save error: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun deleteCustomCategory(userId: String, name: String): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("categories")
                .document(name)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreSync", "Notice: Category delete error: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun saveRecurring(recurring: RecurringTransactionEntity): Result<Unit> {
        return try {
            val data = hashMapOf(
                "id" to recurring.id,
                "userId" to recurring.userId,
                "title" to recurring.title,
                "amount" to recurring.amount,
                "type" to recurring.type.name,
                "category" to recurring.category.name,
                "paymentMethod" to recurring.paymentMethod.name,
                "interval" to recurring.interval.name,
                "nextDueDateMillis" to recurring.nextDueDateMillis,
                "lastProcessedDateMillis" to recurring.lastProcessedDateMillis,
                "isActive" to recurring.isActive,
                "note" to recurring.note,
                "syncedAt" to System.currentTimeMillis()
            )
            firestore.collection("users")
                .document(recurring.userId)
                .collection("recurring")
                .document(recurring.id.toString())
                .set(data, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreSync", "Notice: Recurring save error: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun deleteRecurring(userId: String, id: Long): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("recurring")
                .document(id.toString())
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreSync", "Notice: Recurring delete error: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun saveAccount(account: AccountEntity): Result<Unit> {
        return try {
            val data = hashMapOf(
                "id" to account.id,
                "userId" to account.userId,
                "name" to account.name,
                "type" to account.type.name,
                "institution" to account.institution.name,
                "accountNumber" to account.accountNumber,
                "balance" to account.balance,
                "colorHex" to account.colorHex,
                "isDefault" to account.isDefault,
                "note" to account.note,
                "syncedAt" to System.currentTimeMillis()
            )
            firestore.collection("users")
                .document(account.userId)
                .collection("accounts")
                .document(account.id.toString())
                .set(data, SetOptions.merge())
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreSync", "Notice: Account save error: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(userId: String, id: Long): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("accounts")
                .document(id.toString())
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.w("FirestoreSync", "Notice: Account delete error: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    suspend fun saveUserCurrency(userId: String, currency: AppCurrency): Result<Unit> {
        return try {
            val data = hashMapOf("currency" to currency.code)
            firestore.collection("users").document(userId).set(data, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun startRealtimeSync(
        userId: String,
        transactionDao: TransactionDao,
        budgetDao: BudgetDao,
        categoryDao: CategoryDao,
        recurringDao: RecurringDao,
        accountDao: AccountDao,
        scope: CoroutineScope
    ) {
        stopRealtimeSync()

        // Realtime Transaction Sync
        try {
            transactionListener = firestore.collection("users")
                .document(userId)
                .collection("transactions")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    scope.launch(Dispatchers.IO) {
                        val transactions = snapshot.documents.mapNotNull { doc ->
                            try {
                                val id = (doc.get("id") as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: return@mapNotNull null
                                val title = doc.getString("title") ?: return@mapNotNull null
                                val amount = (doc.get("amount") as? Number)?.toDouble() ?: 0.0
                                val typeStr = doc.getString("type") ?: TransactionType.EXPENSE.name
                                val type = try { TransactionType.valueOf(typeStr) } catch (_: Exception) { TransactionType.EXPENSE }
                                val catStr = doc.getString("category") ?: Category.OTHER.name
                                val category = try { Category.valueOf(catStr) } catch (_: Exception) { Category.OTHER }
                                val methodStr = doc.getString("paymentMethod") ?: PaymentMethod.CASH.name
                                val method = try { PaymentMethod.valueOf(methodStr) } catch (_: Exception) { PaymentMethod.CASH }
                                val dateMillis = (doc.get("dateMillis") as? Number)?.toLong() ?: System.currentTimeMillis()
                                val note = doc.getString("note") ?: ""
                                val accountId = (doc.get("accountId") as? Number)?.toLong()
                                val accountName = doc.getString("accountName")

                                TransactionEntity(
                                    id = id,
                                    userId = userId,
                                    title = title,
                                    amount = amount,
                                    type = type,
                                    category = category,
                                    paymentMethod = method,
                                    dateMillis = dateMillis,
                                    note = note,
                                    accountId = accountId,
                                    accountName = accountName
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (transactions.isNotEmpty()) {
                            transactionDao.insertTransactions(transactions)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error setting up transaction sync", e)
        }

        // Realtime Budget Sync
        try {
            budgetListener = firestore.collection("users")
                .document(userId)
                .collection("budgets")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    scope.launch(Dispatchers.IO) {
                        val budgets = snapshot.documents.mapNotNull { doc ->
                            try {
                                val catStr = doc.getString("category") ?: doc.id
                                val category = try { Category.valueOf(catStr) } catch (_: Exception) { return@mapNotNull null }
                                val limit = (doc.get("monthlyLimit") as? Number)?.toDouble() ?: 0.0
                                BudgetEntity(userId = userId, category = category, monthlyLimit = limit)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (budgets.isNotEmpty()) {
                            budgetDao.insertBudgets(budgets)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error setting up budget sync", e)
        }

        // Realtime Custom Categories Sync
        try {
            categoryListener = firestore.collection("users")
                .document(userId)
                .collection("categories")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    scope.launch(Dispatchers.IO) {
                        val categories = snapshot.documents.mapNotNull { doc ->
                            try {
                                val name = doc.getString("name") ?: doc.id
                                val iconKey = doc.getString("iconKey") ?: "Category"
                                val colorHex = (doc.get("colorHex") as? Number)?.toLong() ?: 0xFF4CAF50
                                val isExpense = doc.getBoolean("isExpense") ?: true
                                CustomCategoryEntity(userId = userId, name = name, iconKey = iconKey, colorHex = colorHex, isExpense = isExpense)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (categories.isNotEmpty()) {
                            categoryDao.insertCategories(categories)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error setting up categories sync", e)
        }

        // Realtime Recurring Transactions Sync
        try {
            recurringListener = firestore.collection("users")
                .document(userId)
                .collection("recurring")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    scope.launch(Dispatchers.IO) {
                        val recurringList = snapshot.documents.mapNotNull { doc ->
                            try {
                                val id = (doc.get("id") as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: return@mapNotNull null
                                val title = doc.getString("title") ?: return@mapNotNull null
                                val amount = (doc.get("amount") as? Number)?.toDouble() ?: 0.0
                                val type = try { TransactionType.valueOf(doc.getString("type") ?: "EXPENSE") } catch (_: Exception) { TransactionType.EXPENSE }
                                val category = try { Category.valueOf(doc.getString("category") ?: "OTHER") } catch (_: Exception) { Category.OTHER }
                                val method = try { PaymentMethod.valueOf(doc.getString("paymentMethod") ?: "CASH") } catch (_: Exception) { PaymentMethod.CASH }
                                val interval = try { RecurrenceInterval.valueOf(doc.getString("interval") ?: "MONTHLY") } catch (_: Exception) { RecurrenceInterval.MONTHLY }
                                val nextDue = (doc.get("nextDueDateMillis") as? Number)?.toLong() ?: System.currentTimeMillis()
                                val lastProcessed = (doc.get("lastProcessedDateMillis") as? Number)?.toLong() ?: 0L
                                val isActive = doc.getBoolean("isActive") ?: true
                                val note = doc.getString("note") ?: ""

                                RecurringTransactionEntity(
                                    id = id,
                                    userId = userId,
                                    title = title,
                                    amount = amount,
                                    type = type,
                                    category = category,
                                    paymentMethod = method,
                                    interval = interval,
                                    nextDueDateMillis = nextDue,
                                    lastProcessedDateMillis = lastProcessed,
                                    isActive = isActive,
                                    note = note
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (recurringList.isNotEmpty()) {
                            recurringDao.insertRecurringList(recurringList)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error setting up recurring sync", e)
        }

        // Realtime Accounts Sync
        try {
            accountListener = firestore.collection("users")
                .document(userId)
                .collection("accounts")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    scope.launch(Dispatchers.IO) {
                        val accountsList = snapshot.documents.mapNotNull { doc ->
                            try {
                                val id = (doc.get("id") as? Number)?.toLong() ?: doc.id.toLongOrNull() ?: return@mapNotNull null
                                val name = doc.getString("name") ?: return@mapNotNull null
                                val type = try { AccountType.valueOf(doc.getString("type") ?: "BANK") } catch (_: Exception) { AccountType.BANK }
                                val institution = try { FinancialInstitution.valueOf(doc.getString("institution") ?: "OTHER") } catch (_: Exception) { FinancialInstitution.OTHER }
                                val accountNumber = doc.getString("accountNumber") ?: ""
                                val balance = (doc.get("balance") as? Number)?.toDouble() ?: 0.0
                                val colorHex = (doc.get("colorHex") as? Number)?.toLong() ?: institution.brandColorHex
                                val isDefault = doc.getBoolean("isDefault") ?: false
                                val note = doc.getString("note") ?: ""

                                AccountEntity(
                                    id = id,
                                    userId = userId,
                                    name = name,
                                    type = type,
                                    institution = institution,
                                    accountNumber = accountNumber,
                                    balance = balance,
                                    colorHex = colorHex,
                                    isDefault = isDefault,
                                    note = note
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (accountsList.isNotEmpty()) {
                            accountDao.insertAccounts(accountsList)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("FirestoreSync", "Error setting up accounts sync", e)
        }
    }

    fun stopRealtimeSync() {
        transactionListener?.remove()
        transactionListener = null
        budgetListener?.remove()
        budgetListener = null
        categoryListener?.remove()
        categoryListener = null
        recurringListener?.remove()
        recurringListener = null
        accountListener?.remove()
        accountListener = null
    }
}
