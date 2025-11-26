package com.example.flowerlyapp.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Entity для хранения заказов
 */
@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"])]
)
data class OrderEntity(
    @PrimaryKey
    val id: String,
    val userId: String, // ID пользователя
    val totalAmount: Double, // Общая сумма заказа
    val status: String, // Статус заказа (pending, confirmed, shipped, delivered, cancelled)
    val deliveryAddress: String? = null, // Адрес доставки
    val deliveryDate: Long? = null, // Дата доставки
    val notes: String? = null, // Дополнительные заметки
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

