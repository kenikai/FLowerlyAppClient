package com.example.flowerlyapp.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Entity для расширенной информации о профиле пользователя
 */
@Entity(
    tableName = "user_profiles",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"], unique = true)]
)
data class UserProfileEntity(
    @PrimaryKey
    val id: String,
    val userId: String, // ID пользователя (связь с UserEntity)
    val phoneNumber: String? = null, // Номер телефона
    val dateOfBirth: Long? = null, // Дата рождения
    val address: String? = null, // Адрес
    val city: String? = null, // Город
    val postalCode: String? = null, // Почтовый индекс
    val avatarUrl: String? = null, // URL аватара
    val preferences: String? = null, // Предпочтения (JSON строка)
    val isEmailNotificationsEnabled: Boolean = true, // Email уведомления
    val isSmsNotificationsEnabled: Boolean = false, // SMS уведомления
    val isPushNotificationsEnabled: Boolean = true, // Push уведомления
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
