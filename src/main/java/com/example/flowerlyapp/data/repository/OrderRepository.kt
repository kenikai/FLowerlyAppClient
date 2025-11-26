package com.example.flowerlyapp.data.repository

import android.content.Context
import com.example.flowerlyapp.data.database.FlowerlyDatabase
import com.example.flowerlyapp.data.database.entities.OrderEntity
import com.example.flowerlyapp.data.database.entities.OrderItemEntity
import com.example.flowerlyapp.data.database.entities.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Репозиторий для работы с заказами
 */
class OrderRepository(private val context: Context) {
    
    private val database = FlowerlyDatabase.getDatabase(context)
    private val orderDao = database.orderDao()
    private val userDao = database.userDao()
    
    /**
     * Получить все заказы пользователя
     */
    fun getOrdersByUser(userId: String): Flow<List<OrderEntity>> {
        android.util.Log.d("OrderRepository", "=== ЗАГРУЗКА ЗАКАЗОВ ПОЛЬЗОВАТЕЛЯ ===")
        android.util.Log.d("OrderRepository", "userId: $userId")
        android.util.Log.d("OrderRepository", "database: $database")
        android.util.Log.d("OrderRepository", "orderDao: $orderDao")
        
        return try {
            orderDao.getOrdersByUser(userId)
        } catch (e: kotlinx.coroutines.CancellationException) {
            android.util.Log.d("OrderRepository", "Получение заказов отменено")
            throw e
        } catch (e: Exception) {
            android.util.Log.e("OrderRepository", "Ошибка получения заказов: ${e.message}", e)
            throw e
        }
    }
    
    /**
     * Получить заказ по ID
     */
    suspend fun getOrderById(orderId: String): OrderEntity? {
        return withContext(Dispatchers.IO) {
            orderDao.getOrderById(orderId)
        }
    }
    
    /**
     * Получить заказы по статусу
     */
    fun getOrdersByStatus(status: String): Flow<List<OrderEntity>> {
        return orderDao.getOrdersByStatus(status)
    }
    
    /**
     * Создать новый заказ
     */
    suspend fun createOrder(
        userId: String,
        totalAmount: Double,
        deliveryAddress: String? = null,
        notes: String? = null
    ): String {
        return withContext(Dispatchers.IO) {
            // Проверяем, существует ли пользователь
            val existingUser = userDao.getUserById(userId)
            if (existingUser == null) {
                android.util.Log.d("OrderRepository", "Пользователь не найден, создаем...")
                
                // Получаем данные пользователя из TokenManager
                val firstName = com.example.flowerlyapp.data.network.TokenManager.getUserFirstName() ?: "Пользователь"
                val lastName = com.example.flowerlyapp.data.network.TokenManager.getUserLastName() ?: ""
                val email = com.example.flowerlyapp.data.network.TokenManager.getUserEmail() ?: "user@example.com"
                
                // Создаем пользователя с реальными данными
                val user = UserEntity(
                    id = userId,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    passwordHash = ""
                )
                userDao.insertUser(user)
                android.util.Log.d("OrderRepository", "Пользователь создан: $userId, имя: $firstName $lastName, email: $email")
            }
            
            val orderId = UUID.randomUUID().toString()
            val order = OrderEntity(
                id = orderId,
                userId = userId,
                totalAmount = totalAmount,
                status = "pending",
                deliveryAddress = deliveryAddress,
                notes = notes,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            orderDao.insertOrder(order)
            orderId
        }
    }
    
    /**
     * Добавить позицию в заказ
     */
    suspend fun addOrderItem(
        orderId: String,
        flowerId: String,
        quantity: Int,
        unitPrice: Double
    ) {
        withContext(Dispatchers.IO) {
            val orderItem = OrderItemEntity(
                id = UUID.randomUUID().toString(),
                orderId = orderId,
                flowerId = flowerId,
                quantity = quantity,
                unitPrice = unitPrice,
                totalPrice = quantity * unitPrice
            )
            orderDao.insertOrderItem(orderItem)
        }
    }
    
    /**
     * Получить позиции заказа
     */
    suspend fun getOrderItems(orderId: String): List<OrderItemEntity> {
        return withContext(Dispatchers.IO) {
            orderDao.getOrderItems(orderId)
        }
    }
    
    /**
     * Обновить статус заказа
     */
    suspend fun updateOrderStatus(orderId: String, status: String) {
        withContext(Dispatchers.IO) {
            val order = orderDao.getOrderById(orderId)
            if (order != null) {
                val updatedOrder = order.copy(
                    status = status,
                    updatedAt = System.currentTimeMillis()
                )
                orderDao.updateOrder(updatedOrder)
            }
        }
    }
    
    /**
     * Удалить заказ
     */
    suspend fun deleteOrder(orderId: String) {
        withContext(Dispatchers.IO) {
            orderDao.deleteOrderById(orderId)
        }
    }
}
