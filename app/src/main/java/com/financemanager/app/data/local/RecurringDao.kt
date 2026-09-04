package com.financemanager.app.data.local

import androidx.room.*
import com.financemanager.app.data.model.RecurringTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringDao {

    @Query("SELECT * FROM recurring_transactions WHERE userId = :userId ORDER BY nextDueDateMillis ASC")
    fun getAllRecurring(userId: String): Flow<List<RecurringTransactionEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE userId = :userId AND isActive = 1")
    suspend fun getActiveRecurring(userId: String): List<RecurringTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurring(recurring: RecurringTransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringList(list: List<RecurringTransactionEntity>)

    @Update
    suspend fun updateRecurring(recurring: RecurringTransactionEntity)

    @Delete
    suspend fun deleteRecurring(recurring: RecurringTransactionEntity)

    @Query("DELETE FROM recurring_transactions WHERE userId = :userId AND id = :id")
    suspend fun deleteRecurringById(userId: String, id: Long)
}
