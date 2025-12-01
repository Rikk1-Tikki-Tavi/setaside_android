package com.example.setaside.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.setaside.data.model.User
import com.example.setaside.data.repository.AuthRepository
import com.example.setaside.data.repository.Result
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val userName: String = "",
    val userRole: String = "customer",
    val error: String? = null,
    val isAdmin: Boolean = false
)

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    init {
        checkLoginStatus()
    }
    
    private fun checkLoginStatus() {
        viewModelScope.launch {
            authRepository.getTokenManager().isLoggedIn.collect { isLoggedIn ->
                if (isLoggedIn) {
                    val userName = authRepository.getTokenManager().userName.first() ?: ""
                    val userRole = authRepository.getTokenManager().userRole.first() ?: "customer"
                    _uiState.update {
                        it.copy(
                            isLoggedIn = true,
                            userName = userName,
                            userRole = userRole,
                            isAdmin = userRole == "admin" || userRole == "cashier"
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoggedIn = false,
                            user = null,
                            userName = "",
                            userRole = "customer",
                            isAdmin = false
                        )
                    }
                }
            }
        }
    }
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = authRepository.login(email, password)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            user = result.data,
                            userName = result.data.fullName,
                            userRole = result.data.role,
                            isAdmin = result.data.role == "admin" || result.data.role == "cashier",
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                is Result.Loading -> {}
            }
        }
    }
    
    fun register(email: String, password: String, fullName: String, phone: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = authRepository.register(email, password, fullName, phone)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            user = result.data,
                            userName = result.data.fullName,
                            userRole = result.data.role,
                            isAdmin = result.data.role == "admin" || result.data.role == "cashier",
                            error = null
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                is Result.Loading -> {}
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update {
                AuthUiState()
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepository) as T
        }
    }
}
