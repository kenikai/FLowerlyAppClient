package com.example.flowerlyapp.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowerlyapp.data.database.entities.FlowerEntity
import com.example.flowerlyapp.data.repository.FavoriteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteViewModel(private val context: Context) : ViewModel() {
    
    private val favoriteRepository = FavoriteRepository(context)
    
    private val _favoriteFlowers = MutableStateFlow<List<FlowerEntity>>(emptyList())
    val favoriteFlowers: StateFlow<List<FlowerEntity>> = _favoriteFlowers.asStateFlow()
    
    private val _favoriteCount = MutableStateFlow(0)
    val favoriteCount: StateFlow<Int> = _favoriteCount.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Загрузить избранные цветы пользователя
     */
    fun loadFavoriteFlowers(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                favoriteRepository.getFavoriteFlowers(userId).collect { flowers ->
                    _favoriteFlowers.value = flowers
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки избранного: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Загрузить количество избранных цветов
     */
    fun loadFavoriteCount(userId: String) {
        viewModelScope.launch {
            try {
                favoriteRepository.getFavoriteCount(userId).collect { count ->
                    _favoriteCount.value = count
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки количества избранного: ${e.message}"
            }
        }
    }
    
    /**
     * Добавить цветок в избранное
     */
    fun addToFavorites(userId: String, flowerId: String) {
        viewModelScope.launch {
            try {
                favoriteRepository.addToFavorites(userId, flowerId)
                // Обновляем количество
                loadFavoriteCount(userId)
            } catch (e: Exception) {
                _error.value = "Ошибка добавления в избранное: ${e.message}"
            }
        }
    }
    
    /**
     * Удалить цветок из избранного
     */
    fun removeFromFavorites(userId: String, flowerId: String) {
        viewModelScope.launch {
            try {
                favoriteRepository.removeFromFavorites(userId, flowerId)
                // Обновляем количество
                loadFavoriteCount(userId)
            } catch (e: Exception) {
                _error.value = "Ошибка удаления из избранного: ${e.message}"
            }
        }
    }
    
    /**
     * Переключить статус избранного
     */
    fun toggleFavorite(userId: String, flowerId: String) {
        android.util.Log.d("FavoriteViewModel", "=== TOGGLE FAVORITE ===")
        android.util.Log.d("FavoriteViewModel", "userId: $userId")
        android.util.Log.d("FavoriteViewModel", "flowerId: $flowerId")
        
        viewModelScope.launch {
            try {
                android.util.Log.d("FavoriteViewModel", "Вызываем favoriteRepository.toggleFavorite($userId, $flowerId)")
                val result = favoriteRepository.toggleFavorite(userId, flowerId)
                android.util.Log.d("FavoriteViewModel", "toggleFavorite результат: $result")
                
                // Обновляем список избранных цветов
                android.util.Log.d("FavoriteViewModel", "Обновляем список избранных цветов")
                loadFavoriteFlowers(userId)
                // Обновляем количество
                android.util.Log.d("FavoriteViewModel", "Обновляем количество избранных")
                loadFavoriteCount(userId)
            } catch (e: Exception) {
                android.util.Log.e("FavoriteViewModel", "Ошибка переключения избранного: ${e.message}", e)
                _error.value = "Ошибка переключения избранного: ${e.message}"
            }
        }
    }
    
    /**
     * Проверить, добавлен ли цветок в избранное
     */
    fun isFavorite(userId: String, flowerId: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val isFavorite = favoriteRepository.isFavorite(userId, flowerId)
                callback(isFavorite)
            } catch (e: Exception) {
                _error.value = "Ошибка проверки избранного: ${e.message}"
                callback(false)
            }
        }
    }
    
    /**
     * Очистить все избранное
     */
    fun clearAllFavorites(userId: String) {
        viewModelScope.launch {
            try {
                favoriteRepository.clearAllFavorites(userId)
                _favoriteFlowers.value = emptyList()
                _favoriteCount.value = 0
            } catch (e: Exception) {
                _error.value = "Ошибка очистки избранного: ${e.message}"
            }
        }
    }
    
    /**
     * Очистить ошибки
     */
    fun clearError() {
        _error.value = null
    }
}
