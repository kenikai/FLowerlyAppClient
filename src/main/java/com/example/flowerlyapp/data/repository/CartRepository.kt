package com.example.flowerlyapp.data.repository

import android.content.Context
import com.example.flowerlyapp.data.database.FlowerlyDatabase
import com.example.flowerlyapp.data.database.dao.CartItemWithFlowerInfo
import com.example.flowerlyapp.data.database.entities.CartItemEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Репозиторий для работы с корзиной
 * Использует PostgreSQL через API вместо SQLite
 */
class CartRepository(private val context: Context) {
    
    // Используем PostgreSQL через API
    private val postgresRepository = PostgresCartRepository(context)
    
    // Оставляем SQLite для fallback (если нужно)
    private val database = FlowerlyDatabase.getDatabase(context)
    private val cartDao = database.cartDao()
    
    // Переключатель между PostgreSQL и SQLite
    private val usePostgres = true // Используем PostgreSQL
    
    /**
     * Получить все товары в корзине с информацией о цветах
     */
    fun getCartItemsWithFlowerInfo(userId: String): Flow<List<CartItemWithFlowerInfo>> {
        android.util.Log.d("CartRepository", "Загружаем корзину для пользователя: $userId, usePostgres: $usePostgres")
        return if (usePostgres) {
            android.util.Log.d("CartRepository", "Используем PostgreSQL репозиторий")
            postgresRepository.getCartItemsWithFlowerInfo(userId)
        } else {
            android.util.Log.d("CartRepository", "Используем SQLite репозиторий")
            cartDao.getCartItemsWithFlowerInfo(userId)
        }
    }
    
    /**
     * Получить количество товаров в корзине
     */
    fun getCartItemCount(userId: String): Flow<Int> {
        return if (usePostgres) {
            postgresRepository.getCartItemCount(userId)
        } else {
            cartDao.getCartItemCountFlow(userId)
        }
    }
    
    /**
     * Получить общую стоимость корзины
     */
    fun getCartTotal(userId: String): Flow<Double?> {
        return if (usePostgres) {
            postgresRepository.getCartTotal(userId)
        } else {
            cartDao.getCartTotalFlow(userId)
        }
    }
    
    /**
     * Добавить цветок в корзину
     */
    suspend fun addToCart(userId: String, flowerId: String, quantity: Int, unitPrice: Double) {
        android.util.Log.d("CartRepository", "=== ДОБАВЛЕНИЕ В КОРЗИНУ REPOSITORY ===")
        android.util.Log.d("CartRepository", "userId: $userId, flowerId: $flowerId, quantity: $quantity, unitPrice: $unitPrice")
        android.util.Log.d("CartRepository", "usePostgres: $usePostgres")
        
        if (usePostgres) {
            postgresRepository.addToCart(userId, flowerId, quantity, unitPrice)
        } else {
            withContext(Dispatchers.IO) {
                try {
                    val existingItem = cartDao.getCartItem(userId, flowerId)
                    android.util.Log.d("CartRepository", "existingItem: $existingItem")
                    
                    if (existingItem != null) {
                        android.util.Log.d("CartRepository", "Товар уже есть в корзине, увеличиваем количество")
                        // Увеличиваем количество существующего товара
                        val newQuantity = existingItem.quantity + quantity
                        val newTotalPrice = newQuantity * unitPrice
                        android.util.Log.d("CartRepository", "Новое количество: $newQuantity, новая цена: $newTotalPrice")
                        cartDao.updateCartItemQuantity(
                            existingItem.id,
                            newQuantity,
                            newTotalPrice,
                            System.currentTimeMillis()
                        )
                        android.util.Log.d("CartRepository", "Количество обновлено")
                    } else {
                        android.util.Log.d("CartRepository", "Новый товар, добавляем в корзину")
                        // Добавляем новый товар в корзину
                        val cartItem = CartItemEntity(
                            id = UUID.randomUUID().toString(),
                            userId = userId,
                            flowerId = flowerId,
                            quantity = quantity,
                            unitPrice = unitPrice,
                            totalPrice = quantity * unitPrice,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        android.util.Log.d("CartRepository", "Создана CartItemEntity: $cartItem")
                        cartDao.insertCartItem(cartItem)
                        android.util.Log.d("CartRepository", "Товар добавлен в корзину")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CartRepository", "Ошибка добавления в корзину: ${e.message}", e)
                    throw e
                }
            }
        }
    }
    
    /**
     * Обновить количество товара в корзине
     */
    suspend fun updateCartItemQuantity(userId: String, flowerId: String, newQuantity: Int) {
        if (usePostgres) {
            postgresRepository.updateCartItemQuantity(userId, flowerId, newQuantity)
        } else {
            withContext(Dispatchers.IO) {
                val cartItem = cartDao.getCartItem(userId, flowerId)
                if (cartItem != null) {
                    if (newQuantity <= 0) {
                        // Удаляем товар из корзины, если количество 0 или меньше
                        cartDao.deleteCartItemByUserAndFlower(userId, flowerId)
                    } else {
                        // Обновляем количество
                        val newTotalPrice = newQuantity * cartItem.unitPrice
                        cartDao.updateCartItemQuantity(
                            cartItem.id,
                            newQuantity,
                            newTotalPrice,
                            System.currentTimeMillis()
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Удалить товар из корзины
     */
    suspend fun removeFromCart(userId: String, flowerId: String) {
        if (usePostgres) {
            postgresRepository.removeFromCart(userId, flowerId)
        } else {
            withContext(Dispatchers.IO) {
                cartDao.deleteCartItemByUserAndFlower(userId, flowerId)
            }
        }
    }
    
    /**
     * Очистить всю корзину пользователя
     */
    suspend fun clearCart(userId: String) {
        if (usePostgres) {
            postgresRepository.clearCart(userId)
        } else {
            withContext(Dispatchers.IO) {
                cartDao.deleteAllCartItemsByUser(userId)
            }
        }
    }
    
    /**
     * Получить товар из корзины
     */
    suspend fun getCartItem(userId: String, flowerId: String): CartItemEntity? {
        return withContext(Dispatchers.IO) {
            cartDao.getCartItem(userId, flowerId)
        }
    }
    
    /**
     * Получить все товары в корзине (без информации о цветах)
     */
    suspend fun getCartItems(userId: String): List<CartItemEntity> {
        return withContext(Dispatchers.IO) {
            cartDao.getCartItems(userId)
        }
    }
}
