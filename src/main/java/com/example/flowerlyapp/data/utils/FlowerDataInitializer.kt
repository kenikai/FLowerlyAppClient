package com.example.flowerlyapp.data.utils

import android.content.Context
import com.example.flowerlyapp.R
import com.example.flowerlyapp.data.database.FlowerlyDatabase
import com.example.flowerlyapp.data.database.entities.CategoryEntity
import com.example.flowerlyapp.data.database.entities.FlowerEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Утилита для инициализации данных цветов в базе данных
 */
object FlowerDataInitializer {
    
    /**
     * Инициализирует данные цветов в базе данных
     */
    suspend fun initializeFlowerData(context: Context) {
        withContext(Dispatchers.IO) {
            val database = FlowerlyDatabase.getDatabase(context)
            val categoryDao = database.categoryDao()
            val flowerDao = database.flowerDao()
            
            // Инициализируем категории
            initializeCategories(categoryDao)
            
            // Инициализируем цветы
            initializeFlowers(flowerDao)
        }
    }
    
    private suspend fun initializeCategories(categoryDao: com.example.flowerlyapp.data.database.dao.CategoryDao) {
        val categories = listOf(
            CategoryEntity(
                id = "category_1",
                name = "Популярные",
                description = "Самые популярные букеты",
                isActive = true
            ),
            CategoryEntity(
                id = "category_2", 
                name = "Сезонные",
                description = "Букеты по сезонам",
                isActive = true
            ),
            CategoryEntity(
                id = "category_3",
                name = "Праздничные", 
                description = "Букеты для особых случаев",
                isActive = true
            ),
            CategoryEntity(
                id = "category_4",
                name = "Классические",
                description = "Классические композиции",
                isActive = true
            )
        )
        
        categoryDao.insertCategories(categories)
    }
    
    private suspend fun initializeFlowers(flowerDao: com.example.flowerlyapp.data.database.dao.FlowerDao) {
        android.util.Log.d("FlowerDataInitializer", "Начинаем инициализацию цветов")
        val flowers = listOf(
            FlowerEntity(
                id = "flower_1",
                name = "Букет 'Осень'",
                description = "Теплый осенний букет с желтыми и оранжевыми оттенками.",
                price = 8000.0,
                imageResourceId = R.drawable.autumn_flower,
                categoryId = "category_2",
                composition = "Оранжевые и медные орхидеи, Сухоцветы",
                isAvailable = true,
                isPopular = true,
                stockQuantity = 5
            ),
            FlowerEntity(
                id = "flower_2",
                name = "Коробка 'trick or treaten'",
                description = "Эффектная цветочная композиция в стиле Хэллоуина, сочетающая элегантность и атмосферу праздника.",
                price = 5000.0,
                imageResourceId = R.drawable.box_flower,
                categoryId = "category_3",
                composition = "Оранжевые розы, Лилии, Рускус",
                isAvailable = true,
                isPopular = true,
                stockQuantity = 3
            ),
            FlowerEntity(
                id = "flower_3",
                name = "Букет 'Валентина'",
                description = "Специальный букет ко Дню Святого Валентина с красными розами.",
                price = 6000.0,
                imageResourceId = R.drawable.valentine_flower,
                categoryId = "category_3",
                composition = "Красные розы, Гвоздики, Ягоды гиперикума, Эвкалипт",
                isAvailable = true,
                isPopular = true,
                stockQuantity = 8
            ),
            FlowerEntity(
                id = "flower_4",
                name = "Букет 'Тюльпанов'",
                description = "Весенний букет из свежих тюльпанов в разных цветах.",
                price = 8000.0,
                imageResourceId = R.drawable.tulip_flower,
                categoryId = "category_2",
                composition = "Тюльпаны разных оттенков, Веточки зелени (эвкалипт, рускус), Мелкие декоративные цветы (ваксфлауэр, гипсофила)",
                isAvailable = true,
                isPopular = true,
                stockQuantity = 6
            ),
            FlowerEntity(
                id = "flower_5",
                name = "Букет 'День рождения'",
                description = "Яркий и веселый букет, специально для Дня Рождения.",
                price = 5000.0,
                imageResourceId = R.drawable.birthday_flower,
                categoryId = "category_3",
                composition = "Розовые розы, Фиолетовая эустома, Красные мини-гвоздики, Зеленые гвоздики",
                isAvailable = true,
                isPopular = true,
                stockQuantity = 4
            ),
            FlowerEntity(
                id = "flower_6",
                name = "Букет 'Лиллия'",
                description = "Изысканный букет с ароматными лилиями.",
                price = 6000.0,
                imageResourceId = R.drawable.lillies_flower,
                categoryId = "category_4",
                composition = "Розовые лилии, Зелень (аспарагус, эвкалипт, рускус)",
                isAvailable = true,
                isPopular = true,
                stockQuantity = 7
            ),
            FlowerEntity(
                id = "flower_7",
                name = "Букет 'Гербер'",
                description = "Яркий букет из гербер, который поднимет настроение.",
                price = 8000.0,
                imageResourceId = R.drawable.gerbera_flower,
                categoryId = "category_4",
                composition = "Оранжевые герберы, Мелкие белые цветы (гипсофила), Эвкалипт",
                isAvailable = true,
                isPopular = true,
                stockQuantity = 5
            ),
            FlowerEntity(
                id = "flower_8",
                name = "Букет 'Ромашки'",
                description = "Нежный букет из белых ромашек с зеленью.",
                price = 4500.0,
                imageResourceId = R.drawable.autumn_flower, // Используем существующее изображение
                categoryId = "category_1",
                composition = "Белые ромашки, Зелень (эвкалипт, рускус)",
                isAvailable = true,
                isPopular = true,
                stockQuantity = 6
            ),
            FlowerEntity(
                id = "flower_9",
                name = "Букет 'Пионы'",
                description = "Роскошный букет из ароматных пионов.",
                price = 12000.0,
                imageResourceId = R.drawable.valentine_flower, // Используем существующее изображение
                categoryId = "category_4",
                composition = "Розовые пионы, Зелень (аспарагус, эвкалипт)",
                isAvailable = true,
                isPopular = true,
                stockQuantity = 3
            ),
            FlowerEntity(
                id = "flower_10",
                name = "Букет 'Солнечный'",
                description = "Яркий букет с подсолнухами и желтыми цветами.",
                price = 7000.0,
                imageResourceId = R.drawable.birthday_flower, // Используем существующее изображение
                categoryId = "category_1",
                composition = "Подсолнухи, Желтые хризантемы, Зелень (эвкалипт)",
                isAvailable = true,
                isPopular = true,
                stockQuantity = 4
            )
        )
        
        android.util.Log.d("FlowerDataInitializer", "Добавляем ${flowers.size} цветов в базу данных")
        flowers.forEach { flower ->
            android.util.Log.d("FlowerDataInitializer", "Цветок: ${flower.name}")
        }
        
        try {
            flowerDao.insertFlowers(flowers)
            android.util.Log.d("FlowerDataInitializer", "Цветы успешно добавлены в базу данных")
        } catch (e: Exception) {
            android.util.Log.e("FlowerDataInitializer", "Ошибка при добавлении цветов в базу", e)
        }
        
        // Проверяем, что цветы действительно добавились
        android.util.Log.d("FlowerDataInitializer", "Проверяем количество цветов в базе...")
        // Не можем вызвать suspend функцию здесь, но добавим логирование в FlowerViewModel
    }
}
