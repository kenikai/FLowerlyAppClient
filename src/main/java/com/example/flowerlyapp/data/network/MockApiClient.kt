package com.example.flowerlyapp.data.network

import com.example.flowerlyapp.data.models.*
import kotlinx.coroutines.delay
import java.util.*

/**
 * Моковый API клиент для тестирования без реального сервера
 * Имитирует работу Ktor сервера локально в приложении
 */
class MockApiClient {
    
    // Моковая база данных пользователей
    private val users = mutableMapOf<String, User>()
    private val passwords = mutableMapOf<String, String>()
    
    init {
        // Добавляем тестового пользователя
        val testUser = User(
            id = "user123",
            firstName = "Test",
            lastName = "User", 
            email = "test@example.com",
            createdAt = Date().toString()
        )
        users["test@example.com"] = testUser
        passwords["test@example.com"] = "password123"
    }
    
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        // Имитируем задержку сети
        delay(1000)
        
        return try {
            val user = users[email]
            val storedPassword = passwords[email]
            
            if (user != null && storedPassword == password) {
                val token = generateMockJWT(user.id, user.email)
                val response = AuthResponse(
                    success = true,
                    message = "Успешный вход",
                    token = token,
                    user = user
                )
                Result.success(response)
            } else {
                Result.failure(Exception("Неверный email или пароль"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка входа: ${e.message}"))
        }
    }
    
    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): Result<AuthResponse> {
        // Имитируем задержку сети
        delay(1500)
        
        return try {
            if (users.containsKey(email)) {
                Result.failure(Exception("Пользователь с таким email уже существует"))
            } else if (password.length < 6) {
                Result.failure(Exception("Пароль должен содержать минимум 6 символов"))
            } else {
                val userId = UUID.randomUUID().toString()
                val user = User(
                    id = userId,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    createdAt = Date().toString()
                )
                
                users[email] = user
                passwords[email] = password
                
                val token = generateMockJWT(userId, email)
                val response = AuthResponse(
                    success = true,
                    message = "Регистрация успешна",
                    token = token,
                    user = user
                )
                Result.success(response)
            }
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка регистрации: ${e.message}"))
        }
    }
    
    suspend fun refreshToken(): Result<String> {
        delay(500)
        return try {
            val token = generateMockJWT("user123", "test@example.com")
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(Exception("Ошибка обновления токена: ${e.message}"))
        }
    }
    
    suspend fun logout(): Result<Unit> {
        delay(300)
        return Result.success(Unit)
    }
    
    private fun generateMockJWT(userId: String, email: String): String {
        // Простая имитация JWT токена
        val header = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9"
        val payload = Base64.getEncoder().encodeToString(
            """{"userId":"$userId","email":"$email","exp":${System.currentTimeMillis() + 86400000}}""".toByteArray()
        )
        val signature = "mock_signature_${System.currentTimeMillis()}"
        
        return "$header.$payload.$signature"
    }
    
    fun isLoggedIn(): Boolean {
        return TokenManager.isLoggedIn()
    }
    
    fun getCurrentUserId(): String? {
        return TokenManager.getUserId()
    }
    
    fun getCurrentUserEmail(): String? {
        return TokenManager.getUserEmail()
    }
}
