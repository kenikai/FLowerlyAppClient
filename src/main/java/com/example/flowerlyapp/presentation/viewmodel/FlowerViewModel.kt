package com.example.flowerlyapp.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowerlyapp.data.database.entities.FlowerEntity
import com.example.flowerlyapp.data.repository.FlowerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FlowerViewModel(private val context: Context) : ViewModel() {
    
    private val flowerRepository = FlowerRepository(context)
    
    private val _popularFlowers = MutableStateFlow<List<FlowerEntity>>(emptyList())
    val popularFlowers: StateFlow<List<FlowerEntity>> = _popularFlowers.asStateFlow()
    
    private val _allFlowers = MutableStateFlow<List<FlowerEntity>>(emptyList())
    val allFlowers: StateFlow<List<FlowerEntity>> = _allFlowers.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    init {
        loadPopularFlowers()
        loadAllFlowers()
    }
    
    fun loadPopularFlowers() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                flowerRepository.getPopularFlowers().collect { flowers ->
                    _popularFlowers.value = flowers
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки популярных цветов: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun loadAllFlowers() {
        viewModelScope.launch {
            try {
                android.util.Log.d("FlowerViewModel", "Загружаем все цветы")
                _isLoading.value = true
                flowerRepository.getAllAvailableFlowers().collect { flowers ->
                    android.util.Log.d("FlowerViewModel", "Загружено ${flowers.size} цветов")
                    android.util.Log.d("FlowerViewModel", "flowers.isEmpty() = ${flowers.isEmpty()}")
                    flowers.forEach { flower ->
                        android.util.Log.d("FlowerViewModel", "Цветок: ${flower.name}, imageResourceId: ${flower.imageResourceId}")
                    }
                    _allFlowers.value = flowers
                    _isLoading.value = false
                    android.util.Log.d("FlowerViewModel", "allFlowers.value обновлен: ${_allFlowers.value.size} цветов")
                }
            } catch (e: Exception) {
                android.util.Log.e("FlowerViewModel", "Ошибка загрузки цветов", e)
                _error.value = "Ошибка загрузки цветов: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun searchFlowers(query: String) {
        viewModelScope.launch {
            try {
                android.util.Log.d("FlowerViewModel", "Начинаем поиск: '$query'")
                _isLoading.value = true
                flowerRepository.searchFlowers(query).collect { flowers ->
                    android.util.Log.d("FlowerViewModel", "Результаты поиска: ${flowers.size} цветов")
                    android.util.Log.d("FlowerViewModel", "flowers.isEmpty(): ${flowers.isEmpty()}")
                    flowers.forEach { flower ->
                        android.util.Log.d("FlowerViewModel", "Найден цветок: ${flower.name}")
                    }
                    _allFlowers.value = flowers
                    _isLoading.value = false
                    android.util.Log.d("FlowerViewModel", "allFlowers.value обновлен: ${_allFlowers.value.size} цветов")
                }
            } catch (e: Exception) {
                android.util.Log.e("FlowerViewModel", "Ошибка поиска", e)
                _error.value = "Ошибка поиска: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun getFlowersByPriceRange(minPrice: Double, maxPrice: Double) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                flowerRepository.getFlowersByPriceRange(minPrice, maxPrice).collect { flowers ->
                    _allFlowers.value = flowers
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Ошибка фильтрации по цене: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    fun clearError() {
        _error.value = null
    }
}

