package com.example.flowerlyapp.domain.usecase

import com.example.flowerlyapp.data.models.AuthResponse
import com.example.flowerlyapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class RegisterUseCase(private val authRepository: AuthRepository) {
    
    suspend operator fun invoke(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Flow<Result<AuthResponse>> = flow {
        
        // Валидация имени
        if (firstName.trim().isEmpty()) {
            emit(Result.failure(Exception("Имя не может быть пустым")))
            return@flow
        }
        
        if (firstName.trim().length < 2) {
            emit(Result.failure(Exception("Имя должно содержать минимум 2 символа")))
            return@flow
        }
        
        // Валидация фамилии
        if (lastName.trim().isEmpty()) {
            emit(Result.failure(Exception("Фамилия не может быть пустой")))
            return@flow
        }
        
        if (lastName.trim().length < 2) {
            emit(Result.failure(Exception("Фамилия должна содержать минимум 2 символа")))
            return@flow
        }
        
        // Валидация email
        if (!isValidEmail(email)) {
            emit(Result.failure(Exception("Некорректный формат email")))
            return@flow
        }
        
        // Валидация пароля
        if (password.length < 6) {
            emit(Result.failure(Exception("Пароль должен содержать минимум 6 символов")))
            return@flow
        }
        
        if (!isValidPassword(password)) {
            emit(Result.failure(Exception("Пароль должен содержать буквы и цифры")))
            return@flow
        }
        
        // Проверка совпадения паролей
        if (password != confirmPassword) {
            emit(Result.failure(Exception("Пароли не совпадают")))
            return@flow
        }
        
        try {
            val result = authRepository.register(
                firstName.trim(),
                lastName.trim(),
                email.trim(),
                password
            )
            emit(result)
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    private fun isValidPassword(password: String): Boolean {
        // Пароль должен содержать хотя бы одну букву и одну цифру
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return hasLetter && hasDigit
    }
}
