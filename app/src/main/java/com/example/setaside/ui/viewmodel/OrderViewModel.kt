package com.example.setaside.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.setaside.data.model.Order
import com.example.setaside.data.model.OrderStatus
import com.example.setaside.data.repository.OrderRepository
import com.example.setaside.data.repository.Result
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class OrdersUiState(
    val isLoading: Boolean = false,
    val orders: List<Order> = emptyList(),
    val selectedOrder: Order? = null,
    val filterStatus: OrderStatus? = null,
    val error: String? = null,
    val isUpdatingStatus: Boolean = false,
    val statusUpdateSuccess: Boolean = false,
    val showCompletionModal: Boolean = false
)

class OrderViewModel(private val orderRepository: OrderRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()
    
    fun loadOrders(status: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = orderRepository.getOrders(status = status)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, orders = result.data.orders)
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
    
    fun loadOrderDetails(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = orderRepository.getOrder(orderId)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, selectedOrder = result.data)
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
    
    fun setFilterStatus(status: OrderStatus?) {
        _uiState.update { it.copy(filterStatus = status) }
        loadOrders(status?.toApiString())
    }
    
    fun selectOrder(order: Order?) {
        _uiState.update { it.copy(selectedOrder = order) }
    }
    
    fun updateOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdatingStatus = true, error = null) }
            
            when (val result = orderRepository.updateOrderStatus(orderId, newStatus)) {
                is Result.Success -> {
                    _uiState.update { state ->
                        val updatedOrders = state.orders.map { order ->
                            if (order.id == orderId) result.data else order
                        }.filter { order ->
                            // Filter out orders that no longer match the current filter
                            state.filterStatus == null || order.status == state.filterStatus
                        }
                        state.copy(
                            isUpdatingStatus = false,
                            orders = updatedOrders,
                            selectedOrder = if (state.selectedOrder?.id == orderId) result.data else state.selectedOrder,
                            statusUpdateSuccess = true,
                            // Show completion modal when order is marked as picked up
                            showCompletionModal = newStatus == "pickedup"
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isUpdatingStatus = false, error = result.message)
                    }
                }
                is Result.Loading -> {}
            }
        }
    }
    
    fun deleteOrder(orderId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = orderRepository.deleteOrder(orderId)) {
                is Result.Success -> {
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            orders = state.orders.filter { it.id != orderId },
                            selectedOrder = if (state.selectedOrder?.id == orderId) null else state.selectedOrder
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
    
    fun resetStatusUpdateSuccess() {
        _uiState.update { it.copy(statusUpdateSuccess = false) }
    }
    
    fun dismissCompletionModal() {
        _uiState.update { it.copy(showCompletionModal = false) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    class Factory(private val orderRepository: OrderRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OrderViewModel(orderRepository) as T
        }
    }
}
