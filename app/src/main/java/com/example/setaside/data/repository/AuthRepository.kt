package com.example.setaside.data.repository

import com.example.setaside.data.local.TokenManager
import com.example.setaside.data.model.*
import com.example.setaside.data.remote.ApiService
import kotlinx.coroutines.flow.first

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: Int = -1) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val response = apiService.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val authResponse = response.body()!!
                // Get user info
                val userResponse = apiService.getCurrentUser()
                if (userResponse.isSuccessful) {
                    val user = userResponse.body()!!
                    tokenManager.saveAuthData(
                        token = authResponse.accessToken,
                        userId = user.id,
                        email = user.email,
                        name = user.fullName,
                        role = user.role
                    )
                    Result.Success(user)
                } else {
                    Result.Error("Failed to get user info", userResponse.code())
                }
            } else {
                Result.Error("Login failed: ${response.message()}", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun register(email: String, password: String, fullName: String, phone: String?): Result<User> {
        return try {
            val response = apiService.register(RegisterRequest(email, password, fullName, phone))
            if (response.isSuccessful) {
                val authResponse = response.body()!!
                // Get user info
                val userResponse = apiService.getCurrentUser()
                if (userResponse.isSuccessful) {
                    val user = userResponse.body()!!
                    tokenManager.saveAuthData(
                        token = authResponse.accessToken,
                        userId = user.id,
                        email = user.email,
                        name = user.fullName,
                        role = user.role
                    )
                    Result.Success(user)
                } else {
                    Result.Error("Failed to get user info", userResponse.code())
                }
            } else {
                Result.Error("Registration failed: ${response.message()}", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun logout() {
        tokenManager.clearAuthData()
    }
    
    suspend fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn.first()
    }
    
    suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Failed to get user", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
    
    fun getTokenManager() = tokenManager
}
