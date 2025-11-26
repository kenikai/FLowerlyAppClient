package com.example.flowerlyapp.data.repository

import android.content.Context
import com.example.flowerlyapp.data.database.FlowerlyDatabase
import com.example.flowerlyapp.data.database.entities.FavoriteEntity
import com.example.flowerlyapp.data.database.entities.FlowerEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Репозиторий для работы с избранными цветами
 */
class FavoriteRepository(private val context: Context) {
    
    private val database = FlowerlyDatabase.getDatabase(context)
    private val favoriteDao = database.favoriteDao()
    
    /**
     * Получить все избранные цветы пользователя
     */
    fun getFavoriteFlowers(userId: String): Flow<List<FlowerEntity>> {
        return favoriteDao.getFavoriteFlowers(userId)
    }
    
    /**
     * Проверить, добавлен ли цветок в избранное
     */
    suspend fun isFavorite(userId: String, flowerId: String): Boolean {
        return withContext(Dispatchers.IO) {
            favoriteDao.isFavorite(userId, flowerId)
        }
    }
    
    /**
     * Получить количество избранных цветов
     */
    fun getFavoriteCount(userId: String): Flow<Int> {
        return favoriteDao.getFavoriteCountFlow(userId)
    }
    
    /**
     * Добавить цветок в избранное
     */
    suspend fun addToFavorites(userId: String, flowerId: String) {
        withContext(Dispatchers.IO) {
            val favorite = FavoriteEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                flowerId = flowerId,
                createdAt = System.currentTimeMillis()
            )
            favoriteDao.insertFavorite(favorite)
        }
    }
    
    /**
     * Удалить цветок из избранного
     */
    suspend fun removeFromFavorites(userId: String, flowerId: String) {
        withContext(Dispatchers.IO) {
            favoriteDao.deleteFavoriteByUserAndFlower(userId, flowerId)
        }
    }
    
    /**
     * Переключить статус избранного (добавить/удалить)
     */
    suspend fun toggleFavorite(userId: String, flowerId: String): Boolean {
        return withContext(Dispatchers.IO) {
            android.util.Log.d("FavoriteRepository", "=== TOGGLE FAVORITE REPOSITORY ===")
            android.util.Log.d("FavoriteRepository", "userId: $userId, flowerId: $flowerId")
            
            val isCurrentlyFavorite = favoriteDao.isFavorite(userId, flowerId)
            android.util.Log.d("FavoriteRepository", "isCurrentlyFavorite: $isCurrentlyFavorite")
            
            if (isCurrentlyFavorite) {
                android.util.Log.d("FavoriteRepository", "Удаляем из избранного")
                favoriteDao.deleteFavoriteByUserAndFlower(userId, flowerId)
                android.util.Log.d("FavoriteRepository", "Удалено из избранного")
                false
            } else {
                android.util.Log.d("FavoriteRepository", "Добавляем в избранное")
                val favorite = FavoriteEntity(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    flowerId = flowerId,
                    createdAt = System.currentTimeMillis()
                )
                android.util.Log.d("FavoriteRepository", "Создана FavoriteEntity: $favorite")
                favoriteDao.insertFavorite(favorite)
                android.util.Log.d("FavoriteRepository", "Добавлено в избранное")
                true
            }
        }
    }
    
    /**
     * Очистить все избранное пользователя
     */
    suspend fun clearAllFavorites(userId: String) {
        withContext(Dispatchers.IO) {
            favoriteDao.deleteAllFavoritesByUser(userId)
        }
    }
}
