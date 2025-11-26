package com.example.flowerlyapp.data.database.dao

import androidx.room.*
import com.example.flowerlyapp.data.database.entities.CartItemEntity
import com.example.flowerlyapp.data.database.entities.FlowerEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object для работы с корзиной
 */
@Dao
interface CartDao {
    
    @Query("""
        SELECT c.*, f.name as flowerName, f.imageResourceId, f.price as currentPrice
        FROM cart_items c
        INNER JOIN flowers f ON c.flowerId = f.id
        WHERE c.userId = :userId
        ORDER BY c.createdAt ASC
    """)
    fun getCartItemsWithFlowerInfo(userId: String): Flow<List<CartItemWithFlowerInfo>>
    
    @Query("SELECT * FROM cart_items WHERE userId = :userId")
    suspend fun getCartItems(userId: String): List<CartItemEntity>
    
    @Query("SELECT * FROM cart_items WHERE userId = :userId AND flowerId = :flowerId")
    suspend fun getCartItem(userId: String, flowerId: String): CartItemEntity?
    
    @Query("SELECT COUNT(*) FROM cart_items WHERE userId = :userId")
    suspend fun getCartItemCount(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM cart_items WHERE userId = :userId")
    fun getCartItemCountFlow(userId: String): Flow<Int>
    
    @Query("SELECT SUM(totalPrice) FROM cart_items WHERE userId = :userId")
    suspend fun getCartTotal(userId: String): Double?
    
    @Query("SELECT SUM(totalPrice) FROM cart_items WHERE userId = :userId")
    fun getCartTotalFlow(userId: String): Flow<Double?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItemEntity)
    
    @Update
    suspend fun updateCartItem(cartItem: CartItemEntity)
    
    @Delete
    suspend fun deleteCartItem(cartItem: CartItemEntity)
    
    @Query("DELETE FROM cart_items WHERE userId = :userId AND flowerId = :flowerId")
    suspend fun deleteCartItemByUserAndFlower(userId: String, flowerId: String)
    
    @Query("DELETE FROM cart_items WHERE userId = :userId")
    suspend fun deleteAllCartItemsByUser(userId: String)
    
    @Query("DELETE FROM cart_items WHERE id = :cartItemId")
    suspend fun deleteCartItemById(cartItemId: String)
    
    @Query("UPDATE cart_items SET quantity = :quantity, totalPrice = :totalPrice, updatedAt = :updatedAt WHERE id = :cartItemId")
    suspend fun updateCartItemQuantity(cartItemId: String, quantity: Int, totalPrice: Double, updatedAt: Long)
}

/**
 * Data class для объединения информации о товаре в корзине и цветке
 */
data class CartItemWithFlowerInfo(
    val id: String,
    val userId: String,
    val flowerId: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val createdAt: Long,
    val updatedAt: Long,
    val flowerName: String,
    val imageResourceId: Int,
    val currentPrice: Double
)
