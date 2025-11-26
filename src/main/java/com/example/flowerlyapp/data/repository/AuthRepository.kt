package com.example.flowerlyapp.data.repository

import android.content.Context
import android.util.Log
import com.example.flowerlyapp.data.models.*
import com.example.flowerlyapp.data.network.ApiClient
import com.example.flowerlyapp.data.network.MockApiClient
import com.example.flowerlyapp.data.network.TokenManager
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class AuthRepository(private val context: Context) {
    
    private val client = ApiClient.client
    private val mockClient = MockApiClient()
    private val databaseRepository = DatabaseAuthRepository(context)
    
    // Используем только PostgreSQL через Ktor сервер
    private val dataSource = 2 // PostgreSQL через Ktor сервер
    
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        Log.d("AuthRepository", "Попытка входа: email=$email, dataSource=$dataSource")
        return when (dataSource) {
            0 -> {
                // Моковый сервер (в памяти)
                mockClient.login(email, password).also { result ->
                    result.getOrNull()?.let { authResponse ->
                        if (authResponse.success && authResponse.token != null) {
                            TokenManager.saveTokens(authResponse.token)
                            authResponse.user?.let { user ->
                                TokenManager.saveUserInfo(user.id, user.email, user.firstName, user.lastName)
                            }
                        }
                    }
                }
            }
            1 -> {
                // Реальная база данных (SQLite)
                databaseRepository.login(email, password)
            }
            2 -> {
                // Реальный Ktor сервер
                withContext(Dispatchers.IO) {
                    try {
                        val response = client.post("/api/auth/login") {
                            setBody(LoginRequest(email, password))
                        }
                        
                        when (response.status) {
                            HttpStatusCode.OK -> {
                                val authResponse: AuthResponse = response.body()
                                if (authResponse.success && authResponse.token != null) {
                                    TokenManager.saveTokens(authResponse.token)
                                    authResponse.user?.let { user ->
                                        TokenManager.saveUserInfo(user.id, user.email, user.firstName, user.lastName)
                                    }
                                }
                                Result.success(authResponse)
                            }
                            HttpStatusCode.Unauthorized -> {
                                Result.failure(Exception("Неверный email или пароль"))
                            }
                            else -> {
                                Result.failure(Exception("Ошибка сервера: ${response.status}"))
                            }
                        }
                    } catch (e: Exception) {
                        Result.failure(Exception("Ошибка сети: ${e.message}"))
                    }
                }
            }
            else -> Result.failure(Exception("Неизвестный источник данных"))
        }
    }
    
    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): Result<AuthResponse> {
        Log.d("AuthRepository", "Попытка регистрации: email=$email, dataSource=$dataSource")
        return when (dataSource) {
            0 -> {
                // Моковый сервер (в памяти)
                mockClient.register(firstName, lastName, email, password).also { result ->
                    result.getOrNull()?.let { authResponse ->
                        if (authResponse.success && authResponse.token != null) {
                            TokenManager.saveTokens(authResponse.token)
                            authResponse.user?.let { user ->
                                TokenManager.saveUserInfo(user.id, user.email, user.firstName, user.lastName)
                            }
                        }
                    }
                }
            }
            1 -> {
                // Реальная база данных (SQLite)
                databaseRepository.register(firstName, lastName, email, password)
            }
            2 -> {
                // Реальный Ktor сервер
                withContext(Dispatchers.IO) {
                    try {
                        val response = client.post("/api/auth/register") {
                            setBody(RegisterRequest(firstName, lastName, email, password, password))
                        }
                        
                        when (response.status) {
                            HttpStatusCode.Created -> {
                                val authResponse: AuthResponse = response.body()
                                if (authResponse.success && authResponse.token != null) {
                                    TokenManager.saveTokens(authResponse.token)
                                    authResponse.user?.let { user ->
                                        TokenManager.saveUserInfo(user.id, user.email, user.firstName, user.lastName)
                                    }
                                }
                                Result.success(authResponse)
                            }
                            HttpStatusCode.Conflict -> {
                                Result.failure(Exception("Пользователь с таким email уже существует"))
                            }
                            HttpStatusCode.BadRequest -> {
                                Result.failure(Exception("Некорректные данные"))
                            }
                            else -> {
                                Result.failure(Exception("Ошибка сервера: ${response.status}"))
                            }
                        }
                    } catch (e: Exception) {
                        Result.failure(Exception("Ошибка сети: ${e.message}"))
                    }
                }
            }
            else -> Result.failure(Exception("Неизвестный источник данных"))
        }
    }
    
    suspend fun refreshToken(): Result<String> {
        return when (dataSource) {
            0 -> mockClient.refreshToken()
            1 -> databaseRepository.refreshToken()
            2 -> {
                withContext(Dispatchers.IO) {
                    try {
                        val refreshToken = TokenManager.getRefreshToken()
                        if (refreshToken == null) {
                            return@withContext Result.failure(Exception("Refresh token не найден"))
                        }
                        
                        val response = client.post("/api/auth/refresh") {
                            setBody(mapOf("refreshToken" to refreshToken))
                        }
                        
                        when (response.status) {
                            HttpStatusCode.OK -> {
                                val tokenResponse: TokenResponse = response.body()
                                TokenManager.saveTokens(tokenResponse.accessToken, tokenResponse.refreshToken)
                                Result.success(tokenResponse.accessToken)
                            }
                            HttpStatusCode.Unauthorized -> {
                                TokenManager.clearTokens()
                                Result.failure(Exception("Сессия истекла"))
                            }
                            else -> {
                                Result.failure(Exception("Ошибка обновления токена: ${response.status}"))
                            }
                        }
                    } catch (e: Exception) {
                        Result.failure(Exception("Ошибка сети: ${e.message}"))
                    }
                }
            }
            else -> Result.failure(Exception("Неизвестный источник данных"))
        }
    }
    
    suspend fun logout(): Result<Unit> {
        return when (dataSource) {
            0 -> {
                mockClient.logout().also {
                    TokenManager.clearTokens()
                }
            }
            1 -> databaseRepository.logout()
            2 -> {
                withContext(Dispatchers.IO) {
                    try {
                        val response = client.post("/api/auth/logout")
                        TokenManager.clearTokens()
                        when (response.status) {
                            HttpStatusCode.OK -> Result.success(Unit)
                            else -> Result.success(Unit)
                        }
                    } catch (e: Exception) {
                        TokenManager.clearTokens()
                        Result.success(Unit)
                    }
                }
            }
            else -> Result.failure(Exception("Неизвестный источник данных"))
        }
    }
    
    fun isLoggedIn(): Boolean {
        return when (dataSource) {
            0 -> mockClient.isLoggedIn()
            1 -> databaseRepository.isLoggedIn()
            2 -> TokenManager.isLoggedIn()
            else -> false
        }
    }
    
    fun getCurrentUserId(): String? {
        return when (dataSource) {
            0 -> mockClient.getCurrentUserId()
            1 -> databaseRepository.getCurrentUserId()
            2 -> TokenManager.getUserId()
            else -> null
        }
    }
    
    fun getCurrentUserEmail(): String? {
        return when (dataSource) {
            0 -> mockClient.getCurrentUserEmail()
            1 -> databaseRepository.getCurrentUserEmail()
            2 -> TokenManager.getUserEmail()
            else -> null
        }
    }
    
    /**
     * Получение текущего пользователя (только для базы данных)
     */
    suspend fun getCurrentUser(): User? {
        return when (dataSource) {
            1 -> databaseRepository.getCurrentUser()
            else -> null
        }
    }
}
