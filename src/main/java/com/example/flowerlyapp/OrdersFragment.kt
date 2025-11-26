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
import com.example.flowerlyapp.Adapters.OrdersAdapter
import com.example.flowerlyapp.databinding.FragmentOrdersBinding
import com.example.flowerlyapp.presentation.viewmodel.AuthViewModelFactory
import com.example.flowerlyapp.presentation.viewmodel.OrderViewModel
import com.example.flowerlyapp.data.network.TokenManager
import kotlinx.coroutines.launch

class OrdersFragment : Fragment() {
    
    // Интерфейс для связи с родительской активностью
    interface OrdersCountListener {
        fun onOrdersCountUpdated(count: Int)
    }
    
    private var ordersCountListener: OrdersCountListener? = null
    
    private var _binding: FragmentOrdersBinding? = null
    private val binding get() = _binding!!
    
    // ViewModels
    private val orderViewModel: OrderViewModel by viewModels { AuthViewModelFactory(requireContext()) }
    
    // UI Components
    private lateinit var recyclerViewOrders: RecyclerView
    private lateinit var emptyOrdersLayout: View
    
    // Adapter
    private lateinit var ordersAdapter: OrdersAdapter
    
    // State
    private var currentUserId: String? = null
    
    companion object {
        @JvmStatic
        fun newInstance() = OrdersFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Получаем user_id
        currentUserId = TokenManager.getUserId()
        val isLoggedIn = TokenManager.isLoggedIn()
        android.util.Log.d("OrdersFragment", "onCreate: currentUserId = $currentUserId")
        android.util.Log.d("OrdersFragment", "onCreate: isLoggedIn = $isLoggedIn")
        
        // Если пользователь не авторизован, показываем сообщение
        if (!isLoggedIn || currentUserId == null) {
            android.util.Log.w("OrdersFragment", "Пользователь не авторизован! Заказы недоступны.")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews()
        setupRecyclerView()
        observeData()
        loadOrders()
    }
    
    private fun initViews() {
        recyclerViewOrders = binding.recyclerViewOrders
        emptyOrdersLayout = binding.emptyOrdersLayout
    }
    
    private fun setupRecyclerView() {
        ordersAdapter = OrdersAdapter(
            requireContext(),
            emptyList(),
            onOrderClick = { order ->
                // TODO: Переход к деталям заказа
                Toast.makeText(requireContext(), "Заказ #${order.id}", Toast.LENGTH_SHORT).show()
            }
        )
        
        recyclerViewOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ordersAdapter
        }
    }
    
    private fun observeData() {
        lifecycleScope.launch {
            orderViewModel.orders.collect { orders ->
                android.util.Log.d("OrdersFragment", "=== ПОЛУЧЕНЫ ЗАКАЗЫ ===")
                android.util.Log.d("OrdersFragment", "Количество заказов: ${orders.size}")
                orders.forEach { order ->
                    android.util.Log.d("OrdersFragment", "Заказ: ${order.id}, статус: ${order.status}, сумма: ${order.totalAmount}")
                }
                ordersAdapter.updateOrders(orders)
                updateEmptyState(orders.isEmpty())
                updateOrdersCount(orders.size)
            }
        }
        
        lifecycleScope.launch {
            orderViewModel.isLoading.collect { isLoading ->
                android.util.Log.d("OrdersFragment", "isLoading: $isLoading")
                // TODO: Показать/скрыть ProgressBar
            }
        }
        
        lifecycleScope.launch {
            orderViewModel.error.collect { error ->
                if (error != null) {
                    android.util.Log.e("OrdersFragment", "Ошибка: $error")
                    // Показываем Toast только если это не отмена корутины
                    if (!error.contains("cancelled", ignoreCase = true)) {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
                    }
                    orderViewModel.clearError()
                }
            }
        }
    }
    
    private fun loadOrders() {
        android.util.Log.d("OrdersFragment", "=== ЗАГРУЗКА ЗАКАЗОВ ===")
        android.util.Log.d("OrdersFragment", "currentUserId: $currentUserId")
        
        currentUserId?.let { userId ->
            android.util.Log.d("OrdersFragment", "Загружаем заказы для пользователя: $userId")
            try {
                orderViewModel.loadOrders(userId)
                android.util.Log.d("OrdersFragment", "loadOrders вызван успешно")
            } catch (e: Exception) {
                android.util.Log.e("OrdersFragment", "Ошибка при вызове loadOrders: ${e.message}", e)
            }
        } ?: run {
            android.util.Log.e("OrdersFragment", "ОШИБКА: currentUserId равен null!")
            updateEmptyState(true)
        }
    }
    
    private fun updateEmptyState(isEmpty: Boolean) {
        android.util.Log.d("OrdersFragment", "updateEmptyState: $isEmpty")
        
        recyclerViewOrders.visibility = if (isEmpty) View.GONE else View.VISIBLE
        emptyOrdersLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        
        if (isEmpty) {
            val isLoggedIn = TokenManager.isLoggedIn()
            val userId = TokenManager.getUserId()
            android.util.Log.d("OrdersFragment", "Пустое состояние: isLoggedIn=$isLoggedIn, userId=$userId")
        }
    }
    
    private fun updateOrdersCount(count: Int) {
        android.util.Log.d("OrdersFragment", "updateOrdersCount: $count")
        
        // Уведомляем родительскую активность
        ordersCountListener?.onOrdersCountUpdated(count)
    }
    
    fun setOrdersCountListener(listener: OrdersCountListener?) {
        ordersCountListener = listener
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
