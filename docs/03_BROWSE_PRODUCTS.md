# Scenario: Browse Products

**Path:** `View (HomeScreen)` → `ViewModel (ProductViewModel)` → `Repository (ProductRepository)` → `API (Retrofit)`

## 📊 Flow Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                         BROWSE PRODUCTS FLOW                                  │
└──────────────────────────────────────────────────────────────────────────────┘

                              APP LAUNCH
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          ProductViewModel                                    │
│                                                                              │
│   init {                                                                     │
│       loadProducts()      ◄─────── Automatic on ViewModel creation          │
│       loadCategories()                                                       │
│   }                                                                          │
└─────────────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                          ProductRepository                                   │
│                                                                              │
│   GET /products?is_available=true                                           │
│   GET /products (to extract categories)                                     │
└─────────────────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            HomeScreen                                        │
│                                                                              │
│   ┌─────────────────────────────────────────────────────────────────────┐  │
│   │  Header: "Hello, [userName]"              [Cart Icon with Badge]    │  │
│   ├─────────────────────────────────────────────────────────────────────┤  │
│   │  [🔍 Search products...]                                            │  │
│   ├─────────────────────────────────────────────────────────────────────┤  │
│   │  Categories: [All] [Vegetables] [Fruits] [Dairy] ...               │  │
│   ├─────────────────────────────────────────────────────────────────────┤  │
│   │  ┌─────────┐  ┌─────────┐  ┌─────────┐  ┌─────────┐               │  │
│   │  │ Product │  │ Product │  │ Product │  │ Product │               │  │
│   │  │  Card   │  │  Card   │  │  Card   │  │  Card   │               │  │
│   │  └─────────┘  └─────────┘  └─────────┘  └─────────┘               │  │
│   ├─────────────────────────────────────────────────────────────────────┤  │
│   │  [🏠 Home]        [📋 Orders]        [👤 Profile]                  │  │
│   └─────────────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 📝 User Actions

| Action | Result |
|--------|--------|
| Opens app | Products load automatically |
| Taps category chip | Products filtered by category |
| Types in search | Products filtered by search query |
| Taps product card | Product detail dialog opens |
| Pull down to refresh | Products reloaded |

---

## 🔄 Code Flow

### Step 1: ViewModel Initialization

**File**: `ui/viewmodel/ProductViewModel.kt`

```kotlin
class ProductViewModel(private val productRepository: ProductRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()
    
    init {
        loadProducts()              // Load products on creation
        loadAllCategoriesFromAll()  // Load categories
    }
```

**ProductsUiState:**
```kotlin
data class ProductsUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,   // null = "All"
    val searchQuery: String = "",
    val selectedProduct: Product? = null,   // For detail dialog
    val error: String? = null
)
```

---

### Step 2: Load Products

**File**: `ui/viewmodel/ProductViewModel.kt`

```kotlin
fun loadProducts(includeUnavailable: Boolean = false) {
    viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        val category = _uiState.value.selectedCategory
        val search = _uiState.value.searchQuery.takeIf { it.isNotBlank() }
        val isAvailable = if (includeUnavailable) null else true
        
        when (val result = productRepository.getProducts(
            category = category,
            search = search,
            isAvailable = isAvailable
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
        }
    }
}
```

---

### Step 3: Repository API Call

**File**: `data/repository/ProductRepository.kt`

```kotlin
suspend fun getProducts(
    page: Int = 1,
    limit: Int = 20,
    category: String? = null,
    isAvailable: Boolean? = true,
    search: String? = null
): Result<ProductsResponse> {
    return try {
        val response = apiService.getProducts(page, limit, category, isAvailable, search)
        if (response.isSuccessful) {
            Result.Success(response.body()!!)
        } else {
            Result.Error("Failed to get products", response.code())
        }
    } catch (e: Exception) {
        Result.Error(e.message ?: "Unknown error")
    }
}
```

**API Endpoint:**
```kotlin
@GET("products")
suspend fun getProducts(
    @Query("page") page: Int = 1,
    @Query("limit") limit: Int = 10,
    @Query("category") category: String? = null,
    @Query("is_available") isAvailable: Boolean? = null,
    @Query("search") search: String? = null
): Response<ProductsResponse>
```

---

### Step 4: Display in HomeScreen

**File**: `ui/screens/home/HomeScreen.kt`

```kotlin
@Composable
fun HomeScreen(
    userName: String,
    productsUiState: ProductsUiState,
    cartUiState: CartUiState,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onCategoryClick: (String?) -> Unit,
    onSearchChange: (String) -> Unit,
    ...
) {
    // Categories
    CategorySection(
        categories = productsUiState.categories,
        selectedCategory = productsUiState.selectedCategory,
        onCategoryClick = onCategoryClick
    )
    
    // Products Grid
    LazyVerticalGrid(columns = GridCells.Fixed(2)) {
        items(productsUiState.products) { product ->
            ProductCard(
                product = product,
                onClick = { onProductClick(product) },
                onAddToCart = { onAddToCart(product) }
            )
        }
    }
}
```

---

## 🔍 Category Filtering

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                          CATEGORY FILTER FLOW                                 │
└──────────────────────────────────────────────────────────────────────────────┘

User taps "Vegetables" chip
         │
         ▼
┌────────────────────────┐
│ onCategoryClick        │
│ ("Vegetables")         │
└───────────┬────────────┘
            │
            ▼
┌────────────────────────────────────────────────────────────────┐
│ ProductViewModel.setSelectedCategory("Vegetables")             │
│                                                                 │
│   fun setSelectedCategory(category: String?) {                 │
│       _uiState.update { it.copy(selectedCategory = category) } │
│       loadProducts()  // Reload with new filter                │
│   }                                                             │
└────────────────────────────────────────────────────────────────┘
            │
            ▼
┌────────────────────────────────────────────────────────────────┐
│ API: GET /products?category=Vegetables&is_available=true       │
└────────────────────────────────────────────────────────────────┘
            │
            ▼
┌────────────────────────────────────────────────────────────────┐
│ UI updates: Only vegetables shown, chip highlighted            │
└────────────────────────────────────────────────────────────────┘
```

---

## 🔎 Search Flow

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                            SEARCH FLOW                                        │
└──────────────────────────────────────────────────────────────────────────────┘

User types "apple" in search field
         │
         ▼
┌────────────────────────────────────────────────────────────────┐
│ OutlinedTextField                                               │
│   onValueChange = {                                             │
│       searchQuery = it          // Local state                  │
│       onSearchChange(it)        // Callback to parent           │
│   }                                                             │
└────────────────────────────────────────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────────────────────────────┐
│ MainActivity routes to ViewModel:                               │
│                                                                 │
│   onSearchChange = { query ->                                   │
│       productViewModel.setSearchQuery(query)                    │
│   }                                                             │
└────────────────────────────────────────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────────────────────────────┐
│ ProductViewModel.setSearchQuery("apple")                        │
│                                                                 │
│   fun setSearchQuery(query: String) {                          │
│       _uiState.update { it.copy(searchQuery = query) }         │
│       loadProducts()  // Reload with search                     │
│   }                                                             │
└────────────────────────────────────────────────────────────────┘
         │
         ▼
┌────────────────────────────────────────────────────────────────┐
│ API: GET /products?search=apple&is_available=true              │
└────────────────────────────────────────────────────────────────┘
```

---

## 🖼️ Product Card Component

```kotlin
@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }  // Open detail dialog
    ) {
        // Product Image
        Box {
            if (product.imageUrl != null) {
                AsyncImage(model = product.imageUrl, ...)
            } else {
                Text("🛍️")  // Placeholder
            }
        }
        
        // Product Info
        Text(product.name)
        Text("$${product.price}")
        Text(product.category)
        
        // Add to Cart Button
        Box(
            modifier = Modifier.clickable { onAddToCart() }
        ) {
            Text(if (product.isAvailable) "ADD TO CART" else "OUT OF STOCK")
        }
        
        // Buy Now Button
        Box(
            modifier = Modifier.clickable { onBuyNow() }
        ) {
            Text("BUY NOW")
        }
    }
}
```

---

## 🎯 Key Points for Exam

1. **Auto-load on Init**: Products load in ViewModel's `init` block
2. **Server-side Filtering**: Category and search filters sent to API
3. **State Combination**: Filter + search can work together
4. **LazyVerticalGrid**: Efficient grid layout for many products
5. **Coil AsyncImage**: Async image loading library

---

## 🗣️ How to Explain This

> "When the user navigates to the HomeScreen, the ProductViewModel initializes and automatically calls loadProducts(). This makes an API call to fetch available products. The products are stored in the StateFlow and the HomeScreen observes this to display them in a grid. When the user taps a category chip, setSelectedCategory is called, which updates the state and reloads products with the category filter. Same for search - each keystroke updates the search query and triggers a new API call with that search parameter."

---

*Duration: Products load in ~1-2 seconds*
