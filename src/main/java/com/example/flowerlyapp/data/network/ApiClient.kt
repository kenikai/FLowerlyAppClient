package com.example.flowerlyapp.data.network

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object ApiClient {
    
    private const val BASE_URL = "http://10.0.2.2:8080" // Для эмулятора Android
    // Если используете реальное устройство, замените на IP вашего компьютера:
    // private const val BASE_URL = "http://192.168.1.XXX:8080" // Замените XXX на ваш IP
    
    val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        
        install(Logging) {
            level = LogLevel.INFO
        }
        
        install(Auth) {
            bearer {
                loadTokens {
                    // Загружаем токен из SharedPreferences или другого хранилища
                    val token = TokenManager.getAccessToken()
                    if (token != null) {
                        BearerTokens(token, token)
                    } else {
                        null
                    }
                }
            }
        }
        
        install(DefaultRequest) {
            url(BASE_URL)
            contentType(ContentType.Application.Json)
        }
        
        install(HttpTimeout) {
            requestTimeoutMillis = 10000
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 10000
        }
    }
}
