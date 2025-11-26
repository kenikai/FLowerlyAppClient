package com.example.flowerlyapp.domain.usecase

import com.example.flowerlyapp.data.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LogoutUseCase(private val authRepository: AuthRepository) {
    
    suspend operator fun invoke(): Flow<Result<Unit>> = flow {
        try {
            val result = authRepository.logout()
            emit(result)
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}
