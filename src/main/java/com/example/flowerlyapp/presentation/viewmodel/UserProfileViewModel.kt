package com.example.flowerlyapp.presentation.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flowerlyapp.data.database.entities.UserProfileEntity
import com.example.flowerlyapp.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserProfileViewModel(private val context: Context) : ViewModel() {
    
    private val userProfileRepository = UserProfileRepository(context)
    
    private val _userProfile = MutableStateFlow<UserProfileEntity?>(null)
    val userProfile: StateFlow<UserProfileEntity?> = _userProfile.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * Загрузить профиль пользователя
     */
    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                userProfileRepository.getUserProfileFlow(userId).collect { profile ->
                    _userProfile.value = profile
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки профиля: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Создать профиль пользователя
     */
    fun createUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val profile = userProfileRepository.createUserProfile(userId)
                _userProfile.value = profile
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Ошибка создания профиля: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Получить или создать профиль пользователя
     */
    fun getOrCreateUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val profile = userProfileRepository.getOrCreateUserProfile(userId)
                _userProfile.value = profile
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Ошибка получения профиля: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Обновить профиль пользователя
     */
    fun updateUserProfile(profile: UserProfileEntity) {
        viewModelScope.launch {
            try {
                userProfileRepository.updateUserProfile(profile)
                _userProfile.value = profile
            } catch (e: Exception) {
                _error.value = "Ошибка обновления профиля: ${e.message}"
            }
        }
    }
    
    /**
     * Обновить номер телефона
     */
    fun updatePhoneNumber(userId: String, phoneNumber: String) {
        viewModelScope.launch {
            try {
                userProfileRepository.updatePhoneNumber(userId, phoneNumber)
                // Обновляем локальный профиль
                _userProfile.value?.let { profile ->
                    _userProfile.value = profile.copy(phoneNumber = phoneNumber)
                }
            } catch (e: Exception) {
                _error.value = "Ошибка обновления номера телефона: ${e.message}"
            }
        }
    }
    
    /**
     * Обновить адрес
     */
    fun updateAddress(userId: String, address: String, city: String, postalCode: String) {
        viewModelScope.launch {
            try {
                userProfileRepository.updateAddress(userId, address, city, postalCode)
                // Обновляем локальный профиль
                _userProfile.value?.let { profile ->
                    _userProfile.value = profile.copy(
                        address = address,
                        city = city,
                        postalCode = postalCode
                    )
                }
            } catch (e: Exception) {
                _error.value = "Ошибка обновления адреса: ${e.message}"
            }
        }
    }
    
    /**
     * Обновить аватар
     */
    fun updateAvatar(userId: String, avatarUrl: String) {
        viewModelScope.launch {
            try {
                userProfileRepository.updateAvatar(userId, avatarUrl)
                // Обновляем локальный профиль
                _userProfile.value?.let { profile ->
                    _userProfile.value = profile.copy(avatarUrl = avatarUrl)
                }
            } catch (e: Exception) {
                _error.value = "Ошибка обновления аватара: ${e.message}"
            }
        }
    }
    
    /**
     * Обновить настройки уведомлений
     */
    fun updateNotificationSettings(
        userId: String,
        emailNotifications: Boolean,
        smsNotifications: Boolean,
        pushNotifications: Boolean
    ) {
        viewModelScope.launch {
            try {
                userProfileRepository.updateNotificationSettings(
                    userId,
                    emailNotifications,
                    smsNotifications,
                    pushNotifications
                )
                // Обновляем локальный профиль
                _userProfile.value?.let { profile ->
                    _userProfile.value = profile.copy(
                        isEmailNotificationsEnabled = emailNotifications,
                        isSmsNotificationsEnabled = smsNotifications,
                        isPushNotificationsEnabled = pushNotifications
                    )
                }
            } catch (e: Exception) {
                _error.value = "Ошибка обновления настроек уведомлений: ${e.message}"
            }
        }
    }
    
    /**
     * Очистить ошибки
     */
    fun clearError() {
        _error.value = null
    }
}
