package com.example.flowerlyapp

import android.app.Application

/**
 * Application класс для получения глобального контекста
 */
class FlowerlyApplication : Application() {
    
    companion object {
        lateinit var appContext: android.content.Context
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        
        // Применяем сохраненную тему при запуске приложения
        com.example.flowerlyapp.data.network.ThemeManager.applyTheme()
    }
}