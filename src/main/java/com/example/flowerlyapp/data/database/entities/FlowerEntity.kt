package com.example.flowerlyapp.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Entity для хранения цветов
 */
@Entity(
    tableName = "flowers",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["categoryId"])]
)
data class FlowerEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val price: Double, // Цена в рублях
    val imageResourceId: Int, // ID ресурса изображения
    val imageUrl: String? = null, // URL изображения (для будущего использования)
    val categoryId: String? = null, // ID категории
    val composition: String? = null, // Состав букета
    val isAvailable: Boolean = true, // Доступен ли для заказа
    val isPopular: Boolean = false, // Популярный ли цветок
    val stockQuantity: Int = 0, // Количество на складе
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

