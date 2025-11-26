package com.example.flowerlyapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flowerlyapp.Adapters.ShopAdapter
import com.example.flowerlyapp.data.database.entities.FlowerEntity
import com.example.flowerlyapp.databinding.FragmentShopBinding
import com.example.flowerlyapp.presentation.viewmodel.AuthViewModelFactory
import com.example.flowerlyapp.presentation.viewmodel.FlowerViewModel
import com.example.flowerlyapp.presentation.viewmodel.CartViewModel
import com.example.flowerlyapp.presentation.viewmodel.FavoriteViewModel
import com.example.flowerlyapp.data.network.TokenManager
import org.json.JSONArray
import org.json.JSONObject

class ShopFragment : Fragment() {

    private var _binding: FragmentShopBinding? = null
    private val binding get() = _binding!!

    // ViewModels
    private val flowerViewModel: FlowerViewModel by viewModels {
        AuthViewModelFactory(requireContext())
    }
    
    init {
        android.util.Log.d("ShopFragment", "ShopFragment инициализирован")
    }
    
    private val favoriteViewModel: FavoriteViewModel by viewModels {
        AuthViewModelFactory(requireContext())
    }
    
    private val cartViewModel: CartViewModel by viewModels {
        AuthViewModelFactory(requireContext())
    }

    // UI Components
    private lateinit var searchEditText: EditText
    private lateinit var clearSearchButton: ImageView
    private lateinit var recyclerViewShop: RecyclerView
    private lateinit var emptyShopLayout: View
    private lateinit var progressBar: ProgressBar
    
    // New UI Components for placeholders and history
    private lateinit var noResultsLayout: LinearLayout
    private lateinit var errorLayout: LinearLayout
    private lateinit var refreshButton: Button
    private lateinit var searchHistoryLayout: LinearLayout
    private lateinit var searchHistoryList: ListView
    private lateinit var clearHistoryButton: Button
    private lateinit var refreshSearchButton: Button
    private lateinit var searchErrorLayout: LinearLayout
    private lateinit var retrySearchButton: Button

    // Adapter
    private lateinit var shopAdapter: ShopAdapter
    private lateinit var searchHistoryAdapter: ArrayAdapter<String>

    // State
    private var currentUserId: String? = null
    private var isSearchMode = false
    private var lastSearchQuery = ""
    private var isSearching = false
    private var searchHistory = mutableListOf<String>()
    private var searchStartTime = 0L
    private var favoriteFlowerIds = mutableSetOf<String>()
    
    // SharedPreferences for search history
    private lateinit var sharedPreferences: SharedPreferences

    // Saved state keys
    companion object {
        private const val KEY_SEARCH_TEXT = "search_text"
        private const val KEY_SEARCH_HISTORY = "search_history"
        private const val PREFS_NAME = "search_history_prefs"
        private const val MAX_HISTORY_SIZE = 10
        
        @JvmStatic
        fun newInstance() = ShopFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Инициализируем SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Получаем user_id из TokenManager (правильный способ)
        currentUserId = TokenManager.getUserId()
        val isLoggedIn = TokenManager.isLoggedIn()
        android.util.Log.d("ShopFragment", "onCreate: currentUserId = $currentUserId")
        android.util.Log.d("ShopFragment", "onCreate: isLoggedIn = $isLoggedIn")
        
        // Если пользователь не авторизован, показываем сообщение
        if (!isLoggedIn || currentUserId == null) {
            android.util.Log.w("ShopFragment", "Пользователь не авторизован! Функциональность избранного недоступна.")
        }
        
        // Восстанавливаем состояние
        savedInstanceState?.let {
            isSearchMode = it.getString(KEY_SEARCH_TEXT)?.isNotEmpty() == true
        }
        
        // Загружаем историю поиска
        loadSearchHistory()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentShopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initViews()
        setupRecyclerView()
        setupSearch()
        observeData()
        
        // Загружаем данные только если НЕ в режиме поиска
        if (!isSearchMode) {
            android.util.Log.d("ShopFragment", "onViewCreated: загружаем обычные данные")
            loadData()
        } else {
            android.util.Log.d("ShopFragment", "onViewCreated: пропускаем загрузку - мы в режиме поиска")
        }
        
        // Загружаем избранные цветы для текущего пользователя
        android.util.Log.d("ShopFragment", "=== ИНИЦИАЛИЗАЦИЯ ИЗБРАННОГО ===")
        android.util.Log.d("ShopFragment", "currentUserId: $currentUserId")
        
        currentUserId?.let { userId ->
            android.util.Log.d("ShopFragment", "Загружаем избранные цветы для пользователя: $userId")
            favoriteViewModel.loadFavoriteFlowers(userId)
        } ?: run {
            android.util.Log.e("ShopFragment", "ОШИБКА: currentUserId равен null! Пользователь не авторизован.")
        }
        
        // Восстанавливаем текст поиска
        savedInstanceState?.getString(KEY_SEARCH_TEXT)?.let { searchText ->
            searchEditText.setText(searchText)
            updateClearButtonVisibility()
        }
    }
    
    override fun onResume() {
        super.onResume()
        android.util.Log.d("ShopFragment", "onResume: принудительно обновляем UI")
        android.util.Log.d("ShopFragment", "onResume: isSearchMode=$isSearchMode, lastSearchQuery='$lastSearchQuery'")
        
        // Принудительно обновляем UI при возвращении на страницу
        if (isSearchMode && lastSearchQuery.isNotEmpty()) {
            android.util.Log.d("ShopFragment", "onResume: восстанавливаем результаты поиска для '$lastSearchQuery'")
            forceUpdateUI()
            
            // Дополнительная задержка для гарантированного обновления
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                android.util.Log.d("ShopFragment", "onResume: дополнительное обновление с задержкой")
                forceUpdateUI()
            }, 200)
        } else if (!isSearchMode) {
            android.util.Log.d("ShopFragment", "onResume: НЕ в режиме поиска, загружаем обычные данные")
            // Загружаем обычные данные только если НЕ в режиме поиска
            loadData()
        } else {
            android.util.Log.d("ShopFragment", "onResume: режим поиска, но нет запроса - ничего не делаем")
        }
    }

    private fun initViews() {
        searchEditText = binding.searchEditText
        clearSearchButton = binding.clearSearchButton
        recyclerViewShop = binding.recyclerViewShop
        emptyShopLayout = binding.emptyShopLayout
        progressBar = binding.progressBar
        android.util.Log.d("ShopFragment", "ProgressBar инициализирован: ${progressBar != null}")
        
        // Получаем элементы истории поиска из layout
        searchHistoryLayout = binding.searchHistoryLayout
        searchHistoryList = binding.searchHistoryList
        clearHistoryButton = binding.clearHistoryButton
        refreshSearchButton = binding.refreshSearchButton
        searchErrorLayout = binding.searchErrorLayout
        retrySearchButton = binding.retrySearchButton
        
        // Если их нет, создадим программно
        setupPlaceholderViews()
        setupSearchHistoryViews()
    }

    private fun setupRecyclerView() {
        shopAdapter = ShopAdapter(
            requireContext(),
            emptyList(),
            onAddToCart = { flower ->
                android.util.Log.d("ShopFragment", "=== НАЖАТА КНОПКА В КОРЗИНУ ===")
                android.util.Log.d("ShopFragment", "currentUserId: $currentUserId")
                android.util.Log.d("ShopFragment", "flower.id: ${flower.id}")
                android.util.Log.d("ShopFragment", "flower.name: ${flower.name}")
                android.util.Log.d("ShopFragment", "flower.price: ${flower.price}")
                
                currentUserId?.let { userId ->
                    android.util.Log.d("ShopFragment", "Вызываем cartViewModel.addToCart($userId, ${flower.id}, 1, ${flower.price})")
                    cartViewModel.addToCart(userId, flower.id, 1, flower.price)
                    Toast.makeText(requireContext(), "Добавлено в корзину", Toast.LENGTH_SHORT).show()
                } ?: run {
                    android.util.Log.e("ShopFragment", "ОШИБКА: currentUserId равен null!")
                    Toast.makeText(requireContext(), "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
                }
            },
            onToggleFavorite = { flower ->
                android.util.Log.d("ShopFragment", "=== НАЖАТА КНОПКА ИЗБРАННОГО ===")
                android.util.Log.d("ShopFragment", "currentUserId: $currentUserId")
                android.util.Log.d("ShopFragment", "flower.id: ${flower.id}")
                android.util.Log.d("ShopFragment", "flower.name: ${flower.name}")
                
                currentUserId?.let { userId ->
                    android.util.Log.d("ShopFragment", "Вызываем favoriteViewModel.toggleFavorite($userId, ${flower.id})")
                    favoriteViewModel.toggleFavorite(userId, flower.id)
                    val message = if (isFlowerFavorite(flower.id)) {
                        "Удалено из избранного"
                    } else {
                        "Добавлено в избранное"
                    }
                    android.util.Log.d("ShopFragment", "Сообщение: $message")
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                } ?: run {
                    android.util.Log.e("ShopFragment", "ОШИБКА: currentUserId равен null!")
                    Toast.makeText(requireContext(), "Ошибка: пользователь не авторизован", Toast.LENGTH_SHORT).show()
                }
            },
            isFavorite = { flowerId ->
                isFlowerFavorite(flowerId)
            }
        )

        recyclerViewShop.apply {
            layoutManager = GridLayoutManager(requireContext(), 1)
            adapter = shopAdapter
        }
    }

    private fun setupSearch() {
        // Обработчик изменения текста
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateClearButtonVisibility()
                hideSearchHistory() // Скрываем историю при вводе
            }
            
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                android.util.Log.d("ShopFragment", "=== afterTextChanged ===")
                android.util.Log.d("ShopFragment", "Запрос: '$query'")
                android.util.Log.d("ShopFragment", "Длина запроса: ${query.length}")
                
                if (query.isNotEmpty()) {
                    android.util.Log.d("ShopFragment", "Запускаем поиск из afterTextChanged для '$query'")
                    isSearchMode = true
                    performSearch(query)
                } else {
                    android.util.Log.d("ShopFragment", "Очищаем поиск - запрос пустой")
                    isSearchMode = false
                    isSearching = false // Принудительно сбрасываем флаг поиска
                    hideProgressBar() // Принудительно скрываем ProgressBar
                    hideAllPlaceholders()
                    loadData()
                }
            }
        })

        // Обработчик нажатия Enter
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = searchEditText.text?.toString()?.trim() ?: ""
                android.util.Log.d("ShopFragment", "Enter нажат: query='$query'")
                if (query.isNotEmpty()) {
                    addToSearchHistory(query)
                    hideKeyboard()
                    hideSearchHistory()
                }
                true
            } else {
                false
            }
        }

        // Обработчик кнопки очистки
        clearSearchButton.setOnClickListener {
            clearSearch()
        }
        
        // Обработчик кнопки обновления поиска
        refreshSearchButton.setOnClickListener {
            android.util.Log.d("ShopFragment", "Кнопка 'Обновить' нажата")
            retryLastSearch()
        }
        
        // Обработчик кнопки повторного поиска при ошибке
        retrySearchButton.setOnClickListener {
            android.util.Log.d("ShopFragment", "Кнопка 'Обновить' при ошибке нажата")
            retryLastSearch()
        }

        // Обработчик фокуса - показываем историю при фокусе
        searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                showKeyboard()
                if (searchHistory.isNotEmpty()) {
                    showSearchHistory()
                }
            } else {
                hideSearchHistory()
            }
        }
        
        // Обработчик клика по полю поиска
        searchEditText.setOnClickListener {
            if (searchHistory.isNotEmpty()) {
                showSearchHistory()
            }
        }
    }


    private fun observeData() {
        android.util.Log.d("ShopFragment", "=== НАСТРОЙКА НАБЛЮДЕНИЯ ЗА ДАННЫМИ ===")
        
        // Наблюдение за цветами
        lifecycleScope.launch {
            android.util.Log.d("ShopFragment", "Запускаем наблюдение за allFlowers")
            flowerViewModel.allFlowers.collect { flowers ->
                android.util.Log.d("ShopFragment", "=== ПОЛУЧЕНЫ ДАННЫЕ ===")
                android.util.Log.d("ShopFragment", "Количество цветов: ${flowers.size}")
                android.util.Log.d("ShopFragment", "isSearchMode: $isSearchMode, isSearching: $isSearching")
                android.util.Log.d("ShopFragment", "Детали цветов: ${flowers.map { it.name }}")
                
                shopAdapter.updateItems(flowers)
                
                if (isSearchMode && isSearching) {
                    android.util.Log.d("ShopFragment", "Режим поиска активен, результаты: ${flowers.size}")
                    android.util.Log.d("ShopFragment", "Детали результатов: ${flowers.map { it.name }}")
                    
                    // Сбрасываем флаг поиска сразу при получении результатов
                    isSearching = false
                    
                    // Добавляем минимальную задержку для ProgressBar (минимум 1 секунда)
                    val elapsedTime = System.currentTimeMillis() - searchStartTime
                    val remainingTime = maxOf(0L, 1000L - elapsedTime)
                    
                    android.util.Log.d("ShopFragment", "Время поиска: ${elapsedTime}мс, оставшееся время: ${remainingTime}мс")
                    
                    if (flowers.isEmpty()) {
                        android.util.Log.d("ShopFragment", "Результаты поиска пусты, показываем плейсхолдер")
                        showNoResultsPlaceholder()
                        
                        // Скрываем ProgressBar с задержкой
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            hideProgressBar()
                            android.util.Log.d("ShopFragment", "ProgressBar скрыт после пустых результатов")
                        }, remainingTime)
                    } else {
                        android.util.Log.d("ShopFragment", "Найдено ${flowers.size} результатов - показываем с задержкой")
                        flowers.forEach { flower ->
                            android.util.Log.d("ShopFragment", "Найден цветок: ${flower.name}")
                        }
                        
                        // Обновляем данные сразу
                        shopAdapter.updateItems(flowers)
                        shopAdapter.notifyDataSetChanged()
                        recyclerViewShop.visibility = View.VISIBLE
                        recyclerViewShop.requestLayout()
                        recyclerViewShop.invalidate()
                        emptyShopLayout.visibility = View.GONE
                        hideAllPlaceholders()
                        
                        // Скрываем ProgressBar с задержкой
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            hideProgressBar()
                            android.util.Log.d("ShopFragment", "ProgressBar скрыт после показа результатов")
                        }, remainingTime)
                        
                        android.util.Log.d("ShopFragment", "Результаты поиска отображены с задержкой")
                    }
                } else {
                    android.util.Log.d("ShopFragment", "Обычная загрузка, результаты: ${flowers.size}")
                    android.util.Log.d("ShopFragment", "isSearchMode: $isSearchMode, isSearching: $isSearching")
                    
                    // НЕ загружаем данные, если мы в режиме поиска
                    if (!isSearchMode) {
                        hideProgressBar() // Скрываем ProgressBar для обычной загрузки
                        android.util.Log.d("ShopFragment", "flowers.isEmpty() = ${flowers.isEmpty()}")
                        
                        // Если данные пустые, попробуем загрузить их снова через небольшую задержку
                        if (flowers.isEmpty()) {
                            android.util.Log.d("ShopFragment", "Данные пустые, пробуем загрузить снова через 2 секунды")
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                android.util.Log.d("ShopFragment", "Повторная загрузка данных")
                                loadData()
                            }, 2000)
                        }
                        
                        updateEmptyState(flowers.isEmpty())
                    } else {
                        android.util.Log.d("ShopFragment", "Пропускаем обычную загрузку - мы в режиме поиска")
                    }
                }
                
                // Дополнительная проверка: если мы в режиме поиска, но результаты не отображаются
                if (isSearchMode && !isSearching && flowers.isNotEmpty() && recyclerViewShop.visibility != View.VISIBLE) {
                    android.util.Log.d("ShopFragment", "ИСПРАВЛЕНИЕ: Принудительно показываем результаты поиска")
                    recyclerViewShop.visibility = View.VISIBLE
                    recyclerViewShop.requestLayout()
                    recyclerViewShop.invalidate()
                    hideAllPlaceholders()
                }
            }
        }


        // Наблюдение за загрузкой (временно отключено для отладки)
        lifecycleScope.launch {
            flowerViewModel.isLoading.collect { isLoading ->
                android.util.Log.d("ShopFragment", "isLoading: $isLoading")
                // Временно отключаем автоматическое управление ProgressBar
                // if (isLoading) {
                //     showProgressBar()
                // } else {
                //     hideProgressBar()
                // }
            }
        }

        // Наблюдение за ошибками
        lifecycleScope.launch {
            flowerViewModel.error.collect { error ->
                error?.let {
                    android.util.Log.d("ShopFragment", "Ошибка: $it")
                    
                    // Сбрасываем состояние поиска при ошибке
                    isSearching = false
                    hideProgressBar()
                    
                    if (isSearchMode) {
                        android.util.Log.d("ShopFragment", "Ошибка в режиме поиска, показываем плейсхолдер ошибки")
                        showSearchErrorPlaceholder()
                    } else {
                        android.util.Log.d("ShopFragment", "Ошибка в обычном режиме, показываем Toast")
                        Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    }
                    flowerViewModel.clearError()
                }
            }
        }

        // Наблюдение за избранными цветами
        lifecycleScope.launch {
            favoriteViewModel.favoriteFlowers.collect { flowers ->
                favoriteFlowerIds.clear()
                favoriteFlowerIds.addAll(flowers.map { it.id })
                android.util.Log.d("ShopFragment", "Обновлены избранные цветы: ${favoriteFlowerIds.size}")
                // Обновляем адаптер, чтобы показать правильные иконки избранного
                shopAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun loadData() {
        android.util.Log.d("ShopFragment", "=== ЗАГРУЗКА ДАННЫХ ===")
        android.util.Log.d("ShopFragment", "Загружаем все цветы")
        flowerViewModel.loadAllFlowers()
    }

    private fun updateClearButtonVisibility() {
        val hasText = searchEditText.text?.isNotEmpty() == true
        clearSearchButton.visibility = if (hasText) View.VISIBLE else View.GONE
    }

    private fun clearSearch() {
        searchEditText.text?.clear()
        hideKeyboard()
        hideSearchHistory()
        isSearchMode = false
        isSearching = false // Принудительно сбрасываем флаг поиска
        hideProgressBar() // Принудительно скрываем ProgressBar
        hideAllPlaceholders()
        loadData()
        android.util.Log.d("ShopFragment", "Поиск очищен, состояние сброшено")
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        android.util.Log.d("ShopFragment", "updateEmptyState: isEmpty = $isEmpty")
        emptyShopLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerViewShop.visibility = if (isEmpty) View.GONE else View.VISIBLE
        android.util.Log.d("ShopFragment", "emptyShopLayout.visibility = ${emptyShopLayout.visibility}")
        android.util.Log.d("ShopFragment", "recyclerViewShop.visibility = ${recyclerViewShop.visibility}")
    }

    private fun showKeyboard() {
        searchEditText.requestFocus()
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(searchEditText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_SEARCH_TEXT, searchEditText.text?.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Очищаем ресурсы
        _binding = null
    }
    
    // === Новые методы для плейсхолдеров и истории поиска ===
    
    private fun setupPlaceholderViews() {
        // Создаем плейсхолдеры программно, если их нет в layout
        // Находим FrameLayout контейнер для правильного позиционирования
        val frameLayout = binding.root.findViewById<ViewGroup>(R.id.frameLayout) 
        val parentLayout = frameLayout ?: binding.root as? ViewGroup
        
        // Плейсхолдер "Нет результатов"
        noResultsLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(32, 64, 32, 64)
            visibility = View.GONE
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            
            addView(TextView(requireContext()).apply {
                text = "Ничего не найдено"
                textSize = 18f
                gravity = android.view.Gravity.CENTER
                setTextColor(android.graphics.Color.GRAY)
            })
            
            addView(TextView(requireContext()).apply {
                text = "Попробуйте изменить поисковый запрос"
                textSize = 14f
                gravity = android.view.Gravity.CENTER
                setTextColor(android.graphics.Color.GRAY)
                setPadding(0, 16, 0, 0)
            })
        }
        
        // Плейсхолдер "Ошибка"
        errorLayout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(32, 64, 32, 64)
            visibility = View.GONE
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            
            addView(TextView(requireContext()).apply {
                text = "Ошибка поиска"
                textSize = 18f
                gravity = android.view.Gravity.CENTER
                setTextColor(android.graphics.Color.RED)
            })
            
            refreshButton = Button(requireContext()).apply {
                text = "Обновить"
                setOnClickListener {
                    retryLastSearch()
                }
            }
            addView(refreshButton)
        }
        
        parentLayout?.addView(noResultsLayout)
        parentLayout?.addView(errorLayout)
    }
    
    private fun setupSearchHistoryViews() {
        // Проверяем, есть ли элементы в layout
        if (::searchHistoryLayout.isInitialized && ::searchHistoryList.isInitialized && ::clearHistoryButton.isInitialized) {
            // Элементы уже есть в layout, настраиваем их
            searchHistoryLayout.visibility = View.GONE
            
            // Настраиваем обработчик для кнопки очистки истории
            clearHistoryButton.setOnClickListener {
                clearSearchHistory()
            }
            
            // Настраиваем обработчик для списка истории
            searchHistoryList.setOnItemClickListener { _, _, position, _ ->
                val selectedQuery = searchHistory[position]
                searchEditText.setText(selectedQuery)
                performSearch(selectedQuery)
                hideSearchHistory()
            }
        } else {
            // Создаем историю поиска программно (fallback)
            val parentLayout = binding.root as? ViewGroup
            
            searchHistoryLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                visibility = View.GONE
                setBackgroundColor(android.graphics.Color.WHITE)
                elevation = 8f
                
                // Заголовок
                val headerLayout = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    setPadding(16, 16, 16, 8)
                    
                    val title = TextView(requireContext()).apply {
                        text = "История поиска"
                        textSize = 16f
                        setTextColor(android.graphics.Color.BLACK)
                    }
                    
                    clearHistoryButton = Button(requireContext()).apply {
                        text = "Очистить"
                        textSize = 12f
                        setOnClickListener {
                            clearSearchHistory()
                        }
                    }
                    
                    addView(title)
                    addView(clearHistoryButton)
                }
                addView(headerLayout)
                
                // Список истории
                searchHistoryList = ListView(requireContext()).apply {
                    setPadding(16, 0, 16, 16)
                    setOnItemClickListener { _, _, position, _ ->
                        val selectedQuery = searchHistory[position]
                        searchEditText.setText(selectedQuery)
                        performSearch(selectedQuery)
                        hideSearchHistory()
                    }
                }
                addView(searchHistoryList)
            }
            
            parentLayout?.addView(searchHistoryLayout)
        }
        
        // Адаптер для истории поиска
        searchHistoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, searchHistory)
        searchHistoryList.adapter = searchHistoryAdapter
    }
    
    private fun loadSearchHistory() {
        val historyJson = sharedPreferences.getString(KEY_SEARCH_HISTORY, "[]")
        try {
            val jsonArray = JSONArray(historyJson)
            searchHistory.clear()
            for (i in 0 until jsonArray.length()) {
                searchHistory.add(jsonArray.getString(i))
            }
            // Ограничиваем размер при загрузке
            if (searchHistory.size > MAX_HISTORY_SIZE) {
                android.util.Log.d("ShopFragment", "Загруженная история поиска превышает лимит ${MAX_HISTORY_SIZE}, обрезаем до ${MAX_HISTORY_SIZE} элементов")
                searchHistory = searchHistory.take(MAX_HISTORY_SIZE).toMutableList()
                saveSearchHistory() // Сохраняем обрезанную историю
            }
        } catch (e: Exception) {
            searchHistory.clear()
        }
    }
    
    private fun saveSearchHistory() {
        val jsonArray = JSONArray()
        searchHistory.forEach { query ->
            jsonArray.put(query)
        }
        sharedPreferences.edit()
            .putString(KEY_SEARCH_HISTORY, jsonArray.toString())
            .apply()
    }
    
    private fun addToSearchHistory(query: String) {
        if (query.isBlank()) return
        
        android.util.Log.d("ShopFragment", "Добавляем в историю поиска: '$query' (текущий размер: ${searchHistory.size})")
        
        // Удаляем дубликаты
        searchHistory.remove(query)
        
        // Добавляем в начало
        searchHistory.add(0, query)
        
        // Ограничиваем размер
        if (searchHistory.size > MAX_HISTORY_SIZE) {
            android.util.Log.d("ShopFragment", "История поиска превышает лимит ${MAX_HISTORY_SIZE}, обрезаем до ${MAX_HISTORY_SIZE} элементов")
            searchHistory = searchHistory.take(MAX_HISTORY_SIZE).toMutableList()
        }
        
        // Сохраняем
        saveSearchHistory()
        searchHistoryAdapter.notifyDataSetChanged()
    }
    
    private fun clearSearchHistory() {
        searchHistory.clear()
        saveSearchHistory()
        searchHistoryAdapter.notifyDataSetChanged()
        hideSearchHistory()
    }
    
    private fun showSearchHistory() {
        if (searchHistory.isNotEmpty()) {
            searchHistoryLayout.visibility = View.VISIBLE
        }
    }
    
    private fun hideSearchHistory() {
        searchHistoryLayout.visibility = View.GONE
    }
    
    private fun performSearch(query: String) {
        if (query.isBlank()) {
            android.util.Log.d("ShopFragment", "Запрос поиска пустой, пропускаем")
            return
        }
        
        android.util.Log.d("ShopFragment", "=== НАЧАЛО ПОИСКА ===")
        android.util.Log.d("ShopFragment", "Запрос: '$query'")
        android.util.Log.d("ShopFragment", "Текущее состояние: isSearchMode=$isSearchMode, isSearching=$isSearching")
        
        // Сбрасываем состояние поиска перед новым поиском
        isSearching = false
        hideProgressBar()
        hideAllPlaceholders()
        
        isSearchMode = true // Включаем режим поиска
        isSearching = true
        lastSearchQuery = query
        searchStartTime = System.currentTimeMillis() // Засекаем время начала поиска
        
        android.util.Log.d("ShopFragment", "isSearchMode установлен в true, isSearching установлен в true")
        
        // Сначала показываем ProgressBar
        showProgressBar()
        // Скрываем результаты поиска и принудительно обновляем RecyclerView
        recyclerViewShop.visibility = View.GONE
        recyclerViewShop.requestLayout()
        emptyShopLayout.visibility = View.GONE
        android.util.Log.d("ShopFragment", "ProgressBar показан, запускаем поиск...")
        
        // Запускаем поиск
        android.util.Log.d("ShopFragment", "Запускаем поиск сразу для запроса: '$query'")
        android.util.Log.d("ShopFragment", "flowerViewModel: $flowerViewModel")
        
        try {
            android.util.Log.d("ShopFragment", "Вызываем flowerViewModel.searchFlowers('$query')")
            android.util.Log.d("ShopFragment", "ViewModel состояние: ${flowerViewModel}")
            
            // Проверяем, что ViewModel инициализирован
            if (flowerViewModel == null) {
                android.util.Log.e("ShopFragment", "ОШИБКА: flowerViewModel равен null!")
                return
            }
            
            flowerViewModel.searchFlowers(query)
            android.util.Log.d("ShopFragment", "Метод searchFlowers() вызван успешно")
            
            // Дополнительная проверка через небольшую задержку
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                android.util.Log.d("ShopFragment", "Проверка через 1 секунду: isSearching=$isSearching")
                if (isSearching) {
                    android.util.Log.d("ShopFragment", "Поиск все еще активен через 1 секунду - возможно проблема с ViewModel")
                    android.util.Log.d("ShopFragment", "Попробуем принудительно обновить данные")
                    // Принудительно обновляем данные
                    flowerViewModel.loadAllFlowers()
                }
            }, 1000)
            
        } catch (e: Exception) {
            android.util.Log.e("ShopFragment", "Ошибка при вызове searchFlowers(): ${e.message}", e)
        }
        
        // НЕМЕДЛЕННО обновляем UI после запуска поиска
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            android.util.Log.d("ShopFragment", "НЕМЕДЛЕННОЕ обновление UI после запуска поиска")
            recyclerViewShop.requestLayout()
            recyclerViewShop.invalidate()
            shopAdapter.notifyDataSetChanged()
        }
        
        // Добавляем таймаут как запасной вариант (3 секунды)
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (isSearching) {
                android.util.Log.d("ShopFragment", "ТАЙМАУТ: Принудительно скрываем ProgressBar")
                android.util.Log.d("ShopFragment", "ТАЙМАУТ: Показываем плейсхолдер 'Не найдено'")
                isSearching = false
                hideProgressBar()
                showNoResultsPlaceholder()
            }
        }, 3000)
        
        android.util.Log.d("ShopFragment", "=== КОНЕЦ НАСТРОЙКИ ПОИСКА ===")
        android.util.Log.d("ShopFragment", "Финальное состояние: isSearchMode=$isSearchMode, isSearching=$isSearching")
    }
    
    private fun retryLastSearch() {
        if (lastSearchQuery.isNotEmpty()) {
            android.util.Log.d("ShopFragment", "Повторный поиск для запроса: '$lastSearchQuery'")
            android.util.Log.d("ShopFragment", "Текущее состояние: isSearchMode=$isSearchMode, isSearching=$isSearching")
            
            // НЕ сбрасываем состояние при повторном поиске
            // Просто запускаем новый поиск
            isSearchMode = true
            isSearching = true
            searchStartTime = System.currentTimeMillis()
            
            // Показываем ProgressBar
            showProgressBar()
            recyclerViewShop.visibility = View.GONE
            emptyShopLayout.visibility = View.GONE
            
            // Запускаем поиск
            try {
                android.util.Log.d("ShopFragment", "Вызываем flowerViewModel.searchFlowers('$lastSearchQuery') для повторного поиска")
                flowerViewModel.searchFlowers(lastSearchQuery)
                android.util.Log.d("ShopFragment", "Повторный поиск запущен успешно")
            } catch (e: Exception) {
                android.util.Log.e("ShopFragment", "Ошибка при повторном поиске: ${e.message}", e)
            }
        } else {
            android.util.Log.d("ShopFragment", "Нет запроса для повторного поиска")
        }
    }
    
    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
        recyclerViewShop.visibility = View.GONE
        emptyShopLayout.visibility = View.GONE
        // Принудительно обновляем UI
        progressBar.requestLayout()
        progressBar.invalidate()
        android.util.Log.d("ShopFragment", "Показываем ProgressBar")
    }
    
    private fun hideProgressBar() {
        if (progressBar.visibility != View.GONE) {
            progressBar.visibility = View.GONE
            android.util.Log.d("ShopFragment", "Скрываем ProgressBar (был видимым)")
        } else {
            android.util.Log.d("ShopFragment", "ProgressBar уже скрыт")
        }
    }
    
    private fun hideAllPlaceholders() {
        android.util.Log.d("ShopFragment", "Скрываем все плейсхолдеры")
        noResultsLayout.visibility = View.GONE
        errorLayout.visibility = View.GONE
        emptyShopLayout.visibility = View.GONE
        searchErrorLayout.visibility = View.GONE
        recyclerViewShop.visibility = View.VISIBLE
        // Принудительно обновляем RecyclerView
        recyclerViewShop.requestLayout()
        recyclerViewShop.invalidate()
        // Скрываем ProgressBar только если не в режиме поиска
        if (!isSearching) {
            hideProgressBar()
        }
        android.util.Log.d("ShopFragment", "Плейсхолдеры скрыты, RecyclerView обновлен")
    }
    
    private fun showNoResultsPlaceholder() {
        android.util.Log.d("ShopFragment", "Показываем плейсхолдер 'Ничего не найдено'")
        // Скрываем только результаты поиска, НЕ ProgressBar
        recyclerViewShop.visibility = View.GONE
        emptyShopLayout.visibility = View.VISIBLE
        // ProgressBar будет скрыт отдельно через динамическую задержку
        android.util.Log.d("ShopFragment", "Плейсхолдер 'Ничего не найдено' отображен")
    }
    
    private fun showErrorPlaceholder() {
        hideAllPlaceholders()
        errorLayout.visibility = View.VISIBLE
    }
    
    private fun showSearchErrorPlaceholder() {
        android.util.Log.d("ShopFragment", "Показываем плейсхолдер 'Ошибка поиска'")
        // Скрываем только результаты поиска, НЕ ProgressBar
        recyclerViewShop.visibility = View.GONE
        searchErrorLayout.visibility = View.VISIBLE
        // ProgressBar будет скрыт отдельно через динамическую задержку
        android.util.Log.d("ShopFragment", "Плейсхолдер 'Ошибка поиска' отображен")
    }
    
    private fun forceUpdateUI() {
        android.util.Log.d("ShopFragment", "Принудительное обновление UI")
        android.util.Log.d("ShopFragment", "Состояние: isSearchMode=$isSearchMode, isSearching=$isSearching, itemCount=${shopAdapter.itemCount}")
        
        // Принудительно обновляем все
        recyclerViewShop.requestLayout()
        recyclerViewShop.invalidate()
        shopAdapter.notifyDataSetChanged()
        
        // Дополнительная проверка видимости
        if (isSearchMode && shopAdapter.itemCount > 0) {
            recyclerViewShop.visibility = View.VISIBLE
            emptyShopLayout.visibility = View.GONE
            hideAllPlaceholders()
            android.util.Log.d("ShopFragment", "UI обновлен: показано ${shopAdapter.itemCount} элементов")
        } else if (isSearchMode && shopAdapter.itemCount == 0) {
            android.util.Log.d("ShopFragment", "Режим поиска, но нет результатов - показываем плейсхолдер")
            showNoResultsPlaceholder()
        } else if (!isSearchMode) {
            android.util.Log.d("ShopFragment", "НЕ в режиме поиска - показываем обычные данные")
            recyclerViewShop.visibility = View.VISIBLE
            emptyShopLayout.visibility = View.GONE
            hideAllPlaceholders()
        }
        
        // Дополнительное принудительное обновление
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            recyclerViewShop.requestLayout()
            recyclerViewShop.invalidate()
            shopAdapter.notifyDataSetChanged()
            android.util.Log.d("ShopFragment", "Дополнительное принудительное обновление выполнено")
        }
    }
    
    private fun isFlowerFavorite(flowerId: String): Boolean {
        val isFavorite = favoriteFlowerIds.contains(flowerId)
        android.util.Log.d("ShopFragment", "isFlowerFavorite($flowerId) = $isFavorite")
        android.util.Log.d("ShopFragment", "favoriteFlowerIds: $favoriteFlowerIds")
        return isFavorite
    }
}