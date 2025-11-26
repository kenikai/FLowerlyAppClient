package com.example.flowerlyapp.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowerlyapp.data.database.entities.OrderEntity
import com.example.flowerlyapp.data.database.entities.OrderItemEntity
import com.example.flowerlyapp.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel для управления заказами
 */
class OrderViewModel(private val context: Context) : ViewModel() {
    
    private val orderRepository = OrderRepository(context)
    
    private val _orders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val orders: StateFlow<List<OrderEntity>> = _orders.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Загрузить заказы пользователя
     */
    fun loadOrders(userId: String) {
        android.util.Log.d("OrderViewModel", "=== ЗАГРУЗКА ЗАКАЗОВ ===")
        android.util.Log.d("OrderViewModel", "userId: $userId")
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                
                android.util.Log.d("OrderViewModel", "Начинаем загрузку заказов...")
                orderRepository.getOrdersByUser(userId).collect { ordersList ->
                    android.util.Log.d("OrderViewModel", "Получено заказов: ${ordersList.size}")
                    _orders.value = ordersList
                    _isLoading.value = false
                }
            } catch (e: kotlinx.coroutines.CancellationException) {
                android.util.Log.d("OrderViewModel", "Загрузка заказов отменена")
                // Не показываем ошибку для отмены
            } catch (e: Exception) {
                android.util.Log.e("OrderViewModel", "Ошибка загрузки заказов: ${e.message}", e)
                _error.value = "Ошибка загрузки заказов: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Создать заказ из корзины
     */
    fun createOrderFromCart(
        userId: String,
        cartItems: List<com.example.flowerlyapp.data.database.dao.CartItemWithFlowerInfo>,
        deliveryAddress: String? = null,
        notes: String? = null
    ) {
        android.util.Log.d("OrderViewModel", "=== СОЗДАНИЕ ЗАКАЗА ===")
        android.util.Log.d("OrderViewModel", "userId: $userId")
        android.util.Log.d("OrderViewModel", "cartItems.size: ${cartItems.size}")
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                // Вычисляем общую сумму
                val totalAmount = cartItems.sumOf { it.totalPrice }
                android.util.Log.d("OrderViewModel", "totalAmount: $totalAmount")
                
                // Создаем заказ
                android.util.Log.d("OrderViewModel", "Создаем заказ...")
                val orderId = orderRepository.createOrder(
                    userId = userId,
                    totalAmount = totalAmount,
                    deliveryAddress = deliveryAddress,
                    notes = notes
                )
                android.util.Log.d("OrderViewModel", "Заказ создан с ID: $orderId")
                
                // Добавляем позиции заказа
                cartItems.forEach { cartItem ->
                    android.util.Log.d("OrderViewModel", "Добавляем позицию: ${cartItem.flowerName}, количество: ${cartItem.quantity}")
                    orderRepository.addOrderItem(
                        orderId = orderId,
                        flowerId = cartItem.flowerId,
                        quantity = cartItem.quantity,
                        unitPrice = cartItem.unitPrice
                    )
                }
                android.util.Log.d("OrderViewModel", "Все позиции добавлены")
                
                _isLoading.value = false
                
                // Перезагружаем список заказов
                loadOrders(userId)
                
            } catch (e: kotlinx.coroutines.CancellationException) {
                android.util.Log.d("OrderViewModel", "Создание заказа отменено")
                // Не показываем ошибку для отмены
            } catch (e: Exception) {
                android.util.Log.e("OrderViewModel", "Ошибка создания заказа: ${e.message}", e)
                _error.value = "Ошибка создания заказа: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Обновить статус заказа
     */
    fun updateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            try {
                orderRepository.updateOrderStatus(orderId, status)
                // Перезагружаем список заказов
                val currentOrders = _orders.value
                val updatedOrders = currentOrders.map { order ->
                    if (order.id == orderId) {
                        order.copy(status = status, updatedAt = System.currentTimeMillis())
                    } else {
                        order
                    }
                }
                _orders.value = updatedOrders
            } catch (e: kotlinx.coroutines.CancellationException) {
                android.util.Log.d("OrderViewModel", "Обновление статуса отменено")
                // Не показываем ошибку для отмены
            } catch (e: Exception) {
                android.util.Log.e("OrderViewModel", "Ошибка обновления статуса: ${e.message}", e)
                _error.value = "Ошибка обновления статуса: ${e.message}"
            }
        }
    }
    
    /**
     * Получить позиции заказа
     */
    suspend fun getOrderItems(orderId: String): List<OrderItemEntity> {
        return orderRepository.getOrderItems(orderId)
    }
    
    /**
     * Очистить ошибки
     */
    fun clearError() {
        _error.value = null
    }
}
