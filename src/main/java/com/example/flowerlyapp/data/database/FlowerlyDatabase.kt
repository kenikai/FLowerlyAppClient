package com.example.flowerlyapp.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.example.flowerlyapp.data.database.dao.*
import com.example.flowerlyapp.data.database.entities.*

/**
 * Главная база данных приложения
 */
@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        FlowerEntity::class,
        OrderEntity::class,
        OrderItemEntity::class,
        FavoriteEntity::class,
        CartItemEntity::class,
        UserProfileEntity::class
    ],
    version = 4, // Увеличиваем версию после изменения FavoriteEntity
    exportSchema = false
)
abstract class FlowerlyDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun flowerDao(): FlowerDao
    abstract fun orderDao(): OrderDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun cartDao(): CartDao
    abstract fun userProfileDao(): UserProfileDao
    
    companion object {
        @Volatile
        private var INSTANCE: FlowerlyDatabase? = null
        
        fun getDatabase(context: Context): FlowerlyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FlowerlyDatabase::class.java,
                    "flowerly_database"
                )
                .fallbackToDestructiveMigration() // Для разработки - пересоздает БД при изменении схемы
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
