package com.example.flowerlyapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.flowerlyapp.presentation.viewmodel.AuthViewModel
import com.example.flowerlyapp.presentation.viewmodel.AuthViewModelFactory

class SplashActivity : AppCompatActivity() {
    
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(this)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        enableEdgeToEdge()
        
        // Задержка для показа splash screen
        Handler(Looper.getMainLooper()).postDelayed({
            checkAuthenticationAndNavigate()
        }, 2000)
    }
    
    private fun checkAuthenticationAndNavigate() {
        // Проверяем авторизацию более тщательно
        val isLoggedIn = authViewModel.isLoggedIn() && 
                        authViewModel.getCurrentUserId() != null && 
                        authViewModel.getCurrentUserEmail() != null
        
        val intent = if (isLoggedIn) {
            // Если пользователь уже авторизован, переходим в главное приложение
            Intent(this@SplashActivity, MainActivity::class.java)
        } else {
            // Если не авторизован, переходим на welcome page
            Intent(this@SplashActivity, WelcomePageActivity::class.java)
        }
        
        startActivity(intent)
        finish()
    }
}
