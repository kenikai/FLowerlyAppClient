package com.example.flowerlyapp.data.repository

import android.content.Context
import android.util.Log
import com.example.flowerlyapp.data.database.FlowerlyDatabase
import com.example.flowerlyapp.data.database.dao.CartItemWithFlowerInfo
import com.example.flowerlyapp.data.database.entities.FlowerEntity
import com.example.flowerlyapp.data.network.ApiClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable
data class CartItemResponse(
    val id: String,
    val flowerId: String,
    val flowerName: String,
    val quantity: Int,
    val price: Double,
    val totalPrice: Double,
    val imageResourceId: Int = 0
)

/**
 * Репозиторий для работы с корзиной через PostgreSQL API
 */
class PostgresCartRepository(private val context: Context) {
    
    private val client = ApiClient.client
    private val database by lazy { FlowerlyDatabase.getDatabase(context) }
    private val flowerDao by lazy { database.flowerDao() }
    
    /**
     * Получить цветок из локальной базы данных SQLite
     */
    private fun getLocalFlower(flowerId: String): FlowerEntity? {
        return try {
            runBlocking { flowerDao.getFlowerById(flowerId) }
        } catch (e: Exception) {
            Log.e("PostgresCartRepository", "Ошибка получения данных цветка: ${e.message}")
            null
        }
    }
    
    /**
     * Извлечь значение из JSON строки
     */
    private fun extractJsonValue(json: String, key: String): String {
        // Сначала пробуем найти строковое значение
        val stringPattern = "\"$key\"\\s*:\\s*\"([^\"]+)\""
        val stringRegex = stringPattern.toRegex()
        val stringMatch = stringRegex.find(json)
        if (stringMatch != null) {
            return stringMatch.groupValues[1]
        }
        
        // Затем пробуем найти числовое значение
        val numberPattern = "\"$key\"\\s*:\\s*([0-9.]+)"
        val numberRegex = numberPattern.toRegex()
        val numberMatch = numberRegex.find(json)
        if (numberMatch != null) {
            return numberMatch.groupValues[1]
        }
        
        return ""
    }
    
    /**
     * Парсить JSON массив товаров корзины
     */
    private fun parseCartItemsFromJson(jsonString: String): List<CartItemResponse> {
        val cartItems = mutableListOf<CartItemResponse>()
        try {
            val json = jsonString.trim()
            if (json.startsWith("[") && json.endsWith("]")) {
                // Убираем внешние скобки
                val content = json.substring(1, json.length - 1).trim()
                
                if (content.isEmpty()) {
                    return cartItems
                }
                
                // Более точное разделение объектов
                val items = mutableListOf<String>()
                var currentItem = ""
                var braceCount = 0
                var inString = false
                var escapeNext = false
                
                for (char in content) {
                    if (escapeNext) {
                        currentItem += char
                        escapeNext = false
                        continue
                    }
                    
                    if (char == '\\') {
                        escapeNext = true
                        currentItem += char
                        continue
                    }
                    
                    if (char == '"' && !escapeNext) {
                        inString = !inString
                    }
                    
                    if (!inString) {
                        if (char == '{') {
                            braceCount++
                        } else if (char == '}') {
                            braceCount--
                        }
                    }
                    
                    currentItem += char
                    
                    // Если мы закрыли объект и не внутри строки
                    if (braceCount == 0 && !inString && currentItem.trim().isNotEmpty()) {
                        items.add(currentItem.trim())
                        currentItem = ""
                    }
                }
                
                Log.d("PostgresCartRepository", "Найдено объектов: ${items.size}")
                
                for (i in items.indices) {
                    val itemJson = items[i].trim()
                    Log.d("PostgresCartRepository", "Парсим объект $i: $itemJson")
                    
                    // Пропускаем пустые или некорректные объекты
                    if (itemJson.isEmpty() || itemJson == "," || !itemJson.startsWith("{")) {
                        Log.d("PostgresCartRepository", "Пропускаем пустой объект: $itemJson")
                        continue
                    }
                    
                    // Простой парсинг JSON объекта
                    val id = extractJsonValue(itemJson, "id")
                    val flowerId = extractJsonValue(itemJson, "flowerId")
                    val flowerName = extractJsonValue(itemJson, "flowerName")
                    val quantity = extractJsonValue(itemJson, "quantity").toIntOrNull() ?: 0
                    val price = extractJsonValue(itemJson, "price").toDoubleOrNull() ?: 0.0
                    val totalPrice = extractJsonValue(itemJson, "totalPrice").toDoubleOrNull() ?: (price * quantity)
                    val imageResourceId = extractJsonValue(itemJson, "imageResourceId").toIntOrNull() ?: 0
                    
                    if (id.isNotEmpty()) {
                        cartItems.add(CartItemResponse(
                            id = id,
                            flowerId = flowerId,
                            flowerName = flowerName,
                            quantity = quantity,
                            price = price,
                            totalPrice = totalPrice,
                            imageResourceId = imageResourceId
                        ))
                        Log.d("PostgresCartRepository", "Добавлен товар: $flowerName, количество: $quantity, цена: $price")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("PostgresCartRepository", "Ошибка парсинга JSON: ${e.message}")
        }
        Log.d("PostgresCartRepository", "Всего распарсено товаров: ${cartItems.size}")
        return cartItems
    }
    
    /**
     * Получить все товары в корзине
     */
    fun getCartItemsWithFlowerInfo(userId: String): Flow<List<CartItemWithFlowerInfo>> {
        return flow {
            try {
                Log.d("PostgresCartRepository", "Загружаем корзину для пользователя: $userId")
                val response = client.get("/api/cart") {
                    header("X-User-ID", userId)
                }
                
                when (response.status) {
                    HttpStatusCode.OK -> {
                        val jsonString = response.body<String>()
                        Log.d("PostgresCartRepository", "Получен JSON: $jsonString")
                        
                        val cartItems = parseCartItemsFromJson(jsonString)
                        val cartItemsWithFlowerInfo = cartItems.map { item ->
                            val localFlower = getLocalFlower(item.flowerId)
                            val resolvedName = localFlower?.name?.takeIf { it.isNotBlank() }
                                ?: item.flowerName.ifBlank { "Без названия" }
                            val resolvedPrice = localFlower?.price ?: item.price
                            val resolvedImageId = localFlower?.imageResourceId ?: item.imageResourceId
                            CartItemWithFlowerInfo(
                                id = item.id,
                                userId = userId,
                                flowerId = item.flowerId,
                                quantity = item.quantity,
                                unitPrice = resolvedPrice,
                                totalPrice = resolvedPrice * item.quantity,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis(),
                                flowerName = resolvedName,
                                imageResourceId = resolvedImageId,
                                currentPrice = resolvedPrice
                            )
                        }
                        Log.d("PostgresCartRepository", "Получено ${cartItemsWithFlowerInfo.size} товаров в корзине")
                        emit(cartItemsWithFlowerInfo)
                    }
                    HttpStatusCode.Unauthorized -> {
                        Log.e("PostgresCartRepository", "Пользователь не авторизован")
                        emit(emptyList())
                    }
                    else -> {
                        Log.e("PostgresCartRepository", "Ошибка загрузки корзины: ${response.status}")
                        emit(emptyList())
                    }
                }
            } catch (e: Exception) {
                Log.e("PostgresCartRepository", "Ошибка загрузки корзины: ${e.message}", e)
                emit(emptyList())
            }
        }
    }
    
    /**
     * Получить количество товаров в корзине
     */
    fun getCartItemCount(userId: String): Flow<Int> {
        return flow {
            try {
                val response = client.get("/api/cart") {
                    header("X-User-ID", userId)
                }
                when (response.status) {
                    HttpStatusCode.OK -> {
                        val jsonString = response.body<String>()
                        val cartItems = parseCartItemsFromJson(jsonString)
                        val count = cartItems.sumOf { it.quantity }
                        emit(count)
                    }
                    else -> emit(0)
                }
            } catch (e: Exception) {
                Log.e("PostgresCartRepository", "Ошибка получения количества товаров: ${e.message}")
                emit(0)
            }
        }
    }
    
    /**
     * Получить общую стоимость корзины
     */
    fun getCartTotal(userId: String): Flow<Double?> {
        return flow {
            try {
                val response = client.get("/api/cart") {
                    header("X-User-ID", userId)
                }
                when (response.status) {
                    HttpStatusCode.OK -> {
                        val jsonString = response.body<String>()
                        val cartItems = parseCartItemsFromJson(jsonString)
                        val total = cartItems.sumOf { it.totalPrice }
                        emit(total)
                    }
                    else -> emit(0.0)
                }
            } catch (e: Exception) {
                Log.e("PostgresCartRepository", "Ошибка получения общей стоимости: ${e.message}")
                emit(0.0)
            }
        }
    }
    
    /**
     * Добавить товар в корзину
     */
    suspend fun addToCart(userId: String, flowerId: String, quantity: Int, unitPrice: Double) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("PostgresCartRepository", "Добавляем в корзину: userId=$userId, flowerId=$flowerId, quantity=$quantity, price=$unitPrice")
                
                val request = AddToCartRequest(
                    flowerId = flowerId,
                    quantity = quantity,
                    price = unitPrice
                )
                
                val response = client.post("/api/cart") {
                    setBody(request)
                    header("X-User-ID", userId)
                    header("Content-Type", "application/json")
                }
                
                when (response.status) {
                    HttpStatusCode.OK -> {
                        Log.d("PostgresCartRepository", "Товар успешно добавлен в корзину")
                    }
                    HttpStatusCode.Unauthorized -> {
                        Log.e("PostgresCartRepository", "Пользователь не авторизован")
                        throw Exception("Пользователь не авторизован")
                    }
                    else -> {
                        Log.e("PostgresCartRepository", "Ошибка добавления в корзину: ${response.status}")
                        throw Exception("Ошибка добавления в корзину: ${response.status}")
                    }
                }
            } catch (e: Exception) {
                Log.e("PostgresCartRepository", "Ошибка добавления в корзину: ${e.message}", e)
                throw e
            }
        }
    }
    
    /**
     * Обновить количество товара в корзине
     */
    suspend fun updateCartItemQuantity(userId: String, flowerId: String, newQuantity: Int) {
        withContext(Dispatchers.IO) {
            try {
                val request = UpdateCartRequest(quantity = newQuantity)
                val response = client.put("/api/cart/$flowerId") {
                    setBody(request)
                    header("X-User-ID", userId)
                    header("Content-Type", "application/json")
                }
                
                when (response.status) {
                    HttpStatusCode.OK -> {
                        Log.d("PostgresCartRepository", "Количество товара обновлено")
                    }
                    else -> {
                        Log.e("PostgresCartRepository", "Ошибка обновления количества: ${response.status}")
                        throw Exception("Ошибка обновления количества: ${response.status}")
                    }
                }
            } catch (e: Exception) {
                Log.e("PostgresCartRepository", "Ошибка обновления количества: ${e.message}", e)
                throw e
            }
        }
    }
    
    /**
     * Удалить товар из корзины
     */
    suspend fun removeFromCart(userId: String, flowerId: String) {
        withContext(Dispatchers.IO) {
            try {
                Log.d("PostgresCartRepository", "Удаляем товар: userId=$userId, flowerId=$flowerId")
                val response = client.delete("/api/cart/$flowerId") {
                    header("X-User-ID", userId)
                }
                
                when (response.status) {
                    HttpStatusCode.OK -> {
                        Log.d("PostgresCartRepository", "Товар удален из корзины")
                    }
                    else -> {
                        Log.e("PostgresCartRepository", "Ошибка удаления из корзины: ${response.status}")
                        throw Exception("Ошибка удаления из корзины: ${response.status}")
                    }
                }
            } catch (e: Exception) {
                Log.e("PostgresCartRepository", "Ошибка удаления из корзины: ${e.message}", e)
                throw e
            }
        }
    }
    
    /**
     * Очистить корзину
     */
    suspend fun clearCart(userId: String) {
        withContext(Dispatchers.IO) {
            try {
                val response = client.delete("/api/cart") {
                    header("X-User-ID", userId)
                }
                
                when (response.status) {
                    HttpStatusCode.OK -> {
                        Log.d("PostgresCartRepository", "Корзина очищена")
                    }
                    else -> {
                        Log.e("PostgresCartRepository", "Ошибка очистки корзины: ${response.status}")
                        throw Exception("Ошибка очистки корзины: ${response.status}")
                    }
                }
            } catch (e: Exception) {
                Log.e("PostgresCartRepository", "Ошибка очистки корзины: ${e.message}", e)
                throw e
            }
        }
    }
}


@kotlinx.serialization.Serializable
data class AddToCartRequest(
    val flowerId: String,
    val quantity: Int,
    val price: Double
)

@kotlinx.serialization.Serializable
data class UpdateCartRequest(
    val quantity: Int
)


