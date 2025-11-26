package com.example.flowerlyapp.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowerlyapp.data.database.dao.CartItemWithFlowerInfo
import com.example.flowerlyapp.data.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel(private val context: Context) : ViewModel() {
    
    private val cartRepository = CartRepository(context)
    
    private val _cartItems = MutableStateFlow<List<CartItemWithFlowerInfo>>(emptyList())
    val cartItems: StateFlow<List<CartItemWithFlowerInfo>> = _cartItems.asStateFlow()
    
    private val _cartItemCount = MutableStateFlow(0)
    val cartItemCount: StateFlow<Int> = _cartItemCount.asStateFlow()
    
    private val _cartTotal = MutableStateFlow(0.0)
    val cartTotal: StateFlow<Double> = _cartTotal.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Загрузить товары в корзине
     */
    fun loadCartItems(userId: String) {
        android.util.Log.d("CartViewModel", "=== ЗАГРУЗКА ТОВАРОВ КОРЗИНЫ ===")
        android.util.Log.d("CartViewModel", "userId: $userId")
        viewModelScope.launch {
            try {
                _isLoading.value = true
                android.util.Log.d("CartViewModel", "Начинаем загрузку товаров корзины")
                cartRepository.getCartItemsWithFlowerInfo(userId).collect { items ->
                    android.util.Log.d("CartViewModel", "Получены товары корзины: ${items.size}")
                    android.util.Log.d("CartViewModel", "Детали товаров: ${items.map { it.flowerName }}")
                    _cartItems.value = items
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                android.util.Log.e("CartViewModel", "Ошибка загрузки корзины: ${e.message}", e)
                _error.value = "Ошибка загрузки корзины: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Загрузить количество товаров в корзине
     */
    fun loadCartItemCount(userId: String) {
        viewModelScope.launch {
            try {
                cartRepository.getCartItemCount(userId).collect { count ->
                    _cartItemCount.value = count
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки количества товаров: ${e.message}"
            }
        }
    }
    
    /**
     * Загрузить общую стоимость корзины
     */
    fun loadCartTotal(userId: String) {
        viewModelScope.launch {
            try {
                cartRepository.getCartTotal(userId).collect { total ->
                    _cartTotal.value = total ?: 0.0
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки общей стоимости: ${e.message}"
            }
        }
    }
    
    /**
     * Добавить товар в корзину
     */
    fun addToCart(userId: String, flowerId: String, quantity: Int, unitPrice: Double) {
        android.util.Log.d("CartViewModel", "=== ДОБАВЛЕНИЕ В КОРЗИНУ ===")
        android.util.Log.d("CartViewModel", "userId: $userId, flowerId: $flowerId, quantity: $quantity, unitPrice: $unitPrice")
        viewModelScope.launch {
            try {
                android.util.Log.d("CartViewModel", "Вызываем cartRepository.addToCart")
                cartRepository.addToCart(userId, flowerId, quantity, unitPrice)
                android.util.Log.d("CartViewModel", "Товар добавлен в корзину, обновляем данные")
                // Обновляем данные корзины
                loadCartItemCount(userId)
                loadCartTotal(userId)
                loadCartItems(userId) // Также обновляем список товаров
            } catch (e: Exception) {
                android.util.Log.e("CartViewModel", "Ошибка добавления в корзину: ${e.message}", e)
                _error.value = "Ошибка добавления в корзину: ${e.message}"
            }
        }
    }
    
    /**
     * Обновить количество товара в корзине
     */
    fun updateCartItemQuantity(userId: String, flowerId: String, newQuantity: Int) {
        viewModelScope.launch {
            try {
                cartRepository.updateCartItemQuantity(userId, flowerId, newQuantity)
                // Обновляем данные корзины
                loadCartItems(userId)
                loadCartItemCount(userId)
                loadCartTotal(userId)
            } catch (e: Exception) {
                _error.value = "Ошибка обновления количества: ${e.message}"
            }
        }
    }
    
    /**
     * Удалить товар из корзины
     */
    fun removeFromCart(userId: String, flowerId: String) {
        viewModelScope.launch {
            try {
                cartRepository.removeFromCart(userId, flowerId)
                // Обновляем данные корзины
                loadCartItems(userId)
                loadCartItemCount(userId)
                loadCartTotal(userId)
            } catch (e: Exception) {
                _error.value = "Ошибка удаления из корзины: ${e.message}"
            }
        }
    }
    
    /**
     * Очистить всю корзину
     */
    fun clearCart(userId: String) {
        viewModelScope.launch {
            try {
                cartRepository.clearCart(userId)
                _cartItems.value = emptyList()
                _cartItemCount.value = 0
                _cartTotal.value = 0.0
            } catch (e: Exception) {
                _error.value = "Ошибка очистки корзины: ${e.message}"
            }
        }
    }
    
    /**
     * Получить общее количество товаров в корзине
     */
    fun getTotalItemCount(): Int {
        return _cartItems.value.sumOf { it.quantity }
    }
    
    /**
     * Очистить ошибки
     */
    fun clearError() {
        _error.value = null
    }
}
