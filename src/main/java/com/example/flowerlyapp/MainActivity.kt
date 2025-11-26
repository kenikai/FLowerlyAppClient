package com.example.flowerlyapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.flowerlyapp.databinding.ActivityMainBinding
import com.example.flowerlyapp.presentation.viewmodel.AuthViewModel
import com.example.flowerlyapp.presentation.viewmodel.AuthViewModelFactory
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(this)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Применяем тему перед setContentView
        com.example.flowerlyapp.data.network.ThemeManager.applyTheme()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Проверяем аутентификацию
        checkAuthentication()
        
        setupBottomNavigation()
    }
    
    private fun checkAuthentication() {
        if (!authViewModel.isLoggedIn()) {
            // Если пользователь не авторизован, переходим на экран входа
            val intent = Intent(this, LoginUserActivity::class.java)
            startActivity(intent)
            finish()
            return
        }
        
        // Пользователь авторизован, показываем информацию о нем
        val userId = authViewModel.getCurrentUserId()
        val userEmail = authViewModel.getCurrentUserEmail()
        
        // Можно добавить логику для отображения информации о пользователе
    }
    
    private fun setupBottomNavigation() {
        val bottomNavView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navigationView = findNavController(R.id.fragment_container)
        
        bottomNavView.setupWithNavController(navigationView)
    }
    
    
}