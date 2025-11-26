package com.example.flowerlyapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flowerlyapp.Adapters.FavoriteAdapter
import com.example.flowerlyapp.databinding.FragmentFavoritesBinding
import com.example.flowerlyapp.presentation.viewmodel.AuthViewModelFactory
import com.example.flowerlyapp.presentation.viewmodel.FavoriteViewModel
import com.example.flowerlyapp.presentation.viewmodel.CartViewModel
import com.example.flowerlyapp.data.network.TokenManager
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {
    
    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    
    // ViewModels
    private val favoriteViewModel: FavoriteViewModel by viewModels {
        AuthViewModelFactory(requireContext())
    }
    private val cartViewModel: CartViewModel by viewModels {
        AuthViewModelFactory(requireContext())
    }
    
    // UI Components
    private lateinit var recyclerViewFavorites: RecyclerView
    private lateinit var emptyFavoritesLayout: View
    private lateinit var favoriteCount: TextView
    
    // Adapter
    private lateinit var favoritesAdapter: FavoriteAdapter
    
    // State
    private var currentUserId: String? = null
    
    // SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences
    
    companion object {
        @JvmStatic
        fun newInstance() = FavoritesFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Инициализируем SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        
        // Получаем user_id из TokenManager (правильный способ)
        currentUserId = TokenManager.getUserId()
        val isLoggedIn = TokenManager.isLoggedIn()
        android.util.Log.d("FavoritesFragment", "onCreate: currentUserId = $currentUserId")
        android.util.Log.d("FavoritesFragment", "onCreate: isLoggedIn = $isLoggedIn")
        
        // Если пользователь не авторизован, показываем сообщение
        if (!isLoggedIn || currentUserId == null) {
            android.util.Log.w("FavoritesFragment", "Пользователь не авторизован! Избранное недоступно.")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews()
        setupRecyclerView()
        observeData()
        loadFavorites()
    }
    
    private fun initViews() {
        recyclerViewFavorites = binding.recyclerViewFavorites
        emptyFavoritesLayout = binding.emptyFavoritesLayout
        favoriteCount = binding.favoriteCount
    }
    
    private fun setupRecyclerView() {
        favoritesAdapter = FavoriteAdapter(
            requireContext(),
            mutableListOf(),
            onRemoveFromFavorites = { flowerId ->
                currentUserId?.let { userId ->
                    // Обновляем иконку перед удалением
                    favoritesAdapter.updateFavoriteIcon(flowerId, false)
                    favoriteViewModel.removeFromFavorites(userId, flowerId)
                    Toast.makeText(requireContext(), "Удалено из избранного", Toast.LENGTH_SHORT).show()
                }
            },
            onAddToCart = { flower ->
                currentUserId?.let { userId ->
                    android.util.Log.d("FavoritesFragment", "=== ДОБАВЛЕНИЕ В КОРЗИНУ ИЗ ИЗБРАННОГО ===")
                    android.util.Log.d("FavoritesFragment", "userId: $userId, flowerId: ${flower.id}, flowerName: ${flower.name}, price: ${flower.price}")
                    cartViewModel.addToCart(userId, flower.id, 1, flower.price)
                    Toast.makeText(requireContext(), "Добавлено в корзину", Toast.LENGTH_SHORT).show()
                } ?: run {
                    android.util.Log.e("FavoritesFragment", "ОШИБКА: currentUserId равен null!")
                    Toast.makeText(requireContext(), "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
                }
            }
        )

        recyclerViewFavorites.apply {
            layoutManager = GridLayoutManager(requireContext(), 1)
            adapter = favoritesAdapter
        }
    }
    
    private fun observeData() {
        // Наблюдение за избранными цветами
        lifecycleScope.launch {
            favoriteViewModel.favoriteFlowers.collect { flowers ->
                android.util.Log.d("FavoritesFragment", "=== ПОЛУЧЕНЫ ИЗБРАННЫЕ ЦВЕТЫ ===")
                android.util.Log.d("FavoritesFragment", "Количество избранных: ${flowers.size}")
                android.util.Log.d("FavoritesFragment", "Детали: ${flowers.map { it.name }}")
                
                favoritesAdapter.updateItems(flowers)
                updateEmptyState(flowers.isEmpty())
                updateFavoriteCount(flowers.size)
            }
        }
        
        // Наблюдение за количеством избранных
        lifecycleScope.launch {
            favoriteViewModel.favoriteCount.collect { count ->
                android.util.Log.d("FavoritesFragment", "=== ПОЛУЧЕНО КОЛИЧЕСТВО ИЗБРАННЫХ ===")
                android.util.Log.d("FavoritesFragment", "Количество избранных: $count")
                updateFavoriteCount(count)
            }
        }
        
        // Наблюдение за ошибками
        lifecycleScope.launch {
            favoriteViewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    favoriteViewModel.clearError()
                }
            }
        }
    }
    
    private fun loadFavorites() {
        android.util.Log.d("FavoritesFragment", "=== ЗАГРУЗКА ИЗБРАННОГО ===")
        android.util.Log.d("FavoritesFragment", "currentUserId: $currentUserId")
        
        currentUserId?.let { userId ->
            android.util.Log.d("FavoritesFragment", "Вызываем favoriteViewModel.loadFavoriteFlowers($userId)")
            favoriteViewModel.loadFavoriteFlowers(userId)
            android.util.Log.d("FavoritesFragment", "Вызываем favoriteViewModel.loadFavoriteCount($userId)")
            favoriteViewModel.loadFavoriteCount(userId)
        } ?: run {
            android.util.Log.e("FavoritesFragment", "ОШИБКА: currentUserId равен null!")
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        emptyFavoritesLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerViewFavorites.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    private fun updateFavoriteCount(count: Int) {
        android.util.Log.d("FavoritesFragment", "updateFavoriteCount: $count")
        // Обновляем счетчик избранных товаров
        if (::favoriteCount.isInitialized) {
            try {
                val countText = "$count цветов"
                android.util.Log.d("FavoritesFragment", "Обновляем счетчик: $countText")
                favoriteCount.text = countText
            } catch (e: Exception) {
                android.util.Log.e("FavoritesFragment", "Ошибка обновления счетчика: ${e.message}")
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}