package com.example.flowerlyapp.data.repository

import android.content.Context
import com.example.flowerlyapp.data.database.FlowerlyDatabase
import com.example.flowerlyapp.data.database.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Репозиторий для работы с цветами и категориями
 */
class FlowerRepository(private val context: Context) {
    
    private val database = FlowerlyDatabase.getDatabase(context)
    private val flowerDao = database.flowerDao()
    private val categoryDao = database.categoryDao()
    private val orderDao = database.orderDao()
    
    // === Работа с цветами ===
    
    fun getAllAvailableFlowers(): Flow<List<FlowerEntity>> {
        return flowerDao.getAllAvailableFlowers()
    }
    
    fun getPopularFlowers(): Flow<List<FlowerEntity>> {
        return flowerDao.getPopularFlowers()
    }
    
    fun getFlowersByCategory(categoryId: String): Flow<List<FlowerEntity>> {
        return flowerDao.getFlowersByCategory(categoryId)
    }
    
    suspend fun getFlowerById(flowerId: String): FlowerEntity? {
        return withContext(Dispatchers.IO) {
            flowerDao.getFlowerById(flowerId)
        }
    }
    
    fun searchFlowers(query: String): Flow<List<FlowerEntity>> {
        android.util.Log.d("FlowerRepository", "Поиск в репозитории: '$query'")
        return flowerDao.searchFlowers(query)
    }
    
    fun getFlowersByPriceRange(minPrice: Double, maxPrice: Double): Flow<List<FlowerEntity>> {
        return flowerDao.getFlowersByPriceRange(minPrice, maxPrice)
    }
    
    suspend fun insertFlower(flower: FlowerEntity) {
        withContext(Dispatchers.IO) {
            flowerDao.insertFlower(flower)
        }
    }
    
    suspend fun insertFlowers(flowers: List<FlowerEntity>) {
        withContext(Dispatchers.IO) {
            flowerDao.insertFlowers(flowers)
        }
    }
    
    suspend fun updateFlower(flower: FlowerEntity) {
        withContext(Dispatchers.IO) {
            flowerDao.updateFlower(flower)
        }
    }
    
    suspend fun deleteFlower(flowerId: String) {
        withContext(Dispatchers.IO) {
            flowerDao.deleteFlowerById(flowerId)
        }
    }
    
    suspend fun updateStockQuantity(flowerId: String, quantity: Int) {
        withContext(Dispatchers.IO) {
            flowerDao.updateStockQuantity(flowerId, quantity)
        }
    }
    
    suspend fun updateAvailability(flowerId: String, isAvailable: Boolean) {
        withContext(Dispatchers.IO) {
            flowerDao.updateAvailability(flowerId, isAvailable)
        }
    }
    
    // === Работа с категориями ===
    
    fun getAllActiveCategories(): Flow<List<CategoryEntity>> {
        return categoryDao.getAllActiveCategories()
    }
    
    suspend fun getAllCategories(): List<CategoryEntity> {
        return withContext(Dispatchers.IO) {
            categoryDao.getAllCategories()
        }
    }
    
    suspend fun getCategoryById(categoryId: String): CategoryEntity? {
        return withContext(Dispatchers.IO) {
            categoryDao.getCategoryById(categoryId)
        }
    }
    
    suspend fun insertCategory(category: CategoryEntity) {
        withContext(Dispatchers.IO) {
            categoryDao.insertCategory(category)
        }
    }
    
    suspend fun insertCategories(categories: List<CategoryEntity>) {
        withContext(Dispatchers.IO) {
            categoryDao.insertCategories(categories)
        }
    }
    
    suspend fun updateCategory(category: CategoryEntity) {
        withContext(Dispatchers.IO) {
            categoryDao.updateCategory(category)
        }
    }
    
    suspend fun deleteCategory(categoryId: String) {
        withContext(Dispatchers.IO) {
            categoryDao.deleteCategoryById(categoryId)
        }
    }
    
    // === Работа с заказами ===
    
    fun getOrdersByUser(userId: String): Flow<List<OrderEntity>> {
        return orderDao.getOrdersByUser(userId)
    }
    
    suspend fun getOrderById(orderId: String): OrderEntity? {
        return withContext(Dispatchers.IO) {
            orderDao.getOrderById(orderId)
        }
    }
    
    suspend fun createOrder(order: OrderEntity, orderItems: List<OrderItemEntity>) {
        withContext(Dispatchers.IO) {
            orderDao.insertOrder(order)
            orderDao.insertOrderItems(orderItems)
        }
    }
    
    suspend fun updateOrder(order: OrderEntity) {
        withContext(Dispatchers.IO) {
            orderDao.updateOrder(order)
        }
    }
    
    suspend fun deleteOrder(orderId: String) {
        withContext(Dispatchers.IO) {
            orderDao.deleteOrderItemsByOrderId(orderId)
            orderDao.deleteOrderById(orderId)
        }
    }
    
    suspend fun getOrderItems(orderId: String): List<OrderItemEntity> {
        return withContext(Dispatchers.IO) {
            orderDao.getOrderItems(orderId)
        }
    }
}
