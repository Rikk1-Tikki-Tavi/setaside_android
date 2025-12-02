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
    val userEmail: String = "",
    val userPhone: String = "",
    val userRole: String = "customer",
    val error: String? = null,
    val isAdmin: Boolean = false,
    val isUpdatingProfile: Boolean = false,
    val profileUpdateSuccess: Boolean = false
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
                    val userEmail = authRepository.getTokenManager().userEmail.first() ?: ""
                    val userPhone = authRepository.getTokenManager().userPhone.first() ?: ""
                    val userRole = authRepository.getTokenManager().userRole.first() ?: "customer"
                    _uiState.update {
                        it.copy(
                            isLoggedIn = true,
                            userName = userName,
                            userEmail = userEmail,
                            userPhone = userPhone,
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
                            userEmail = "",
                            userPhone = "",
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
                            userEmail = result.data.email,
                            userPhone = result.data.phone ?: "",
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
                            userEmail = result.data.email,
                            userPhone = result.data.phone ?: "",
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
    
    fun updateProfile(fullName: String?, phone: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingProfile = true, error = null, profileUpdateSuccess = false) }
            
            when (val result = authRepository.updateProfile(fullName, phone)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isUpdatingProfile = false,
                            userName = fullName ?: it.userName,
                            userPhone = phone ?: it.userPhone,
                            profileUpdateSuccess = true
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isUpdatingProfile = false, error = result.message)
                    }
                }
                is Result.Loading -> {}
            }
        }
    }
    
    fun clearProfileUpdateSuccess() {
        _uiState.update { it.copy(profileUpdateSuccess = false) }
    }
    
    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AuthViewModel(authRepository) as T
        }
    }
}
