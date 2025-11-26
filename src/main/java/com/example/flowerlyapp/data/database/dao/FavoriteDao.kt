package com.example.flowerlyapp.data.database.dao

import androidx.room.*
import com.example.flowerlyapp.data.database.entities.FavoriteEntity
import com.example.flowerlyapp.data.database.entities.FlowerEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object для работы с избранными цветами
 */
@Dao
interface FavoriteDao {
    
    @Query("""
        SELECT f.* FROM flowers f
        INNER JOIN favorites fav ON f.id = fav.flowerId
        WHERE fav.userId = :userId
        ORDER BY fav.createdAt DESC
    """)
    fun getFavoriteFlowers(userId: String): Flow<List<FlowerEntity>>
    
    @Query("SELECT * FROM favorites WHERE userId = :userId AND flowerId = :flowerId")
    suspend fun getFavorite(userId: String, flowerId: String): FavoriteEntity?
    
    @Query("SELECT COUNT(*) FROM favorites WHERE userId = :userId")
    suspend fun getFavoriteCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM favorites WHERE userId = :userId")
    fun getFavoriteCountFlow(userId: String): Flow<Int>
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND flowerId = :flowerId)")
    suspend fun isFavorite(userId: String, flowerId: String): Boolean
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)
    
    @Delete
    suspend fun deleteFavorite(favorite: FavoriteEntity)
    
    @Query("DELETE FROM favorites WHERE userId = :userId AND flowerId = :flowerId")
    suspend fun deleteFavoriteByUserAndFlower(userId: String, flowerId: String)
    
    @Query("DELETE FROM favorites WHERE userId = :userId")
    suspend fun deleteAllFavoritesByUser(userId: String)
    
    @Query("DELETE FROM favorites WHERE id = :favoriteId")
    suspend fun deleteFavoriteById(favoriteId: String)
}
