package com.example.flowerlyapp.data.utils

import android.content.Context
import com.example.flowerlyapp.data.database.FlowerlyDatabase
import com.example.flowerlyapp.data.database.entities.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Утилита для инициализации тестовых данных в базе данных
 */
object DatabaseInitializer {
    
    /**
     * Инициализирует тестовые данные в базе данных
     */
    suspend fun initializeTestData(context: Context) {
        withContext(Dispatchers.IO) {
            android.util.Log.d("DatabaseInitializer", "Начинаем инициализацию тестовых данных")
            try {
                val database = FlowerlyDatabase.getDatabase(context)
                val userDao = database.userDao()
            
            // Проверяем, есть ли уже тестовый пользователь
            val existingUser = userDao.getUserByEmail("test@example.com")
            
            if (existingUser == null) {
                // Создаем тестового пользователя
                val testUser = UserEntity(
                    id = "user123",
                    firstName = "Test",
                    lastName = "User",
                    email = "test@example.com",
                    passwordHash = PasswordUtils.hashPassword("password123"),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                userDao.insertUser(testUser)
            }
            
            // Инициализируем данные цветов
            android.util.Log.d("DatabaseInitializer", "Инициализируем данные цветов")
            FlowerDataInitializer.initializeFlowerData(context)
            android.util.Log.d("DatabaseInitializer", "Инициализация данных завершена")
            } catch (e: Exception) {
                android.util.Log.e("DatabaseInitializer", "Ошибка инициализации данных", e)
            }
        }
    }
}
