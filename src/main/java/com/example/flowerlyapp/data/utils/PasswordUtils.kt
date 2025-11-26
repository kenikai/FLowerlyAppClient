package com.example.flowerlyapp.data.utils

import org.mindrot.jbcrypt.BCrypt

/**
 * Утилиты для работы с паролями
 */
object PasswordUtils {
    
    /**
     * Хеширует пароль с использованием BCrypt
     * @param password исходный пароль
     * @return хешированный пароль
     */
    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }
    
    /**
     * Проверяет, соответствует ли пароль хешу
     * @param password исходный пароль
     * @param hash хешированный пароль
     * @return true если пароли совпадают
     */
    fun verifyPassword(password: String, hash: String): Boolean {
        return try {
            BCrypt.checkpw(password, hash)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Проверяет сложность пароля
     * @param password пароль для проверки
     * @return true если пароль соответствует требованиям
     */
    fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }
}

