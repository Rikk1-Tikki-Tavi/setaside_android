# Scenario: Checkout & Place Order

**Path:** `View (CheckoutScreen)` → `ViewModel (CartViewModel)` → `Repository (OrderRepository)` → `API (Retrofit)`

## 📊 Flow Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                           CHECKOUT & ORDER FLOW                               │
└──────────────────────────────────────────────────────────────────────────────┘

┌─────────┐      ┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│  USER   │      │    VIEW      │      │  VIEWMODEL   │      │  REPOSITORY  │
│         │      │CheckoutScreen│      │ CartViewModel│      │OrderRepository│
└────┬────┘      └──────┬───────┘      └──────┬───────┘      └──────┬───────┘
     │                  │                     │                     │
     │ 1. Tap          │                     │                     │
     │ "PROCEED"        │                     │                     │
     │─────────────────>│                     │                     │
     │                  │                     │                     │
     │                  │ 2. onPlaceOrder     │                     │
     │                  │    (notes)          │                     │
     │                  │────────────────────>│                     │
     │                  │                     │                     │
     │                  │                     │ 3. Transform Cart   │
     │                  │                     │    to CreateRequest │
     │                  │                     │                     │
     │                  │                     │ 4. createOrder()    │
     │                  │                     │────────────────────>│
     │                  │                     │                     │
     │                  │                     │                     │ 5. POST /orders
     │                  │                     │                     │──────────────────────>
     │                  │                     │                     │         API
     │                  │                     │                     │<──────────────────────
     │                  │                     │                     │ 6. Order Response
     │                  │                     │                     │                     │
     │                  │                     │<────────────────────│
     │                  │                     │ 7. Result.Success   │
     │                  │                     │                     │
     │                  │              ┌──────┴──────┐              │
     │                  │              │ 8. Update   │              │
     │                  │              │ State:      │              │
     │                  │              │ Success=True│              │
     │                  │              │ OrderId=123 │              │
     │                  │              └──────┬──────┘              │
     │                  │                     │                     │
     │<─────────────────│<────────────────────│                     │
     │ 9. See Success   │ 10. Navigates to    │                     │
     │    Screen        │     OrderDetail     │                     │
     │                  │                     │                     │
```

---

## 📝 User Action

**User taps "PROCEED TO CHECKOUT" on Cart, enters notes (optional), and taps "PLACE ORDER"**

- **Input**: Optional notes/instructions
- **Result**: API creates order, Cart cleared, Redirect to Order Confirmation

---

## 🔄 Code Flow

### Step 1: VIEW LAYER - `CheckoutScreen.kt`

**File**: `ui/screens/cart/CheckoutScreen.kt`

```kotlin
@Composable
fun CheckoutScreen(
    uiState: CartUiState,
    onPlaceOrder: (String?) -> Unit,
    onViewOrder: () -> Unit,
    ...
) {
    // Show success dialog if order placed
    if (uiState.checkoutSuccess) {
        AlertDialog(
            title = { Text("Order Placed!") },
            text = { Text("Your order has been placed successfully.") },
            confirmButton = {
                Button(onClick = onViewOrder) { Text("View Order") }
            }
        )
    }

    // Place Order Button
    Button(
        onClick = { onPlaceOrder(notes) },
        enabled = !uiState.isCheckingOut
    ) {
        if (uiState.isCheckingOut) {
            CircularProgressIndicator(...)
        } else {
            Text("PLACE ORDER")
        }
    }
}
```

---

### Step 2: MAIN ACTIVITY - Routes to ViewModel

**File**: `MainActivity.kt`

```kotlin
composable("checkout") {
    CheckoutScreen(
        uiState = cartState,
        onPlaceOrder = { notes ->
            cartViewModel.checkout(notes)  // Trigger checkout
        },
        onViewOrder = {
            cartState.lastOrderId?.let { orderId ->
                cartViewModel.resetCheckoutState()
                navController.navigate("order/$orderId") {
                    popUpTo("home")
                }
            }
        }
    )
}
```

---

### Step 3: VIEWMODEL - Business Logic

**File**: `ui/viewmodel/CartViewModel.kt`

```kotlin
fun checkout(notes: String? = null, pickupTime: String? = null) {
    viewModelScope.launch {
        // 1. Set loading state
        _uiState.update { it.copy(isCheckingOut = true, error = null) }
        
        // 2. Transform CartItems to API Request
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
        
        // 3. Call Repository
        when (val result = orderRepository.createOrder(request)) {
            is Result.Success -> {
                // 4. Clear Cart & Set Success Flag
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
        }
    }
}
```

**Key Concept**: Note how `CartUiState` is reset to a new instance `CartUiState(...)` on success. This effectively **clears the cart** because the default list of items is empty.

---

### Step 4: REPOSITORY LAYER - API Call

**File**: `data/repository/OrderRepository.kt`

```kotlin
suspend fun createOrder(request: CreateOrderRequest): Result<Order> {
    return try {
        val response = apiService.createOrder(request)
        if (response.isSuccessful) {
            Result.Success(response.body()!!)
        } else {
            Result.Error("Failed to create order", response.code())
        }
    } catch (e: Exception) {
        Result.Error(e.message ?: "Unknown error")
    }
}
```

**API Interface (`ApiService.kt`)**:
```kotlin
@POST("orders")
suspend fun createOrder(@Body request: CreateOrderRequest): Response<Order>
```

---

## 📦 Data Transformation

The most critical part of this flow is transforming the **local cart model** to the **network request model**.

**Local Model (`CartItem`):**
```kotlin
data class CartItem(
    val product: Product,
    var quantity: Int,
    var specialInstructions: String?
)
```

**Network Request (`CreateOrderRequest`):**
```kotlin
data class CreateOrderRequest(
    val notes: String?,
    val items: List<CreateOrderItemRequest>
)

data class CreateOrderItemRequest(
    @SerializedName("product_id") val productId: String,
    val quantity: Int,
    @SerializedName("special_instructions") val specialInstructions: String?
)
```

*The ViewModel performs this mapping.*

---

## 🎯 Key Points for Exam

1. **Transactional State**: The cart is cleared ONLY after the server returns success (200 OK).
2. **Data Transformation**: ViewModel maps UI-friendly `CartItem` objects to API-friendly `CreateOrderRequest` DTOs.
3. **State Reset**: `CartViewModel.resetCheckoutState()` is called after navigation to prevent the success dialog from showing again if the user navigates back.
4. **Error Handling**: If checkout fails, the cart items remain intact so the user can try again.

---

## 🗣️ How to Explain This

> "When the user proceeds to checkout and taps 'Place Order', the checkout function in CartViewModel is called. First, it sets isCheckingOut to true to show a spinner. Then, it transforms the list of local CartItems into a CreateOrderRequest object that the API expects, extracting only the necessary data like product IDs and quantities. This request is sent via OrderRepository. If the API returns success, the ViewModel completely replaces the current state with a fresh CartUiState, effectively clearing the cart, and sets the checkoutSuccess flag. This flag triggers a success dialog in the View, which then allows the user to navigate to the order details screen."

---

*Duration: API call ~1-3 seconds*
