package com.example.flowerlyapp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.flowerlyapp.presentation.viewmodel.AuthViewModel
import com.example.flowerlyapp.presentation.viewmodel.AuthViewModelFactory

class AccountFragment : Fragment() {

    private lateinit var userNameText: TextView
    private lateinit var userEmailText: TextView
    private lateinit var logoutButton: Button
    private lateinit var ordersLayout: View
    private lateinit var themeLayout: View
    private lateinit var themeSwitch: Switch
    private lateinit var aboutLayout: View

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupClickListeners()
        loadUserData()
    }

    private fun initViews(view: View) {
        userNameText = view.findViewById(R.id.userNameText)
        userEmailText = view.findViewById(R.id.userEmailText)
        logoutButton = view.findViewById(R.id.logoutButton)
        ordersLayout = view.findViewById(R.id.ordersLayout)
        themeLayout = view.findViewById(R.id.themeLayout)
        themeSwitch = view.findViewById(R.id.themeSwitch)
        aboutLayout = view.findViewById(R.id.aboutLayout)
    }

    private fun setupClickListeners() {
        logoutButton.setOnClickListener {
            logout()
        }

        ordersLayout.setOnClickListener {
            // Переход к экрану заказов
            val intent = Intent(requireContext(), OrdersActivity::class.java)
            startActivity(intent)
        }

        // Загружаем текущую тему
        themeSwitch.isChecked = com.example.flowerlyapp.data.network.ThemeManager.isDarkTheme()
        
        // Переключатель темы
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            com.example.flowerlyapp.data.network.ThemeManager.setDarkTheme(isChecked)
            if (isChecked) {
                Toast.makeText(requireContext(), "Темная тема включена", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Светлая тема включена", Toast.LENGTH_SHORT).show()
            }
        }

        aboutLayout.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun loadUserData() {
        val userId = authViewModel.getCurrentUserId()
        val userEmail = authViewModel.getCurrentUserEmail()

        if (userId != null && userEmail != null) {
            // Показываем данные пользователя
            userEmailText.text = userEmail
            
            // Получаем имя и фамилию из TokenManager
            val firstName = com.example.flowerlyapp.data.network.TokenManager.getUserFirstName()
            val lastName = com.example.flowerlyapp.data.network.TokenManager.getUserLastName()
            
            // Формируем полное имя
            val fullName = when {
                !firstName.isNullOrEmpty() && !lastName.isNullOrEmpty() -> "$firstName $lastName"
                !firstName.isNullOrEmpty() -> firstName
                !lastName.isNullOrEmpty() -> lastName
                else -> "Пользователь"
            }
            
            userNameText.text = fullName
        } else {
            // Если данные не найдены, переходим на экран входа
            navigateToLogin()
        }
    }

    private fun logout() {
        lifecycleScope.launch {
            authViewModel.logout()
            Toast.makeText(requireContext(), "Вы вышли из аккаунта", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(requireContext(), LoginUserActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun showAboutDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_about, null)
        
        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Закрыть") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .apply {
                // Применяем тему к диалогу
                show()
            }
    }

    companion object {
        @JvmStatic
        fun newInstance() = AccountFragment()
    }
}