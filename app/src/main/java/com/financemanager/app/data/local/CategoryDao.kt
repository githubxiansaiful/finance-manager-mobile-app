package com.financemanager.app.data.local

import androidx.room.*
import com.financemanager.app.data.model.CustomCategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM custom_categories WHERE userId = :userId ORDER BY name ASC")
    fun getAllCategories(userId: String): Flow<List<CustomCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CustomCategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CustomCategoryEntity>)

    @Delete
    suspend fun deleteCategory(category: CustomCategoryEntity)

    @Query("DELETE FROM custom_categories WHERE userId = :userId AND name = :name")
    suspend fun deleteCategoryByName(userId: String, name: String)
}
