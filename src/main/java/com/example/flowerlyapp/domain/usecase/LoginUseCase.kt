package com.example.flowerlyapp.domain.usecase

import com.example.flowerlyapp.data.models.AuthResponse
import com.example.flowerlyapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LoginUseCase(private val authRepository: AuthRepository) {
    
    suspend operator fun invoke(email: String, password: String): Flow<Result<AuthResponse>> = flow {
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
        
        try {
            val result = authRepository.login(email, password)
            emit(result)
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
