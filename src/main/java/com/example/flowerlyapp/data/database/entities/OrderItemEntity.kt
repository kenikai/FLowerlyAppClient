package com.example.flowerlyapp.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Entity для хранения позиций заказа
 */
@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FlowerEntity::class,
            parentColumns = ["id"],
            childColumns = ["flowerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["orderId"]), Index(value = ["flowerId"])]
)
data class OrderItemEntity(
    @PrimaryKey
    val id: String,
    val orderId: String, // ID заказа
    val flowerId: String, // ID цветка
    val quantity: Int, // Количество
    val unitPrice: Double, // Цена за единицу на момент заказа
    val totalPrice: Double // Общая цена позиции
)

