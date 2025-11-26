package com.example.flowerlyapp.data.network

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

/**
 * Менеджер для управления темой приложения
 */
object ThemeManager {
    private const val PREFS_NAME = "theme_prefs"
    private const val KEY_IS_DARK_THEME = "is_dark_theme"
    
    private val prefs: SharedPreferences by lazy {
        // Получаем контекст из Application
        val context = com.example.flowerlyapp.FlowerlyApplication.appContext
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Сохранить настройку темы и применить её
     */
    fun setDarkTheme(isDark: Boolean) {
        prefs.edit()
            .putBoolean(KEY_IS_DARK_THEME, isDark)
            .apply()
        
        // Применяем тему динамически
        applyTheme()
    }
    
    /**
     * Получить текущую настройку темы
     */
    fun isDarkTheme(): Boolean {
        return prefs.getBoolean(KEY_IS_DARK_THEME, false)
    }
    
    /**
     * Применить текущую тему
     */
    fun applyTheme() {
        val isDark = isDarkTheme()
        val mode = if (isDark) {
            AppCompatDelegate.MODE_NIGHT_YES
        } else {
            AppCompatDelegate.MODE_NIGHT_NO
        }
        AppCompatDelegate.setDefaultNightMode(mode)
    }
    
    /**
     * Очистить настройки темы
     */
    fun clearThemeSettings() {
        prefs.edit()
            .remove(KEY_IS_DARK_THEME)
            .apply()
    }
}
