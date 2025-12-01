package com.example.setaside.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.setaside.data.model.Product
import com.example.setaside.data.repository.ProductRepository
import com.example.setaside.data.repository.Result
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ProductsUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val selectedProduct: Product? = null,
    val error: String? = null
)

class ProductViewModel(private val productRepository: ProductRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()
    
    init {
        loadProducts()
        loadCategories()
    }
    
    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val category = _uiState.value.selectedCategory
            val search = _uiState.value.searchQuery.takeIf { it.isNotBlank() }
            
            when (val result = productRepository.getProducts(
                category = category,
                search = search
            )) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, products = result.data.products)
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
    
    fun loadCategories() {
        viewModelScope.launch {
            when (val result = productRepository.getCategories()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(categories = result.data)
                    }
                }
                is Result.Error -> {}
                is Result.Loading -> {}
            }
        }
    }
    
    fun setSelectedCategory(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
        loadProducts()
    }
    
    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        loadProducts()
    }
    
    fun selectProduct(product: Product?) {
        _uiState.update { it.copy(selectedProduct = product) }
    }
    
    fun loadProductDetails(productId: String) {
        viewModelScope.launch {
            when (val result = productRepository.getProduct(productId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(selectedProduct = result.data) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    class Factory(private val productRepository: ProductRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProductViewModel(productRepository) as T
        }
    }
}
