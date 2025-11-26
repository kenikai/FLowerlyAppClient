package com.example.flowerlyapp.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Entity для хранения избранных цветов пользователя
 */
@Entity(
    tableName = "favorites",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["flowerId"]),
        Index(value = ["userId", "flowerId"], unique = true)
    ]
)
data class FavoriteEntity(
    @PrimaryKey
    val id: String,
    val userId: String, // ID пользователя
    val flowerId: String, // ID цветка
    val createdAt: Long = System.currentTimeMillis()
)
