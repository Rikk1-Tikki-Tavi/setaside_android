package com.example.setaside.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.setaside.data.model.CreateProductRequest
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
    val error: String? = null,
    val isCreating: Boolean = false,
    val isUpdating: Boolean = false,
    val isDeleting: Boolean = false
)

class ProductViewModel(private val productRepository: ProductRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()
    
    init {
        loadProducts()
        loadAllCategoriesFromAll()
    }
    
    fun loadProducts(includeUnavailable: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val category = _uiState.value.selectedCategory
            val search = _uiState.value.searchQuery.takeIf { it.isNotBlank() }
            
            // For admin, pass null to get all products; for customers, pass true to get only available
            val isAvailable = if (includeUnavailable) null else true
            
            when (val result = productRepository.getProducts(
                category = category,
                search = search,
                isAvailable = isAvailable
            )) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            products = result.data.products
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
    
    fun loadAllProducts() {
        loadProducts(includeUnavailable = true)
    }
    
    fun loadAllCategoriesFromAll() {
        viewModelScope.launch {
            // Load all products without any filters to extract all available categories
            when (val result = productRepository.getProducts(category = null, search = null, isAvailable = true)) {
                is Result.Success -> {
                    val allCategories = result.data.products
                        .map { p -> p.category.trim() }
                        .filter { it.isNotEmpty() }
                        .distinct()
                        .sorted()
                    
                    _uiState.update { it.copy(categories = allCategories) }
                }
                is Result.Error -> {}
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
    
    fun createProduct(
        name: String,
        description: String?,
        price: Double,
        category: String,
        isAvailable: Boolean,
        stockQuantity: Int?,
        imageUrl: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, error = null) }
            
            val request = CreateProductRequest(
                name = name,
                description = description,
                price = price,
                category = category,
                isAvailable = isAvailable,
                stockQuantity = stockQuantity,
                imageUrl = imageUrl
            )
            
            when (val result = productRepository.createProduct(request)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isCreating = false) }
                    loadProducts() // Refresh list
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isCreating = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }
    
    fun updateProduct(
        id: String,
        name: String?,
        description: String?,
        price: Double?,
        category: String?,
        isAvailable: Boolean?,
        stockQuantity: Int?,
        imageUrl: String?
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, error = null) }
            
            val updates = mutableMapOf<String, Any>()
            name?.let { updates["name"] = it }
            description?.let { updates["description"] = it }
            price?.let { updates["price"] = it }
            category?.let { updates["category"] = it }
            isAvailable?.let { updates["is_available"] = it }
            stockQuantity?.let { updates["stock_quantity"] = it }
            imageUrl?.let { updates["image_url"] = it }
            
            when (val result = productRepository.updateProduct(id, updates)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isUpdating = false) }
                    loadProducts() // Refresh list
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isUpdating = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }
    
    fun deleteProduct(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }
            
            when (val result = productRepository.deleteProduct(id)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isDeleting = false) }
                    loadProducts() // Refresh list
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isDeleting = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }
    
    class Factory(private val productRepository: ProductRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProductViewModel(productRepository) as T
        }
    }
}
