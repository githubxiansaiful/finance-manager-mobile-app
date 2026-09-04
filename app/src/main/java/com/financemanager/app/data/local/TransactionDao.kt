package com.financemanager.app.data.local

import androidx.room.*
import com.financemanager.app.data.model.Category
import com.financemanager.app.data.model.TransactionEntity
import com.financemanager.app.data.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY dateMillis DESC")
    fun getAllTransactions(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY dateMillis DESC LIMIT :limit")
    fun getRecentTransactions(userId: String, limit: Int = 10): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId AND dateMillis BETWEEN :startMillis AND :endMillis ORDER BY dateMillis DESC")
    fun getTransactionsBetween(userId: String, startMillis: Long, endMillis: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id AND userId = :userId")
    suspend fun getTransactionById(userId: String, id: Long): TransactionEntity?

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = :type")
    fun getTotalByType(userId: String, type: TransactionType): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND type = :type AND dateMillis BETWEEN :startMillis AND :endMillis")
    fun getTotalByTypeBetween(userId: String, type: TransactionType, startMillis: Long, endMillis: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE userId = :userId AND category = :category AND type = 'EXPENSE' AND dateMillis BETWEEN :startMillis AND :endMillis")
    fun getExpenseForCategoryBetween(userId: String, category: Category, startMillis: Long, endMillis: Long): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id AND userId = :userId")
    suspend fun deleteTransactionById(userId: String, id: Long)

    @Query("SELECT COUNT(*) FROM transactions WHERE userId = :userId")
    suspend fun getTransactionCount(userId: String): Int

    @Query("SELECT * FROM transactions WHERE userId = :userId")
    suspend fun getAllTransactionsSync(userId: String): List<TransactionEntity>
}
