package com.example.setaside.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.setaside.data.model.*
import com.example.setaside.data.repository.OrderRepository
import com.example.setaside.data.repository.Result
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val isCheckingOut: Boolean = false,
    val checkoutSuccess: Boolean = false,
    val lastOrderId: String? = null,
    val error: String? = null
) {
    val totalItems: Int
        get() = items.sumOf { it.quantity }
    
    val totalPrice: Double
        get() = items.sumOf { it.totalPrice }
    
    val isEmpty: Boolean
        get() = items.isEmpty()
}

class CartViewModel(private val orderRepository: OrderRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()
    
    fun addToCart(product: Product, quantity: Int = 1, specialInstructions: String? = null) {
        _uiState.update { state ->
            val existingItem = state.items.find { it.product.id == product.id }
            if (existingItem != null) {
                val updatedItems = state.items.map {
                    if (it.product.id == product.id) {
                        it.copy(quantity = it.quantity + quantity)
                    } else {
                        it
                    }
                }
                state.copy(items = updatedItems)
            } else {
                state.copy(items = state.items + CartItem(product, quantity, specialInstructions))
            }
        }
    }
    
    fun removeFromCart(productId: String) {
        _uiState.update { state ->
            state.copy(items = state.items.filter { it.product.id != productId })
        }
    }
    
    fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(productId)
            return
        }
        
        _uiState.update { state ->
            val updatedItems = state.items.map {
                if (it.product.id == productId) {
                    it.copy(quantity = quantity)
                } else {
                    it
                }
            }
            state.copy(items = updatedItems)
        }
    }
    
    fun updateSpecialInstructions(productId: String, instructions: String?) {
        _uiState.update { state ->
            val updatedItems = state.items.map {
                if (it.product.id == productId) {
                    it.copy(specialInstructions = instructions)
                } else {
                    it
                }
            }
            state.copy(items = updatedItems)
        }
    }
    
    fun clearCart() {
        _uiState.update { CartUiState() }
    }
    
    fun checkout(notes: String? = null, pickupTime: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingOut = true, error = null) }
            
            val orderItems = _uiState.value.items.map { cartItem ->
                CreateOrderItemRequest(
                    productId = cartItem.product.id,
                    quantity = cartItem.quantity,
                    specialInstructions = cartItem.specialInstructions
                )
            }
            
            val request = CreateOrderRequest(
                notes = notes,
                pickupTime = pickupTime,
                items = orderItems
            )
            
            when (val result = orderRepository.createOrder(request)) {
                is Result.Success -> {
                    _uiState.update {
                        CartUiState(
                            checkoutSuccess = true,
                            lastOrderId = result.data.id
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isCheckingOut = false, error = result.message)
                    }
                }
                is Result.Loading -> {}
            }
        }
    }
    
    fun resetCheckoutState() {
        _uiState.update { it.copy(checkoutSuccess = false, lastOrderId = null) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    class Factory(private val orderRepository: OrderRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CartViewModel(orderRepository) as T
        }
    }
}
