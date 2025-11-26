package com.example.flowerlyapp.data.utils

import com.example.flowerlyapp.R

/**
 * Маппер для соответствия ID изображений из базы данных с ресурсами Android
 */
object ImageResourceMapper {
    
    private val imageResourceMap = mapOf(
        // Используем реальные ID ресурсов из логов
        2131165453 to R.drawable.valentine_flower,     // Букет 'Валентина'
        2131165341 to R.drawable.gerbera_flower,       // Букет 'Гербер'
        2131165314 to R.drawable.birthday_flower,      // Букет 'День рождения'
        2131165366 to R.drawable.lillies_flower,       // Букет 'Лиллия'
        2131165305 to R.drawable.autumn_flower,        // Букет 'Осень'
        2131165452 to R.drawable.tulip_flower,         // Букет 'Тюльпанов'
        2131165315 to R.drawable.box_flower,           // Коробка 'trick or treaten'
        // Добавляем fallback для неизвестных ID
        R.drawable.rose101 to R.drawable.rose101,
        R.drawable.monster_flower to R.drawable.monster_flower,
        R.drawable.classic_flower to R.drawable.classic_flower
    )
    
    /**
     * Получить ресурс изображения по ID из базы данных
     */
    fun getImageResource(imageResourceId: Int): Int {
        android.util.Log.d("ImageResourceMapper", "=== ПОЛУЧЕНИЕ ИЗОБРАЖЕНИЯ ===")
        android.util.Log.d("ImageResourceMapper", "imageResourceId: $imageResourceId")
        
        val resourceId = imageResourceMap[imageResourceId] ?: R.drawable.classic_flower
        android.util.Log.d("ImageResourceMapper", "Найден ресурс: $resourceId")
        
        return resourceId
    }
    
    /**
     * Получить случайное изображение товара из доступных
     */
    fun getRandomImageResource(): Int {
        val productImages = listOf(
            R.drawable.valentine_flower,     // Букет 'Валентина'
            R.drawable.gerbera_flower,       // Букет 'Гербер'
            R.drawable.birthday_flower,      // Букет 'День рождения'
            R.drawable.lillies_flower,       // Букет 'Лиллия'
            R.drawable.autumn_flower,        // Букет 'Осень'
            R.drawable.tulip_flower,         // Букет 'Тюльпанов'
            R.drawable.box_flower,           // Коробка 'trick or treaten'
            R.drawable.rose101,              // 101 Роза
            R.drawable.monster_flower,       // Букет 'Монстр'
            R.drawable.classic_flower        // Классический 'Пастель'
        )
        return productImages.random()
    }
    
    /**
     * Получить все доступные ресурсы изображений
     */
    fun getAllImageResources(): List<Int> {
        return imageResourceMap.values.toList()
    }
}
