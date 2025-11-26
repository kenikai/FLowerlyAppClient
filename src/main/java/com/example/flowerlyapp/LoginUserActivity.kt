package com.example.flowerlyapp

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.flowerlyapp.presentation.viewmodel.AuthViewModel
import com.example.flowerlyapp.presentation.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.launch

class LoginUserActivity : AppCompatActivity() {

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(this)
    }
    
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var buttonContinue: Button
    private lateinit var textViewGoSignUp: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Применяем тему перед setContentView
        com.example.flowerlyapp.data.network.ThemeManager.applyTheme()
        
        setContentView(R.layout.activity_login_user)


        initViews()
        setupClickListeners()
        observeAuthState()
    }
    
    private fun initViews() {
        emailField = findViewById(R.id.email_address)
        passwordField = findViewById(R.id.password_title)
        buttonContinue = findViewById(R.id.continue2)
        textViewGoSignUp = findViewById(R.id.go_sign)
        
        // Настройка inputType для правильной клавиатуры
        emailField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        emailField.imeOptions = EditorInfo.IME_ACTION_NEXT
        
        passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        passwordField.imeOptions = EditorInfo.IME_ACTION_DONE
    }
    
    private fun setupClickListeners() {
        textViewGoSignUp.setOnClickListener {
            val intent = Intent(this, SignUpUserActivity::class.java)
            startActivity(intent)
        }
        
        buttonContinue.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()

            if (validateInput(email, password)) {
                authViewModel.login(email, password)
            }
        }
    }
    
    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true

        if (email.isEmpty()) {
            emailField.error = "Введите email"
            isValid = false
        }

        if (password.isEmpty()) {
            passwordField.error = "Введите пароль"
            isValid = false
        }

        return isValid
    }
    
    private fun observeAuthState() {
        lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                when (state) {
                    is com.example.flowerlyapp.presentation.viewmodel.AuthState.Loading -> {
                        buttonContinue.isEnabled = false
                        buttonContinue.text = "Вход..."
                    }
                    is com.example.flowerlyapp.presentation.viewmodel.AuthState.Success -> {
                        buttonContinue.isEnabled = true
                        buttonContinue.text = "Продолжить"
                        
                        Toast.makeText(this@LoginUserActivity, "Успешный вход!", Toast.LENGTH_SHORT).show()
                        
                        val intent = Intent(this@LoginUserActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    is com.example.flowerlyapp.presentation.viewmodel.AuthState.Error -> {
                        buttonContinue.isEnabled = true
                        buttonContinue.text = "Продолжить"
                        
                        Toast.makeText(this@LoginUserActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        buttonContinue.isEnabled = true
                        buttonContinue.text = "Продолжить"
                    }
                }
            }
        }
    }
}
