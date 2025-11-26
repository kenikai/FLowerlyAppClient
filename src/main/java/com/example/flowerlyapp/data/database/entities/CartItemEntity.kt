package com.example.flowerlyapp.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Entity для хранения товаров в корзине пользователя
 */
@Entity(
    tableName = "cart_items",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FlowerEntity::class,
            parentColumns = ["id"],
            childColumns = ["flowerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["flowerId"]),
        Index(value = ["userId", "flowerId"], unique = true)
    ]
)
data class CartItemEntity(
    @PrimaryKey
    val id: String,
    val userId: String, // ID пользователя
    val flowerId: String, // ID цветка
    val quantity: Int, // Количество
    val unitPrice: Double, // Цена за единицу на момент добавления
    val totalPrice: Double, // Общая цена позиции
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
