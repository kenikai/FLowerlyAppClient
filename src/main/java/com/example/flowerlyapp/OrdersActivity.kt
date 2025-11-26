package com.example.flowerlyapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.flowerlyapp.databinding.ActivityOrdersBinding

class OrdersActivity : AppCompatActivity(), OrdersFragment.OrdersCountListener {

    private lateinit var binding: ActivityOrdersBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Применяем тему перед setContentView
        com.example.flowerlyapp.data.network.ThemeManager.applyTheme()
        
        enableEdgeToEdge()
        
        binding = ActivityOrdersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Настройка кнопки назад
        binding.backButton.setOnClickListener {
            finish()
        }

        // Заменяем контент на OrdersFragment
        if (savedInstanceState == null) {
            val fragment = OrdersFragment.newInstance()
            fragment.setOrdersCountListener(this)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }
    
    override fun onOrdersCountUpdated(count: Int) {
        val countText = "$count заказов"
        binding.ordersCount.text = countText
    }
}
