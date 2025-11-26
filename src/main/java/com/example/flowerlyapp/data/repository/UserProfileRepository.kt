package com.example.flowerlyapp.data.repository

import android.content.Context
import com.example.flowerlyapp.data.database.FlowerlyDatabase
import com.example.flowerlyapp.data.database.entities.UserProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Репозиторий для работы с профилями пользователей
 */
class UserProfileRepository(private val context: Context) {
    
    private val database = FlowerlyDatabase.getDatabase(context)
    private val userProfileDao = database.userProfileDao()
    
    /**
     * Получить профиль пользователя
     */
    suspend fun getUserProfile(userId: String): UserProfileEntity? {
        return withContext(Dispatchers.IO) {
            userProfileDao.getUserProfile(userId)
        }
    }
    
    /**
     * Получить профиль пользователя (Flow)
     */
    fun getUserProfileFlow(userId: String): Flow<UserProfileEntity?> {
        return userProfileDao.getUserProfileFlow(userId)
    }
    
    /**
     * Создать профиль пользователя
     */
    suspend fun createUserProfile(userId: String): UserProfileEntity {
        return withContext(Dispatchers.IO) {
            val profile = UserProfileEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            userProfileDao.insertUserProfile(profile)
            profile
        }
    }
    
    /**
     * Обновить профиль пользователя
     */
    suspend fun updateUserProfile(userProfile: UserProfileEntity) {
        withContext(Dispatchers.IO) {
            val updatedProfile = userProfile.copy(updatedAt = System.currentTimeMillis())
            userProfileDao.updateUserProfile(updatedProfile)
        }
    }
    
    /**
     * Обновить номер телефона
     */
    suspend fun updatePhoneNumber(userId: String, phoneNumber: String) {
        withContext(Dispatchers.IO) {
            userProfileDao.updatePhoneNumber(userId, phoneNumber, System.currentTimeMillis())
        }
    }
    
    /**
     * Обновить адрес
     */
    suspend fun updateAddress(userId: String, address: String, city: String, postalCode: String) {
        withContext(Dispatchers.IO) {
            userProfileDao.updateAddress(userId, address, city, postalCode, System.currentTimeMillis())
        }
    }
    
    /**
     * Обновить аватар
     */
    suspend fun updateAvatar(userId: String, avatarUrl: String) {
        withContext(Dispatchers.IO) {
            userProfileDao.updateAvatar(userId, avatarUrl, System.currentTimeMillis())
        }
    }
    
    /**
     * Обновить настройки уведомлений
     */
    suspend fun updateNotificationSettings(
        userId: String,
        emailNotifications: Boolean,
        smsNotifications: Boolean,
        pushNotifications: Boolean
    ) {
        withContext(Dispatchers.IO) {
            userProfileDao.updateEmailNotifications(userId, emailNotifications, System.currentTimeMillis())
            userProfileDao.updateSmsNotifications(userId, smsNotifications, System.currentTimeMillis())
            userProfileDao.updatePushNotifications(userId, pushNotifications, System.currentTimeMillis())
        }
    }
    
    /**
     * Получить или создать профиль пользователя
     */
    suspend fun getOrCreateUserProfile(userId: String): UserProfileEntity {
        return withContext(Dispatchers.IO) {
            var profile = userProfileDao.getUserProfile(userId)
            if (profile == null) {
                profile = createUserProfile(userId)
            }
            profile
        }
    }
    
    /**
     * Удалить профиль пользователя
     */
    suspend fun deleteUserProfile(userId: String) {
        withContext(Dispatchers.IO) {
            userProfileDao.deleteUserProfileByUserId(userId)
        }
    }
}
