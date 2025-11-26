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

class SignUpUserActivity : AppCompatActivity() {
    
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(this)
    }
    
    private lateinit var firstname: EditText
    private lateinit var lastname: EditText
    private lateinit var emailField: EditText
    private lateinit var passwordField: EditText
    private lateinit var confirmPasswordField: EditText
    private lateinit var buttonSignUp: Button
    private lateinit var textViewGoLogIn: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Применяем тему перед setContentView
        com.example.flowerlyapp.data.network.ThemeManager.applyTheme()
        
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up_user)
        
        
        initViews()
        setupClickListeners()
        observeAuthState()
    }
    
    private fun initViews() {
        firstname = findViewById(R.id.first_name)
        lastname = findViewById(R.id.last_name)
        emailField = findViewById(R.id.email_address1)
        passwordField = findViewById(R.id.password_title1)
        confirmPasswordField = findViewById(R.id.confirm_password)
        buttonSignUp = findViewById(R.id.sign_up_button)
        textViewGoLogIn = findViewById(R.id.go_log_in)
        
        // Настройка inputType для правильной клавиатуры
        firstname.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        firstname.imeOptions = EditorInfo.IME_ACTION_NEXT
        
        lastname.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
        lastname.imeOptions = EditorInfo.IME_ACTION_NEXT
        
        emailField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        emailField.imeOptions = EditorInfo.IME_ACTION_NEXT
        
        passwordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        passwordField.imeOptions = EditorInfo.IME_ACTION_NEXT
        
        confirmPasswordField.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        confirmPasswordField.imeOptions = EditorInfo.IME_ACTION_DONE
    }
    
    private fun setupClickListeners() {
        textViewGoLogIn.setOnClickListener {
            val intent = Intent(this, LoginUserActivity::class.java)
            startActivity(intent)
        }
        
        buttonSignUp.setOnClickListener {
            val firstName = firstname.text.toString().trim()
            val lastName = lastname.text.toString().trim()
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val confirmPassword = confirmPasswordField.text.toString().trim()

            if (validateInput(firstName, lastName, email, password, confirmPassword)) {
                authViewModel.register(firstName, lastName, email, password, confirmPassword)
            }
        }
    }
    
    private fun validateInput(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true

        if (firstName.isEmpty()) {
            firstname.error = "Введите имя"
            isValid = false
        }

        if (lastName.isEmpty()) {
            lastname.error = "Введите фамилию"
            isValid = false
        }

        if (email.isEmpty()) {
            emailField.error = "Введите email"
            isValid = false
        }

        if (password.isEmpty()) {
            passwordField.error = "Введите пароль"
            isValid = false
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordField.error = "Подтвердите пароль"
            isValid = false
        }

        return isValid
    }
    
    private fun observeAuthState() {
        lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                when (state) {
                    is com.example.flowerlyapp.presentation.viewmodel.AuthState.Loading -> {
                        buttonSignUp.isEnabled = false
                        buttonSignUp.text = "Регистрация..."
                    }
                    is com.example.flowerlyapp.presentation.viewmodel.AuthState.Success -> {
                        buttonSignUp.isEnabled = true
                        buttonSignUp.text = "Зарегистрироваться"
                        
                        Toast.makeText(this@SignUpUserActivity, "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                        
                        val intent = Intent(this@SignUpUserActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    is com.example.flowerlyapp.presentation.viewmodel.AuthState.Error -> {
                        buttonSignUp.isEnabled = true
                        buttonSignUp.text = "Зарегистрироваться"
                        
                        Toast.makeText(this@SignUpUserActivity, state.message, Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        buttonSignUp.isEnabled = true
                        buttonSignUp.text = "Зарегистрироваться"
                    }
                }
            }
        }
    }
}
