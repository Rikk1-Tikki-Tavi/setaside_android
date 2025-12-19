# Scenario: View Orders & History

**Path:** `View (OrdersScreen)` → `ViewModel (OrderViewModel)` → `Repository (OrderRepository)` → `API (Retrofit)`

## 📊 Flow Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                            VIEW ORDERS FLOW                                   │
└──────────────────────────────────────────────────────────────────────────────┘

┌─────────┐      ┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│  USER   │      │    VIEW      │      │  VIEWMODEL   │      │  REPOSITORY  │
│         │      │ OrdersScreen │      │ OrderViewModel│      │OrderRepository│
└────┬────┘      └──────┬───────┘      └──────┬───────┘      └──────┬───────┘
     │                  │                     │                     │
     │ 1. Navigates to  │                     │                     │
     │    Orders Tab    │                     │                     │
     │─────────────────>│                     │                     │
     │                  │                     │                     │
     │                  │ 2. LaunchedEffect   │                     │
     │                  │    loadOrders()     │                     │
     │                  │────────────────────>│                     │
     │                  │                     │                     │
     │                  │                     │ 3. getOrders()      │
     │                  │                     │────────────────────>│
     │                  │                     │                     │
     │                  │                     │                     │ 4. GET /orders
     │                  │                     │                     │──────────────────────>
     │                  │                     │                     │         API
     │                  │                     │                     │<──────────────────────
     │                  │                     │                     │ 5. Orders List
     │                  │                     │                     │                     
     │                  │                     │<────────────────────│
     │                  │                     │ 6. Result.Success   │
     │                  │                     │                     │
     │                  │              ┌──────┴──────┐              │
     │                  │              │ 7. Update   │              │
     │                  │              │ State with  │              │
     │                  │              │ List<Order> │              │
     │                  │              └──────┬──────┘              │
     │                  │                     │                     │
     │<─────────────────│<────────────────────│                     │
     │ 8. Values shown  │ 9. UI Recomposes    │                     │
     │    in LazyCol    │                     │                     │
     │                  │                     │                     │
```

---

## 📝 User Action

**User taps the "Orders" tab in the bottom navigation bar**

- **Result**: Displays list of past orders with status (Pending, Ready, etc.)

---

## 🔄 Code Flow

### Step 1: VIEW LAYER - `OrdersScreen.kt`

**File**: `ui/screens/orders/OrdersScreen.kt`

```kotlin
@Composable
fun OrdersScreen(
    uiState: OrdersUiState,
    onRefresh: () -> Unit,
    ...
) {
    // 1. Refresh logic (optional, usually handled by LaunchedEffect in parent)
    
    // 2. Loading State
    if (uiState.isLoading) {
        CircularProgressIndicator(...)
    }
    
    // 3. Orders List
    LazyColumn {
        items(uiState.orders) { order ->
            OrderCard(
                order = order,
                onClick = { onOrderClick(order) }
            )
        }
    }
}
```

---

### Step 2: MAIN ACTIVITY - Load on Navigation

**File**: `MainActivity.kt`

```kotlin
composable("orders") {
    OrdersScreen(
        uiState = ordersState,
        ...
    )

    // Trigger load when screen opens
    LaunchedEffect(Unit) {
        orderViewModel.loadOrders()
    }
}
```

---

### Step 3: VIEWMODEL - Data Loading

**File**: `ui/viewmodel/OrderViewModel.kt`

```kotlin
class OrderViewModel(private val orderRepository: OrderRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OrdersUiState())
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()
    
    fun loadOrders(status: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            when (val result = orderRepository.getOrders(status = status)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            orders = result.data.orders // Update list
                        )
                    }
                }
                is Result.Error -> { ... }
            }
        }
    }
}
```

---

### Step 4: REPOSITORY - Filters

**File**: `data/repository/OrderRepository.kt`

```kotlin
suspend fun getOrders(
    page: Int = 1,
    limit: Int = 1000,
    status: String? = null,
    customerId: String? = null
): Result<OrdersResponse> {
    // ... Implementation ...
}
```

Note: The `customerId` is handled by the server based on the authentication token. The client doesn't need to explicitly pass it for "my orders".

---

## 📋 Order Detail Flow

**User taps an order card to view details**

```kotlin
// MainActivity.kt
composable(
    route = "order/{orderId}",
    arguments = listOf(navArgument("orderId") { type = NavType.StringType })
) { backStackEntry ->
    val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
    
    OrderDetailScreen(
        uiState = ordersState,
        orderId = orderId,
        onLoadOrder = { orderViewModel.loadOrderDetails(it) }
    )
}
```

### ViewModel Logic (`loadOrderDetails`):
1. Sets `isLoading = true`
2. calls `orderRepository.getOrder(orderId)`
3. Updates `uiState.selectedOrder` with the result

---

## 🎯 Key Points for Exam

1. **LaunchedEffect**: Used to trigger data loading when a composable enters the composition.
2. **LazyColumn**: Used for rendering lists efficiently.
3. **Filtering**: `loadOrders` accepts an optional status parameter for filtering (Pending, Completed, etc.).
4. **Master-Detail Pattern**: The list view (`orders`) navigates to a detail view (`order/{id}`) using the ID as a route argument.

---

## 🗣️ How to Explain This

> "When the user navigates to the Orders screen, a LaunchedEffect block triggers the orderViewModel.loadOrders() function. This sends a GET request to the API. Since the user is authenticated with a JWT token, the server automatically knows to return only orders belonging to this user. The list of orders is stored in the OrdersUiState. The view renders this list using a LazyColumn. If the user taps on an order, we navigate to the detail screen passing the order ID, which triggers a separate API call to fetch full details including line items for that specific order."

---

*Duration: List load ~0.5-2 seconds*
