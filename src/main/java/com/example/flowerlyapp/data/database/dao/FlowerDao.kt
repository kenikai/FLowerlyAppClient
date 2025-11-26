package com.example.flowerlyapp.data.database.dao

import androidx.room.*
import com.example.flowerlyapp.data.database.entities.FlowerEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object для работы с цветами
 */
@Dao
interface FlowerDao {
    
    @Query("SELECT * FROM flowers WHERE isAvailable = 1 ORDER BY isPopular DESC, name ASC")
    fun getAllAvailableFlowers(): Flow<List<FlowerEntity>>
    
    @Query("SELECT * FROM flowers WHERE isPopular = 1 AND isAvailable = 1 ORDER BY name ASC")
    fun getPopularFlowers(): Flow<List<FlowerEntity>>
    
    @Query("SELECT * FROM flowers WHERE categoryId = :categoryId AND isAvailable = 1 ORDER BY name ASC")
    fun getFlowersByCategory(categoryId: String): Flow<List<FlowerEntity>>
    
    @Query("SELECT * FROM flowers WHERE id = :flowerId")
    suspend fun getFlowerById(flowerId: String): FlowerEntity?
    
    @Query("SELECT * FROM flowers WHERE (LOWER(name) LIKE LOWER('%' || :searchQuery || '%') OR LOWER(description) LIKE LOWER('%' || :searchQuery || '%')) AND isAvailable = 1 ORDER BY name ASC")
    fun searchFlowers(searchQuery: String): Flow<List<FlowerEntity>>
    
    @Query("SELECT * FROM flowers WHERE price BETWEEN :minPrice AND :maxPrice AND isAvailable = 1 ORDER BY price ASC")
    fun getFlowersByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<FlowerEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlower(flower: FlowerEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlowers(flowers: List<FlowerEntity>)
    
    @Update
    suspend fun updateFlower(flower: FlowerEntity)
    
    @Delete
    suspend fun deleteFlower(flower: FlowerEntity)
    
    @Query("DELETE FROM flowers WHERE id = :flowerId")
    suspend fun deleteFlowerById(flowerId: String)
    
    @Query("UPDATE flowers SET stockQuantity = :quantity WHERE id = :flowerId")
    suspend fun updateStockQuantity(flowerId: String, quantity: Int)
    
    @Query("UPDATE flowers SET isAvailable = :isAvailable WHERE id = :flowerId")
    suspend fun updateAvailability(flowerId: String, isAvailable: Boolean)
}

