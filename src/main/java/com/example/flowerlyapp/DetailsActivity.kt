package com.example.flowerlyapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.example.flowerlyapp.databinding.ActivityDetailsBinding
import com.example.flowerlyapp.presentation.viewmodel.AuthViewModelFactory
import com.example.flowerlyapp.presentation.viewmodel.FavoriteViewModel
import com.example.flowerlyapp.presentation.viewmodel.CartViewModel
import com.example.flowerlyapp.data.network.TokenManager
import com.example.flowerlyapp.data.utils.ImageResourceMapper

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityDetailsBinding
    
    // ViewModels
    private lateinit var favoriteViewModel: FavoriteViewModel
    private lateinit var cartViewModel: CartViewModel
    
    // State
    private var currentUserId: String? = null
    private var flowerId: String? = null
    private var isFavorite: Boolean = false
    private var flowerPrice: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Применяем тему перед setContentView
        com.example.flowerlyapp.data.network.ThemeManager.applyTheme()
        
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация ViewModels
        favoriteViewModel = ViewModelProvider(this, AuthViewModelFactory(this))[FavoriteViewModel::class.java]
        cartViewModel = ViewModelProvider(this, AuthViewModelFactory(this))[CartViewModel::class.java]
        
        // Получаем user_id
        currentUserId = TokenManager.getUserId()
        val isLoggedIn = TokenManager.isLoggedIn()
        android.util.Log.d("DetailsActivity", "onCreate: currentUserId = $currentUserId")
        android.util.Log.d("DetailsActivity", "onCreate: isLoggedIn = $isLoggedIn")

        // Получаем данные цветка
        val flowerImage = intent.getIntExtra("flowerImage", 0)
        val flowerName = intent.getStringExtra("flowerName")
        val flowerDescription = intent.getStringExtra("flowerDescription")
        val flowerCompose = intent.getStringExtra("flowerCompose")
        flowerPrice = intent.getDoubleExtra("flowerPrice", 0.0)
        flowerId = intent.getStringExtra("flowerId")

        // Устанавливаем данные цветка
        binding.menuDFlowerImage.setImageResource(ImageResourceMapper.getImageResource(flowerImage))
        binding.menuDFlowerName.text = flowerName
        binding.shortDescriptionOfFlower.text = flowerDescription
        binding.menuDFlowerCompositions.text = flowerCompose
        binding.flowerPrice.text = "${flowerPrice.toInt()} P"

        // Настройка кнопок
        setupButtons()
        
        // Проверяем статус избранного
        checkFavoriteStatus()

        binding.backHome.setOnClickListener {
            finish()
        }
    }
    
    private fun setupButtons() {
        // Кнопка избранного
        binding.favoriteButton.setOnClickListener {
            if (currentUserId != null && flowerId != null) {
                toggleFavorite()
            } else {
                Toast.makeText(this, "Необходимо войти в систему", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Кнопка добавления в корзину
        binding.addToCartButton.setOnClickListener {
            if (currentUserId != null) {
                addToCart()
            } else {
                Toast.makeText(this, "Необходимо войти в систему", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun toggleFavorite() {
        currentUserId?.let { userId ->
            flowerId?.let { id ->
                android.util.Log.d("DetailsActivity", "Переключаем избранное: userId=$userId, flowerId=$id")
                favoriteViewModel.toggleFavorite(userId, id)
                
                // Обновляем UI
                isFavorite = !isFavorite
                updateFavoriteButton()
                
                val message = if (isFavorite) "Добавлено в избранное" else "Удалено из избранного"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun addToCart() {
        currentUserId?.let { userId ->
            flowerId?.let { id ->
                android.util.Log.d("DetailsActivity", "Добавляем в корзину: userId=$userId, flowerId=$id, price=$flowerPrice")
                cartViewModel.addToCart(userId, id, 1, flowerPrice)
                Toast.makeText(this, "Добавлено в корзину", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            android.util.Log.e("DetailsActivity", "ОШИБКА: currentUserId или flowerId равен null!")
            Toast.makeText(this, "Ошибка: товар не может быть добавлен в корзину", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun checkFavoriteStatus() {
        currentUserId?.let { userId ->
            flowerId?.let { id ->
                favoriteViewModel.isFavorite(userId, id) { favorite ->
                    isFavorite = favorite
                    updateFavoriteButton()
                }
            }
        }
    }
    
    private fun updateFavoriteButton() {
        if (isFavorite) {
            binding.favoriteButton.setImageResource(R.drawable.favorites_full_btn)
        } else {
            binding.favoriteButton.setImageResource(R.drawable.favorites_btn)
        }
    }
}