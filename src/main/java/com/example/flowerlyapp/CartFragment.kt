package com.example.flowerlyapp

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flowerlyapp.Adapters.CartAdapter
import com.example.flowerlyapp.databinding.FragmentCartBinding
import com.example.flowerlyapp.presentation.viewmodel.AuthViewModelFactory
import com.example.flowerlyapp.presentation.viewmodel.CartViewModel
import com.example.flowerlyapp.presentation.viewmodel.OrderViewModel
import com.example.flowerlyapp.data.network.TokenManager
import kotlinx.coroutines.launch

class CartFragment : Fragment() {
    
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    
    // ViewModels
    private val cartViewModel: CartViewModel by viewModels { AuthViewModelFactory(requireContext()) }
    private val orderViewModel: OrderViewModel by viewModels { AuthViewModelFactory(requireContext()) }
    
    // UI Components
    private lateinit var recyclerViewCart: RecyclerView
    private lateinit var emptyCartLayout: View
    private lateinit var cartItemCount: TextView
    private lateinit var totalPrice: TextView
    private lateinit var btnCheckout: View
    private lateinit var btnClearCart: View
    
    // Adapter
    private lateinit var cartAdapter: CartAdapter
    
    // State
    private var currentUserId: String? = null
    
    companion object {
        @JvmStatic
        fun newInstance() = CartFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Получаем user_id
        currentUserId = TokenManager.getUserId()
        val isLoggedIn = TokenManager.isLoggedIn()
        android.util.Log.d("CartFragment", "onCreate: currentUserId = $currentUserId")
        android.util.Log.d("CartFragment", "onCreate: isLoggedIn = $isLoggedIn")
        
        // Если пользователь не авторизован, показываем сообщение
        if (!isLoggedIn || currentUserId == null) {
            android.util.Log.w("CartFragment", "Пользователь не авторизован! Корзина недоступна.")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews()
        setupRecyclerView()
        observeData()
        loadCartItems()
    }
    
    override fun onResume() {
        super.onResume()
        // Обновляем currentUserId при возвращении к фрагменту
        currentUserId = TokenManager.getUserId()
        android.util.Log.d("CartFragment", "onResume: currentUserId = $currentUserId")
        
        loadCartItems()
    }
    
    private fun initViews() {
        recyclerViewCart = binding.recyclerViewCart
        emptyCartLayout = binding.emptyCartLayout
        cartItemCount = binding.cartItemCount
        totalPrice = binding.totalPrice
        btnCheckout = binding.btnCheckout
        btnClearCart = binding.btnClearCart
    }
    
    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            requireContext(),
            emptyList(),
            onQuantityChanged = { flowerId, newQuantity ->
                android.util.Log.d("CartFragment", "=== ИЗМЕНЕНИЕ КОЛИЧЕСТВА ===")
                android.util.Log.d("CartFragment", "flowerId: $flowerId, newQuantity: $newQuantity")
                currentUserId?.let { userId ->
                    android.util.Log.d("CartFragment", "Вызываем cartViewModel.updateCartItemQuantity($userId, $flowerId, $newQuantity)")
                    cartViewModel.updateCartItemQuantity(userId, flowerId, newQuantity)
                } ?: run {
                    android.util.Log.e("CartFragment", "ОШИБКА: currentUserId равен null!")
                }
            },
            onRemoveItem = { flowerId ->
                android.util.Log.d("CartFragment", "=== УДАЛЕНИЕ ТОВАРА ===")
                android.util.Log.d("CartFragment", "flowerId: $flowerId")
                currentUserId?.let { userId ->
                    android.util.Log.d("CartFragment", "Вызываем cartViewModel.removeFromCart($userId, $flowerId)")
                    cartViewModel.removeFromCart(userId, flowerId)
                } ?: run {
                    android.util.Log.e("CartFragment", "ОШИБКА: currentUserId равен null!")
                }
            }
        )

        recyclerViewCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
        
        // Обработчики кнопок
        btnCheckout.setOnClickListener {
            if (currentUserId != null) {
                checkout()
            } else {
                Toast.makeText(requireContext(), "Необходимо войти в систему", Toast.LENGTH_SHORT).show()
            }
        }
        
        btnClearCart.setOnClickListener {
            if (currentUserId != null) {
                clearCart()
            } else {
                Toast.makeText(requireContext(), "Необходимо войти в систему", Toast.LENGTH_SHORT).show()
            }
        }
        
    }
    
    private fun observeData() {
        // Наблюдение за товарами в корзине
        lifecycleScope.launch {
            cartViewModel.cartItems.collect { items ->
                android.util.Log.d("CartFragment", "=== ПОЛУЧЕНЫ ТОВАРЫ КОРЗИНЫ ===")
                android.util.Log.d("CartFragment", "Количество товаров: ${items.size}")
                android.util.Log.d("CartFragment", "Детали: ${items.map { "${it.flowerName} (${it.quantity})" }}")
                
                // Логируем детали каждого товара
                items.forEach { item ->
                    android.util.Log.d("CartFragment", "Товар: ${item.flowerName}, количество: ${item.quantity}, цена: ${item.unitPrice}")
                }
                
                cartAdapter.updateItems(items)
                updateEmptyState(items.isEmpty())
            }
        }
        
        // Наблюдение за количеством товаров
        lifecycleScope.launch {
            cartViewModel.cartItemCount.collect { count ->
                android.util.Log.d("CartFragment", "=== ПОЛУЧЕНО КОЛИЧЕСТВО ТОВАРОВ ===")
                android.util.Log.d("CartFragment", "Количество товаров: $count")
                updateCartItemCount(count)
            }
        }
        
        // Наблюдение за общей стоимостью
        lifecycleScope.launch {
            cartViewModel.cartTotal.collect { total ->
                android.util.Log.d("CartFragment", "=== ПОЛУЧЕНА ОБЩАЯ СТОИМОСТЬ ===")
                android.util.Log.d("CartFragment", "Общая стоимость: $total")
                updateTotalPrice(total)
            }
        }
        
        // Наблюдение за ошибками
        lifecycleScope.launch {
            cartViewModel.error.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    cartViewModel.clearError()
                }
            }
        }
    }
    
    private fun loadCartItems() {
        android.util.Log.d("CartFragment", "=== ЗАГРУЗКА КОРЗИНЫ ===")
        android.util.Log.d("CartFragment", "currentUserId: $currentUserId")
        
        // Проверяем авторизацию еще раз
        val isLoggedIn = TokenManager.isLoggedIn()
        val userId = TokenManager.getUserId()
        val accessToken = TokenManager.getAccessToken()
        val userEmail = TokenManager.getUserEmail()
        
        android.util.Log.d("CartFragment", "isLoggedIn: $isLoggedIn")
        android.util.Log.d("CartFragment", "userId: $userId")
        android.util.Log.d("CartFragment", "accessToken: $accessToken")
        android.util.Log.d("CartFragment", "userEmail: $userEmail")
        
        if (isLoggedIn && userId != null) {
            android.util.Log.d("CartFragment", "Пользователь авторизован, загружаем корзину")
            android.util.Log.d("CartFragment", "Вызываем cartViewModel.loadCartItems($userId)")
            cartViewModel.loadCartItems(userId)
            // Также загружаем количество и общую стоимость
            cartViewModel.loadCartItemCount(userId)
            cartViewModel.loadCartTotal(userId)
        } else {
            android.util.Log.e("CartFragment", "ОШИБКА: Пользователь не авторизован!")
            android.util.Log.e("CartFragment", "isLoggedIn: $isLoggedIn, userId: $userId")
            android.util.Log.e("CartFragment", "accessToken: $accessToken, userEmail: $userEmail")
            // Показываем пустое состояние, если пользователь не авторизован
            updateEmptyState(true)
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        emptyCartLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerViewCart.visibility = if (isEmpty) View.GONE else View.VISIBLE
        
        // Добавляем дополнительную информацию в пустое состояние
        if (isEmpty) {
            val isLoggedIn = TokenManager.isLoggedIn()
            val userId = TokenManager.getUserId()
            android.util.Log.d("CartFragment", "Пустое состояние: isLoggedIn=$isLoggedIn, userId=$userId")
        }
    }
    
    private fun updateCartItemCount(count: Int) {
        android.util.Log.d("CartFragment", "updateCartItemCount: $count")
        if (::cartItemCount.isInitialized) {
            try {
                val countText = "$count товаров"
                android.util.Log.d("CartFragment", "Обновляем счетчик: $countText")
                cartItemCount.text = countText
            } catch (e: Exception) {
                android.util.Log.e("CartFragment", "Ошибка обновления счетчика: ${e.message}")
            }
        }
    }
    
    private fun updateTotalPrice(total: Double) {
        android.util.Log.d("CartFragment", "updateTotalPrice: $total")
        if (::totalPrice.isInitialized) {
            try {
                val totalText = "${total.toInt()} ₽"
                android.util.Log.d("CartFragment", "Обновляем общую стоимость: $totalText")
                totalPrice.text = totalText
            } catch (e: Exception) {
                android.util.Log.e("CartFragment", "Ошибка обновления общей стоимости: ${e.message}")
            }
        }
    }
    
    private fun checkout() {
        android.util.Log.d("CartFragment", "=== ОФОРМЛЕНИЕ ЗАКАЗА ===")
        currentUserId?.let { userId ->
            android.util.Log.d("CartFragment", "currentUserId: $userId")
            // Получаем текущие товары в корзине
            lifecycleScope.launch {
                try {
                    // Получаем товары из корзины
                    val cartItems = cartViewModel.cartItems.value
                    android.util.Log.d("CartFragment", "Товаров в корзине: ${cartItems.size}")
                    if (cartItems.isNotEmpty()) {
                        // Создаем заказ из корзины
                        orderViewModel.createOrderFromCart(
                            userId = userId,
                            cartItems = cartItems,
                            deliveryAddress = null, // TODO: Добавить поле адреса
                            notes = null // TODO: Добавить поле заметок
                        )
                        
                        // Очищаем корзину после создания заказа
                        cartViewModel.clearCart(userId)
                        
                        Toast.makeText(requireContext(), "Заказ оформлен! Проверьте раздел 'Мои заказы'", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(requireContext(), "Корзина пуста", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CartFragment", "Ошибка оформления заказа: ${e.message}", e)
                    Toast.makeText(requireContext(), "Ошибка оформления заказа: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        } ?: run {
            Toast.makeText(requireContext(), "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun clearCart() {
        currentUserId?.let { userId ->
            cartViewModel.clearCart(userId)
            Toast.makeText(requireContext(), "Корзина очищена", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun testAuth() {
        android.util.Log.d("CartFragment", "=== ТЕСТ АВТОРИЗАЦИИ ===")
        val isLoggedIn = TokenManager.isLoggedIn()
        val userId = TokenManager.getUserId()
        val accessToken = TokenManager.getAccessToken()
        val userEmail = TokenManager.getUserEmail()
        
        android.util.Log.d("CartFragment", "isLoggedIn: $isLoggedIn")
        android.util.Log.d("CartFragment", "userId: $userId")
        android.util.Log.d("CartFragment", "accessToken: $accessToken")
        android.util.Log.d("CartFragment", "userEmail: $userEmail")
        
        // Принудительно устанавливаем тестового пользователя
        if (!isLoggedIn || userId == null) {
            android.util.Log.d("CartFragment", "Устанавливаем тестового пользователя")
            TokenManager.saveUserInfo("user123", "test@example.com")
            TokenManager.saveTokens("test_token")
            
            // Обновляем currentUserId
            currentUserId = "user123"
            
            // Перезагружаем корзину
            loadCartItems()
        }
    }
    
    
    private fun testDatabase() {
        android.util.Log.d("CartFragment", "=== ТЕСТ БАЗЫ ДАННЫХ ===")
        try {
            val database = com.example.flowerlyapp.data.database.FlowerlyDatabase.getDatabase(requireContext())
            android.util.Log.d("CartFragment", "База данных создана: $database")
            
            // Проверяем, есть ли цветы в базе
            val flowerDao = database.flowerDao()
            android.util.Log.d("CartFragment", "FlowerDao создан: $flowerDao")
            
        } catch (e: Exception) {
            android.util.Log.e("CartFragment", "Ошибка создания базы данных: ${e.message}", e)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}