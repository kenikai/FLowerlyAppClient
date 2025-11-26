package com.example.flowerlyapp.presentation.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowerlyapp.data.models.AuthResponse
import com.example.flowerlyapp.data.repository.AuthRepository
import com.example.flowerlyapp.domain.usecase.LoginUseCase
import com.example.flowerlyapp.domain.usecase.LogoutUseCase
import com.example.flowerlyapp.domain.usecase.RegisterUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val context: Context) : ViewModel() {
    
    private val authRepository = AuthRepository(context)
    private val loginUseCase = LoginUseCase(authRepository)
    private val registerUseCase = RegisterUseCase(authRepository)
    private val logoutUseCase = LogoutUseCase(authRepository)
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    fun login(email: String, password: String) {
        Log.d("AuthViewModel", "Попытка входа: email=$email")
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Loading
            
            loginUseCase(email, password).collect { result ->
                _isLoading.value = false
                if (result.isSuccess) {
                    Log.d("AuthViewModel", "✅ Вход успешен")
                    _authState.value = AuthState.Success(result.getOrNull())
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Неизвестная ошибка"
                    Log.e("AuthViewModel", "❌ Ошибка входа: $error")
                    _authState.value = AuthState.Error(error)
                }
            }
        }
    }
    
    fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        Log.d("AuthViewModel", "Попытка регистрации: email=$email, firstName=$firstName, lastName=$lastName")
        viewModelScope.launch {
            _isLoading.value = true
            _authState.value = AuthState.Loading
            
            registerUseCase(firstName, lastName, email, password, confirmPassword).collect { result ->
                _isLoading.value = false
                if (result.isSuccess) {
                    Log.d("AuthViewModel", "✅ Регистрация успешна")
                    _authState.value = AuthState.Success(result.getOrNull())
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Неизвестная ошибка"
                    Log.e("AuthViewModel", "❌ Ошибка регистрации: $error")
                    _authState.value = AuthState.Error(error)
                }
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            
            logoutUseCase().collect { result ->
                _isLoading.value = false
                if (result.isSuccess) {
                    _authState.value = AuthState.LoggedOut
                } else {
                    _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Ошибка выхода")
                }
            }
        }
    }
    
    fun clearState() {
        _authState.value = AuthState.Idle
    }
    
    fun isLoggedIn(): Boolean {
        return authRepository.isLoggedIn()
    }
    
    fun getCurrentUserId(): String? {
        return authRepository.getCurrentUserId()
    }
    
    fun getCurrentUserEmail(): String? {
        return authRepository.getCurrentUserEmail()
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val authResponse: AuthResponse?) : AuthState()
    data class Error(val message: String) : AuthState()
    object LoggedOut : AuthState()
}
