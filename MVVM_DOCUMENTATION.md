# SetAside Android - MVVM Architecture Documentation

## 📋 Table of Contents
1. [Project Overview](#project-overview)
2. [Technology Stack](#technology-stack)
3. [MVVM Architecture Implementation](#mvvm-architecture-implementation)
4. [Project Structure](#project-structure)
5. [Layer-by-Layer Documentation](#layer-by-layer-documentation)
6. [Scenario-Based Code Flows](#scenario-based-code-flows)
7. [State Management](#state-management)
8. [Common Exam Questions & Answers](#common-exam-questions--answers)

---

## 1. Project Overview

**SetAside** is an Android e-commerce application that allows customers to browse products, add them to cart, and place orders. It also includes an admin panel for managing products and orders.

### Main Features
| Feature | Description |
|---------|-------------|
| **Authentication** | User registration, login, logout, profile management |
| **Product Browsing** | View products, filter by category, search functionality |
| **Shopping Cart** | Add/remove items, update quantities, special instructions |
| **Order Management** | Place orders, view order history, track order status |
| **Admin Panel** | Manage products (CRUD), manage orders, update order status |

### User Roles
- **Customer**: Browse products, manage cart, place orders
- **Admin/Cashier**: All customer features + product/order management

---

## 2. Technology Stack

| Component | Technology |
|-----------|------------|
| **Language** | Kotlin |
| **UI Framework** | Jetpack Compose |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **Reactive Library** | Kotlin StateFlow & Coroutines |
| **Networking** | Retrofit + OkHttp |
| **JSON Parsing** | Gson |
| **Image Loading** | Coil |
| **Local Storage** | DataStore Preferences |
| **Navigation** | Jetpack Navigation Compose |

---

## 3. MVVM Architecture Implementation

### Architecture Diagram
```
┌─────────────────────────────────────────────────────────────────┐
│                         VIEW LAYER                               │
│  (Composable Screens: HomeScreen, CartScreen, SignInScreen...)  │
│         ↑ Observes StateFlow    │ Triggers Events               │
└────────────────────────────────────────────────────────────────┘
                    ↑                          ↓
┌─────────────────────────────────────────────────────────────────┐
│                      VIEWMODEL LAYER                             │
│    (AuthViewModel, ProductViewModel, CartViewModel, OrderVM)    │
│    - Holds UI State (StateFlow<UiState>)                        │
│    - Business Logic                                              │
│    - Transforms data for UI                                      │
└─────────────────────────────────────────────────────────────────┘
                    ↑                          ↓
┌─────────────────────────────────────────────────────────────────┐
│                    REPOSITORY LAYER                              │
│      (AuthRepository, ProductRepository, OrderRepository)       │
│    - Single source of truth                                      │
│    - Abstracts data sources                                      │
└─────────────────────────────────────────────────────────────────┘
                    ↑                          ↓
┌─────────────────────────────────────────────────────────────────┐
│                      DATA SOURCES                                │
│  ┌──────────────────┐          ┌──────────────────────────────┐ │
│  │   Remote API     │          │    Local Storage             │ │
│  │   (ApiService    │          │    (TokenManager/DataStore)  │ │
│  │   + Retrofit)    │          │                              │ │
│  └──────────────────┘          └──────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

### Data Flow Pattern
**Unidirectional Data Flow (UDF)**:
1. **User Action** → View triggers event
2. **Event** → ViewModel processes action
3. **State Update** → ViewModel updates StateFlow
4. **UI Recomposition** → View observes and recomposes

---

## 4. Project Structure

```
app/src/main/java/com/example/setaside/
├── MainActivity.kt              # Entry point, navigation setup
├── SetAsideApplication.kt       # Application class
│
├── data/
│   ├── local/
│   │   └── TokenManager.kt      # DataStore for auth tokens
│   ├── model/
│   │   ├── User.kt              # User & auth models
│   │   ├── Product.kt           # Product models
│   │   ├── Order.kt             # Order models
│   │   └── CartItem.kt          # Cart item model
│   ├── remote/
│   │   ├── ApiService.kt        # Retrofit API interface
│   │   └── RetrofitClient.kt    # Retrofit configuration
│   └── repository/
│       ├── AuthRepository.kt    # Auth data operations
│       ├── ProductRepository.kt # Product data operations
│       └── OrderRepository.kt   # Order data operations
│
└── ui/
    ├── components/
    │   └── CommonComponents.kt  # Reusable UI components
    ├── screens/
    │   ├── auth/                # SignInScreen, SignUpScreen
    │   ├── home/                # HomeScreen, ProductDetailDialog
    │   ├── cart/                # CartScreen, CheckoutScreen
    │   ├── orders/              # OrdersScreen, OrderDetailScreen
    │   ├── profile/             # ProfileScreen
    │   └── admin/               # AdminOrdersScreen, ProductsManagementScreen
    ├── theme/                   # Color, Theme, Typography
    └── viewmodel/
        ├── AuthViewModel.kt     # Authentication logic
        ├── ProductViewModel.kt  # Product management logic
        ├── CartViewModel.kt     # Cart management logic
        └── OrderViewModel.kt    # Order management logic
```

---

## 5. Layer-by-Layer Documentation

### 5.1 MODEL LAYER

#### Data Classes

**User.kt** - User entity and auth requests/responses:
```kotlin
data class User(
    val id: String = "",
    val email: String = "",
    @SerializedName("full_name") val fullName: String = "",
    val phone: String? = null,
    val role: String = "customer",  // "customer", "admin", "cashier"
    @SerializedName("created_at") val createdAt: String? = null
)

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val password: String, 
                           @SerializedName("full_name") val fullName: String, 
                           val phone: String? = null)
data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    val user: User? = null
)
```

**Product.kt** - Product entity:
```kotlin
data class Product(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val price: Double = 0.0,
    val category: String = "",
    @SerializedName("is_available") val isAvailable: Boolean = true,
    @SerializedName("stock_quantity") val stockQuantity: Int? = null,
    @SerializedName("image_url") val imageUrl: String? = null
)
```

**Order.kt** - Order entity with status enum:
```kotlin
data class Order(
    val id: String = "",
    @SerializedName("customer_id") val customerId: String = "",
    val status: OrderStatus? = OrderStatus.PENDING,
    val notes: String? = null,
    @SerializedName("total_amount") val totalAmount: Double = 0.0,
    val items: List<OrderItem> = emptyList()
)

enum class OrderStatus {
    @SerializedName("pending") PENDING,
    @SerializedName("preparing") PREPARING,
    @SerializedName("ready") READY,
    @SerializedName("pickedup") PICKEDUP,
    @SerializedName("completed") COMPLETED
}
```

**CartItem.kt** - Local cart management:
```kotlin
data class CartItem(
    val product: Product,
    var quantity: Int = 1,
    var specialInstructions: String? = null
) {
    val totalPrice: Double get() = product.price * quantity
}
```

### 5.2 REPOSITORY LAYER

**Result Sealed Class** - Wrapper for API responses:
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: Int = -1) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

**AuthRepository.kt** - Handles authentication:
```kotlin
class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String, fullName: String, phone: String?): Result<User>
    suspend fun logout()
    suspend fun updateProfile(fullName: String?, phone: String?): Result<User>
}
```

**ProductRepository.kt** - Handles product operations:
```kotlin
class ProductRepository(private val apiService: ApiService) {
    suspend fun getProducts(page: Int, limit: Int, category: String?, 
                           isAvailable: Boolean?, search: String?): Result<ProductsResponse>
    suspend fun getProduct(id: String): Result<Product>
    suspend fun createProduct(request: CreateProductRequest): Result<Product>
    suspend fun updateProduct(id: String, updates: Map<String, Any>): Result<Product>
    suspend fun deleteProduct(id: String): Result<Unit>
}
```

### 5.3 VIEWMODEL LAYER

**UI State Pattern** - Each ViewModel has a corresponding UiState:
```kotlin
data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val userName: String = "",
    val userEmail: String = "",
    val userRole: String = "customer",
    val error: String? = null,
    val isAdmin: Boolean = false
)
```

**ViewModel Structure**:
```kotlin
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            when (val result = authRepository.login(email, password)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = true, ...) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }
}
```

### 5.4 VIEW LAYER

**Composable Screen Pattern**:
```kotlin
@Composable
fun SignInScreen(
    uiState: AuthUiState,              // State from ViewModel
    onLogin: (String, String) -> Unit,  // Event callbacks
    onNavigateToSignUp: () -> Unit,
    onClearError: () -> Unit
) {
    // Local UI state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    // Error handling
    if (uiState.error != null) {
        ErrorDialog(message = uiState.error, onDismiss = onClearError)
    }
    
    // UI implementation...
    Button(onClick = { onLogin(email, password) }) { ... }
}
```

---

## 6. Scenario-Based Code Flows

### Scenario 1: User Login

**User Action**: User enters email/password and taps "Sign In"

#### Step-by-Step Code Flow:

**1. VIEW LAYER** - `SignInScreen.kt`
```kotlin
// User enters credentials in TextField
var email by remember { mutableStateOf("") }
var password by remember { mutableStateOf("") }

// User taps Sign In button
LoadingButton(
    text = "Sign In",
    onClick = { onLogin(email, password) },  // Triggers callback
    isLoading = uiState.isLoading
)
```

**2. MAIN ACTIVITY** - Routes event to ViewModel
```kotlin
SignInScreen(
    uiState = authState,
    onLogin = { email, password ->
        authViewModel.login(email, password)  // Calls ViewModel
    }
)
```

**3. VIEWMODEL LAYER** - `AuthViewModel.kt`
```kotlin
fun login(email: String, password: String) {
    viewModelScope.launch {
        // Set loading state
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        // Call repository
        when (val result = authRepository.login(email, password)) {
            is Result.Success -> {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        userName = result.data.fullName,
                        isAdmin = result.data.role == "admin"
                    )
                }
            }
            is Result.Error -> {
                _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }
}
```

**4. REPOSITORY LAYER** - `AuthRepository.kt`
```kotlin
suspend fun login(email: String, password: String): Result<User> {
    return try {
        // API call
        val response = apiService.login(LoginRequest(email, password))
        if (response.isSuccessful) {
            val authResponse = response.body()!!
            tokenManager.saveAccessToken(authResponse.accessToken)
            
            // Fetch user details
            val userResponse = apiService.getCurrentUser()
            if (userResponse.isSuccessful) {
                val user = userResponse.body()!!
                tokenManager.saveAuthData(token, userId, email, name, role)
                Result.Success(user)
            }
        } else {
            Result.Error("Login failed: ${response.message()}")
        }
    } catch (e: Exception) {
        Result.Error(e.message ?: "Unknown error")
    }
}
```

**5. RETURN PATH** - UI Updates via StateFlow
```kotlin
// In MainActivity - observing state
val authState by authViewModel.uiState.collectAsState()

// When isLoggedIn becomes true, LaunchedEffect triggers navigation
LaunchedEffect(authState.isLoggedIn) {
    if (authState.isLoggedIn) {
        val destination = if (authState.isAdmin) "admin_home" else "home"
        navController.navigate(destination) {
            popUpTo("signin") { inclusive = true }
        }
    }
}
```

---

### Scenario 2: Add Product to Cart

**User Action**: User taps "ADD TO CART" on a product

#### Code Flow:

**1. VIEW** - `HomeScreen.kt`
```kotlin
ProductCard(
    product = product,
    onAddToCart = { onAddToCart(product) }  // Callback triggered
)
```

**2. MAIN ACTIVITY** - Routes to ViewModel
```kotlin
onAddToCart = { product ->
    cartViewModel.addToCart(product)
}
```

**3. VIEWMODEL** - `CartViewModel.kt`
```kotlin
fun addToCart(product: Product, quantity: Int = 1, specialInstructions: String? = null) {
    _uiState.update { state ->
        val existingItem = state.items.find { it.product.id == product.id }
        if (existingItem != null) {
            // Update existing item quantity
            val updatedItems = state.items.map {
                if (it.product.id == product.id) {
                    it.copy(quantity = it.quantity + quantity)
                } else it
            }
            state.copy(items = updatedItems)
        } else {
            // Add new item
            state.copy(items = state.items + CartItem(product, quantity, specialInstructions))
        }
    }
}
```

**Key Point**: Cart is managed **locally** in ViewModel memory - no API call needed until checkout.

---

### Scenario 3: Checkout Flow

**User Action**: User taps "PLACE ORDER" on checkout screen

#### Code Flow:

**1. VIEW** - `CheckoutScreen.kt`
```kotlin
Button(onClick = { onPlaceOrder(notes) })
```

**2. VIEWMODEL** - `CartViewModel.kt`
```kotlin
fun checkout(notes: String? = null) {
    viewModelScope.launch {
        _uiState.update { it.copy(isCheckingOut = true) }
        
        // Transform cart items to order request
        val orderItems = _uiState.value.items.map { cartItem ->
            CreateOrderItemRequest(
                productId = cartItem.product.id,
                quantity = cartItem.quantity,
                specialInstructions = cartItem.specialInstructions
            )
        }
        
        val request = CreateOrderRequest(notes = notes, items = orderItems)
        
        when (val result = orderRepository.createOrder(request)) {
            is Result.Success -> {
                _uiState.update {
                    CartUiState(  // Reset cart, store order ID
                        checkoutSuccess = true,
                        lastOrderId = result.data.id
                    )
                }
            }
            is Result.Error -> {
                _uiState.update { it.copy(isCheckingOut = false, error = result.message) }
            }
        }
    }
}
```

**3. REPOSITORY** - `OrderRepository.kt`
```kotlin
suspend fun createOrder(request: CreateOrderRequest): Result<Order> {
    val response = apiService.createOrder(request)
    return if (response.isSuccessful) {
        Result.Success(response.body()!!)
    } else {
        Result.Error("Failed to create order")
    }
}
```

---

## 7. State Management

### UI State Classes Summary

| ViewModel | UiState | Key Properties |
|-----------|---------|----------------|
| AuthViewModel | AuthUiState | isLoading, isLoggedIn, user, error, isAdmin |
| ProductViewModel | ProductsUiState | isLoading, products, categories, selectedCategory, searchQuery |
| CartViewModel | CartUiState | items, isCheckingOut, checkoutSuccess, totalItems, totalPrice |
| OrderViewModel | OrdersUiState | isLoading, orders, selectedOrder, filterStatus |

### StateFlow Usage Pattern
```kotlin
// ViewModel exposes immutable StateFlow
val uiState: StateFlow<UiState> = _uiState.asStateFlow()

// View collects as Compose state
val state by viewModel.uiState.collectAsState()

// State updates trigger recomposition automatically
```

---

## 8. Common Exam Questions & Answers

### Q1: "Why did you choose MVVM?"

**Answer**: 
- **Separation of Concerns**: UI logic separated from business logic
- **Testability**: ViewModels can be unit tested without UI
- **Lifecycle Awareness**: ViewModels survive configuration changes
- **Reactive Updates**: StateFlow provides automatic UI updates
- **Google Recommended**: Official Android architecture pattern

### Q2: "How does data binding work in your implementation?"

**Answer**:
We use **Kotlin StateFlow** with Jetpack Compose:
1. ViewModel holds `MutableStateFlow<UiState>`
2. Exposes as immutable `StateFlow` to views
3. Composables use `collectAsState()` to observe
4. Any state change triggers recomposition

```kotlin
// ViewModel
private val _uiState = MutableStateFlow(AuthUiState())
val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

// View
val authState by authViewModel.uiState.collectAsState()
```

### Q3: "How do you handle errors?"

**Answer**:
- Repository returns `Result<T>` sealed class (Success/Error/Loading)
- ViewModel catches errors and updates `error` property in UiState
- View shows `ErrorDialog` when `uiState.error != null`
- User dismisses dialog, triggering `clearError()` in ViewModel

### Q4: "Explain the ViewModel lifecycle"

**Answer**:
- Created when first accessed via `viewModel()` in Composable
- Survives configuration changes (rotation)
- Cleared when associated Activity/Fragment is destroyed permanently
- Uses `viewModelScope` for coroutines that auto-cancel on ViewModel clear

### Q5: "How would you test this component?"

**Answer**:
```kotlin
// Unit test for ViewModel
@Test
fun `login success updates state correctly`() = runTest {
    val mockRepo = mockk<AuthRepository>()
    coEvery { mockRepo.login(any(), any()) } returns Result.Success(testUser)
    
    val viewModel = AuthViewModel(mockRepo)
    viewModel.login("test@email.com", "password")
    
    assertEquals(true, viewModel.uiState.value.isLoggedIn)
    assertEquals("Test User", viewModel.uiState.value.userName)
}
```

### Q6: "What design patterns are used?"

**Answer**:
| Pattern | Usage |
|---------|-------|
| **MVVM** | Overall architecture |
| **Repository** | Data abstraction layer |
| **Factory** | ViewModel creation with dependencies |
| **Observer** | StateFlow for reactive updates |
| **Singleton** | RetrofitClient for API instance |

---

## 📝 Quick Reference: Key File Locations

| Component | File Path |
|-----------|-----------|
| Entry Point | `MainActivity.kt` |
| Auth ViewModel | `ui/viewmodel/AuthViewModel.kt` |
| Product ViewModel | `ui/viewmodel/ProductViewModel.kt` |
| Cart ViewModel | `ui/viewmodel/CartViewModel.kt` |
| Auth Repository | `data/repository/AuthRepository.kt` |
| API Service | `data/remote/ApiService.kt` |
| Token Storage | `data/local/TokenManager.kt` |
| Home Screen | `ui/screens/home/HomeScreen.kt` |
| Sign In Screen | `ui/screens/auth/SignInScreen.kt` |

---

*Documentation generated for oral exam preparation - SetAside Android MVVM Project*
