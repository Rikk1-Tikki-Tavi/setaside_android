# Scenario: Add to Cart

**Path:** `View (HomeScreen)` → `ViewModel (CartViewModel)` → `Local State (Memory)` [NO API CALL]

## 📊 Flow Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                           ADD TO CART FLOW                                    │
│                     (Local State - No API Call!)                             │
└──────────────────────────────────────────────────────────────────────────────┘

┌─────────┐      ┌──────────────┐      ┌──────────────┐
│  USER   │      │    VIEW      │      │  VIEWMODEL   │
│         │      │  HomeScreen  │      │ CartViewModel│
└────┬────┘      └──────┬───────┘      └──────┬───────┘
     │                  │                     │
     │ 1. Tap          │                     │
     │ "ADD TO CART"    │                     │
     │─────────────────>│                     │
     │                  │                     │
     │                  │ 2. onAddToCart      │
     │                  │    (product)        │
     │                  │────────────────────>│
     │                  │                     │
     │                  │              ┌──────┴──────┐
     │                  │              │ 3. Check if │
     │                  │              │ product     │
     │                  │              │ exists in   │
     │                  │              │ cart        │
     │                  │              └──────┬──────┘
     │                  │                     │
     │                  │              ┌──────┴──────┐
     │                  │              │ 4A. EXISTS: │
     │                  │              │ quantity++  │
     │                  │              │             │
     │                  │              │ 4B. NEW:    │
     │                  │              │ add item    │
     │                  │              └──────┬──────┘
     │                  │                     │
     │                  │              ┌──────┴──────┐
     │                  │              │ 5. Update   │
     │                  │              │ StateFlow   │
     │                  │              │ with new    │
     │                  │              │ cart items  │
     │                  │              └──────┬──────┘
     │                  │                     │
     │                  │<────────────────────│
     │                  │ 6. State emitted    │
     │                  │                     │
     │<─────────────────│                     │
     │ 7. Cart badge    │                     │
     │    updates       │                     │
     │    (shows count) │                     │
     │                  │                     │
     ▼                  ▼                     ▼

     ┌────────────────────────────────────────────────────────┐
     │  NOTE: Cart is stored in ViewModel MEMORY only!       │
     │  No API call until CHECKOUT                            │
     │  Cart is lost if app is closed                         │
     └────────────────────────────────────────────────────────┘
```

---

## 📝 User Action

**User taps "ADD TO CART" button on a product card**

- **Input**: Product to add
- **Result**: Item added to local cart, badge updates

---

## 🔄 Code Flow

### Step 1: VIEW LAYER - Product Card

**File**: `ui/screens/home/HomeScreen.kt`

```kotlin
@Composable
fun ProductCard(
    product: Product,
    onAddToCart: () -> Unit,
    ...
) {
    // Add to Cart Button
    Box(
        modifier = Modifier
            .clickable(enabled = product.isAvailable) { 
                onAddToCart()  // Triggers callback
            }
    ) {
        Text(
            text = if (product.isAvailable) "ADD TO CART" else "OUT OF STOCK"
        )
    }
}
```

---

### Step 2: HomeScreen Routes Event

**File**: `ui/screens/home/HomeScreen.kt`

```kotlin
LazyVerticalGrid(...) {
    items(productsUiState.products) { product ->
        ProductCard(
            product = product,
            onAddToCart = { onAddToCart(product) }  // Pass product up
        )
    }
}
```

---

### Step 3: MainActivity Routes to ViewModel

**File**: `MainActivity.kt`

```kotlin
HomeScreen(
    ...
    onAddToCart = { product ->
        cartViewModel.addToCart(product)
    },
    ...
)
```

---

### Step 4: VIEWMODEL - Cart Logic

**File**: `ui/viewmodel/CartViewModel.kt`

```kotlin
class CartViewModel(private val orderRepository: OrderRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()
    
    fun addToCart(product: Product, quantity: Int = 1, specialInstructions: String? = null) {
        _uiState.update { state ->
            // Check if product already in cart
            val existingItem = state.items.find { it.product.id == product.id }
            
            if (existingItem != null) {
                // UPDATE existing item - increase quantity
                val updatedItems = state.items.map {
                    if (it.product.id == product.id) {
                        it.copy(quantity = it.quantity + quantity)
                    } else {
                        it
                    }
                }
                state.copy(items = updatedItems)
            } else {
                // ADD new item to cart
                state.copy(
                    items = state.items + CartItem(product, quantity, specialInstructions)
                )
            }
        }
    }
}
```

---

### Step 5: CartUiState

**File**: `ui/viewmodel/CartViewModel.kt`

```kotlin
data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val isCheckingOut: Boolean = false,
    val checkoutSuccess: Boolean = false,
    val lastOrderId: String? = null,
    val error: String? = null
) {
    // Computed properties
    val totalItems: Int
        get() = items.sumOf { it.quantity }
    
    val totalPrice: Double
        get() = items.sumOf { it.totalPrice }
    
    val isEmpty: Boolean
        get() = items.isEmpty()
}
```

---

### Step 6: CartItem Model

**File**: `data/model/CartItem.kt`

```kotlin
data class CartItem(
    val product: Product,
    var quantity: Int = 1,
    var specialInstructions: String? = null
) {
    val totalPrice: Double
        get() = product.price * quantity
}
```

---

## 🛒 Cart Badge Update

**File**: `ui/screens/home/HomeScreen.kt`

```kotlin
// Cart icon with badge in header
Box(modifier = Modifier.size(38.dp)) {
    IconButton(onClick = onCartClick) {
        Icon(
            imageVector = Icons.Outlined.ShoppingCart,
            contentDescription = "Cart"
        )
    }

    // Badge shows item count
    if (cartUiState.totalItems > 0) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .background(Color(0xFFFF993B), RoundedCornerShape(9.dp))
                .align(Alignment.TopEnd)
        ) {
            Text(
                text = cartUiState.totalItems.toString(),
                fontSize = 10.sp,
                color = Color.White
            )
        }
    }
}
```

---

## 📊 Cart State Visualization

```
┌────────────────────────────────────────────────────────────────────────────┐
│                         CartUiState Example                                 │
├────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  items: [                                                                   │
│    CartItem(                                                                │
│      product: Product(id: "1", name: "Apple", price: 2.99),                │
│      quantity: 3,                                                           │
│      specialInstructions: null                                              │
│    ),                                                      ─┐               │
│    CartItem(                                                │               │
│      product: Product(id: "2", name: "Milk", price: 4.50), │ In Memory     │
│      quantity: 1,                                           │ Only!         │
│      specialInstructions: "Low fat please"                  │               │
│    )                                                       ─┘               │
│  ]                                                                          │
│                                                                             │
│  Computed Values:                                                           │
│  ├─ totalItems: 4  (3 + 1)                                                 │
│  ├─ totalPrice: 13.47  ((2.99 × 3) + (4.50 × 1))                          │
│  └─ isEmpty: false                                                          │
│                                                                             │
└────────────────────────────────────────────────────────────────────────────┘
```

---

## 🔧 Other Cart Operations

### Update Quantity

```kotlin
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
```

### Remove from Cart

```kotlin
fun removeFromCart(productId: String) {
    _uiState.update { state ->
        state.copy(items = state.items.filter { it.product.id != productId })
    }
}
```

### Clear Cart

```kotlin
fun clearCart() {
    _uiState.update { CartUiState() }
}
```

---

## 🎯 Key Points for Exam

| Point | Explanation |
|-------|-------------|
| **Local State** | Cart stored in ViewModel memory, NOT on server |
| **No API Call** | Adding to cart is instant, no network request |
| **Immutable Updates** | Use `state.copy()` for immutable state updates |
| **Duplicate Handling** | Same product → increase quantity instead of duplicate entry |
| **Computed Properties** | `totalItems` and `totalPrice` auto-calculate |
| **Lost on Close** | Cart data lost when app closes (not persisted) |

---

## 🗣️ How to Explain This

> "The Add to Cart flow is entirely local - no API call is made. When the user taps 'ADD TO CART', the callback triggers cartViewModel.addToCart() with the product. The ViewModel first checks if this product already exists in the cart. If it does, it increases the quantity. If not, it creates a new CartItem and adds it to the list. The state update uses Kotlin's immutable copy pattern - we create a new list instead of modifying the existing one. This triggers StateFlow emission, and the UI recomposes. The cart badge in the header observes cartUiState.totalItems, so it automatically updates to show the new count. The cart data stays in ViewModel memory until the user checks out, at which point we transform it into an API request."

---

*Duration: Instant (no network call)*
