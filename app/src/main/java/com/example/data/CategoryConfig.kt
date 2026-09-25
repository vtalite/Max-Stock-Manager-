package com.example.data

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "category_configs")
data class CategoryConfig(
    @PrimaryKey val categoryName: String,
    val minQuantity: Int = 2,
    val unitDescription: String = "unidades",
    val notes: String = ""
)

@Dao
interface CategoryConfigDao {
    @Query("SELECT * FROM category_configs ORDER BY categoryName ASC")
    fun getAllCategoryConfigs(): Flow<List<CategoryConfig>>

    @Query("SELECT * FROM category_configs ORDER BY categoryName ASC")
    suspend fun getAllCategoryConfigsSync(): List<CategoryConfig>

    @Query("SELECT * FROM category_configs WHERE categoryName = :categoryName LIMIT 1")
    suspend fun getByCategoryName(categoryName: String): CategoryConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(config: CategoryConfig)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(configs: List<CategoryConfig>)

    @Query("UPDATE category_configs SET minQuantity = :minQuantity WHERE categoryName = :categoryName")
    suspend fun updateMinQuantity(categoryName: String, minQuantity: Int)

    @Query("UPDATE wines SET minQuantity = :minQuantity WHERE category = :categoryName")
    suspend fun updateAllWinesMinQuantityForCategory(categoryName: String, minQuantity: Int)

    @Query("DELETE FROM category_configs WHERE categoryName = :categoryName")
    suspend fun deleteCategory(categoryName: String)
}
