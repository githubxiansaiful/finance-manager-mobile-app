package com.financemanager.app.data.local

import androidx.room.*
import com.financemanager.app.data.model.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE userId = :userId ORDER BY isDefault DESC, balance DESC, id ASC")
    fun getAllAccounts(userId: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE userId = :userId AND id = :id LIMIT 1")
    suspend fun getAccountById(userId: String, id: Long): AccountEntity?

    @Query("SELECT * FROM accounts WHERE userId = :userId AND isDefault = 1 LIMIT 1")
    suspend fun getDefaultAccount(userId: String): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Update
    suspend fun updateAccount(account: AccountEntity)

    @Query("DELETE FROM accounts WHERE userId = :userId AND id = :id")
    suspend fun deleteAccount(userId: String, id: Long)

    @Query("UPDATE accounts SET balance = :newBalance WHERE userId = :userId AND id = :id")
    suspend fun updateBalance(userId: String, id: Long, newBalance: Double)

    @Query("UPDATE accounts SET balance = balance + :amount WHERE userId = :userId AND id = :id")
    suspend fun adjustBalance(userId: String, id: Long, amount: Double)

    @Query("SELECT SUM(balance) FROM accounts WHERE userId = :userId")
    fun getTotalNetWorth(userId: String): Flow<Double?>
}
