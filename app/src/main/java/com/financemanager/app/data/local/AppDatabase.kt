package com.financemanager.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.financemanager.app.data.model.BudgetEntity
import com.financemanager.app.data.model.CustomCategoryEntity
import com.financemanager.app.data.model.AccountEntity
import com.financemanager.app.data.model.RecurringTransactionEntity
import com.financemanager.app.data.model.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        CustomCategoryEntity::class,
        RecurringTransactionEntity::class,
        AccountEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryDao(): CategoryDao
    abstract fun recurringDao(): RecurringDao
    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_manager_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
