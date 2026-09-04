package com.financemanager.app.data.local

import androidx.room.*
import com.financemanager.app.data.model.BudgetEntity
import com.financemanager.app.data.model.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE userId = :userId")
    fun getAllBudgets(userId: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE userId = :userId AND category = :category")
    fun getBudgetForCategory(userId: String, category: Category): Flow<BudgetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudget(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgets(budgets: List<BudgetEntity>)

    @Delete
    suspend fun deleteBudget(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE userId = :userId AND category = :category")
    suspend fun deleteBudgetByCategory(userId: String, category: Category)

    @Query("SELECT COUNT(*) FROM budgets WHERE userId = :userId")
    suspend fun getBudgetCount(userId: String): Int
}
