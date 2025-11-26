package com.example.flowerlyapp.data.repository

import android.content.Context
import android.util.Log
import com.example.flowerlyapp.data.database.FlowerlyDatabase
import com.example.flowerlyapp.data.database.entities.UserEntity
import com.example.flowerlyapp.data.models.AuthResponse
import com.example.flowerlyapp.data.models.User
import com.example.flowerlyapp.data.network.TokenManager
import com.example.flowerlyapp.data.utils.PasswordUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Репозиторий для работы с аутентификацией через базу данных
 */
class DatabaseAuthRepository(private val context: Context) {
    
    private val database = FlowerlyDatabase.getDatabase(context)
    private val userDao = database.userDao()
    
    /**
     * Вход пользователя
     */
    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("DatabaseAuthRepository", "Поиск пользователя: $email")
                val userEntity = userDao.getUserByEmail(email)
                
                if (userEntity == null) {
                    return@withContext Result.failure(Exception("Пользователь не найден"))
                }
                
                if (!PasswordUtils.verifyPassword(password, userEntity.passwordHash)) {
                    return@withContext Result.failure(Exception("Неверный пароль"))
                }
                
                val user = User(
                    id = userEntity.id,
                    firstName = userEntity.firstName,
                    lastName = userEntity.lastName,
                    email = userEntity.email,
                    createdAt = Date(userEntity.createdAt).toString()
                )
                
                val token = generateJWT(userEntity.id, userEntity.email)
                
                val response = AuthResponse(
                    success = true,
                    message = "Успешный вход",
                    token = token,
                    user = user
                )
                
                // Сохраняем токен и информацию о пользователе
                TokenManager.saveTokens(token)
                TokenManager.saveUserInfo(user.id, user.email)
                
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(Exception("Ошибка входа: ${e.message}"))
            }
        }
    }
    
    /**
     * Регистрация нового пользователя
     */
    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String
    ): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("DatabaseAuthRepository", "Регистрация пользователя: $email")
                // Проверяем валидность пароля
                if (!PasswordUtils.isPasswordValid(password)) {
                    return@withContext Result.failure(Exception("Пароль должен содержать минимум 6 символов"))
                }
                
                // Проверяем, существует ли пользователь с таким email
                val existingUser = userDao.getUserByEmail(email)
                if (existingUser != null) {
                    return@withContext Result.failure(Exception("Пользователь с таким email уже существует"))
                }
                
                // Создаем нового пользователя
                val userId = UUID.randomUUID().toString()
                val passwordHash = PasswordUtils.hashPassword(password)
                
                val userEntity = UserEntity(
                    id = userId,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    passwordHash = passwordHash,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                // Сохраняем в базу данных
                userDao.insertUser(userEntity)
                
                val user = User(
                    id = userId,
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    createdAt = Date(userEntity.createdAt).toString()
                )
                
                val token = generateJWT(userId, email)
                
                val response = AuthResponse(
                    success = true,
                    message = "Регистрация успешна",
                    token = token,
                    user = user
                )
                
                // Сохраняем токен и информацию о пользователе
                TokenManager.saveTokens(token)
                TokenManager.saveUserInfo(user.id, user.email)
                
                Result.success(response)
            } catch (e: Exception) {
                Result.failure(Exception("Ошибка регистрации: ${e.message}"))
            }
        }
    }
    
    /**
     * Обновление токена
     */
    suspend fun refreshToken(): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = TokenManager.getUserId()
                val email = TokenManager.getUserEmail()
                
                if (userId == null || email == null) {
                    return@withContext Result.failure(Exception("Пользователь не авторизован"))
                }
                
                val token = generateJWT(userId, email)
                TokenManager.saveTokens(token)
                
                Result.success(token)
            } catch (e: Exception) {
                Result.failure(Exception("Ошибка обновления токена: ${e.message}"))
            }
        }
    }
    
    /**
     * Выход пользователя
     */
    suspend fun logout(): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                TokenManager.clearTokens()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception("Ошибка выхода: ${e.message}"))
            }
        }
    }
    
    /**
     * Получение текущего пользователя
     */
    suspend fun getCurrentUser(): User? {
        return withContext(Dispatchers.IO) {
            try {
                val userId = TokenManager.getUserId() ?: return@withContext null
                val userEntity = userDao.getUserById(userId) ?: return@withContext null
                
                User(
                    id = userEntity.id,
                    firstName = userEntity.firstName,
                    lastName = userEntity.lastName,
                    email = userEntity.email,
                    createdAt = Date(userEntity.createdAt).toString()
                )
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Проверка, авторизован ли пользователь
     */
    fun isLoggedIn(): Boolean {
        return TokenManager.isLoggedIn()
    }
    
    /**
     * Получение ID текущего пользователя
     */
    fun getCurrentUserId(): String? {
        return TokenManager.getUserId()
    }
    
    /**
     * Получение email текущего пользователя
     */
    fun getCurrentUserEmail(): String? {
        return TokenManager.getUserEmail()
    }
    
    /**
     * Генерация JWT токена (упрощенная версия)
     */
    private fun generateJWT(userId: String, email: String): String {
        val header = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9"
        val payload = Base64.getEncoder().encodeToString(
            """{"userId":"$userId","email":"$email","exp":${System.currentTimeMillis() + 86400000}}""".toByteArray()
        )
        val signature = "db_signature_${System.currentTimeMillis()}"
        
        return "$header.$payload.$signature"
    }
}
