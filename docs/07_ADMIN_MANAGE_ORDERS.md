# Scenario: Admin - Manage Orders (Update Status)

**Path:** `View (AdminOrdersScreen)` → `ViewModel (OrderViewModel)` → `Repository (OrderRepository)` → `API (Retrofit)`

## 📊 Flow Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                           ADMIN ORDER FLOW                                    │
└──────────────────────────────────────────────────────────────────────────────┘

┌─────────┐      ┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│  ADMIN  │      │    VIEW      │      │  VIEWMODEL   │      │  REPOSITORY  │
│         │      │AdminOrdersScn│      │ OrderViewModel│      │OrderRepository│
└────┬────┘      └──────┬───────┘      └──────┬───────┘      └──────┬───────┘
     │                  │                     │                     │
     │ 1. Filter:       │                     │                     │
     │ "Pending"        │                     │                     │
     │─────────────────>│                     │                     │
     │                  │ 2. loadOrders       │                     │
     │                  │    (status="pending")                     │
     │                  │────────────────────>│                     │
     │                  │                     │ 3. GET /orders...   │
     │                  │                     │────────────────────>│
     │                  │                     │                     │
     │<───────────────────────────────────────│<────────────────────│
     │ 4. Sees list     │                     │                     │
     │    of pending    │                     │                     │
     │    orders        │                     │                     │
     │                  │                     │                     │
     │ 5. Taps          │                     │                     │
     │ "Mark Ready"     │                     │                     │
     │─────────────────>│                     │                     │
     │                  │ 6. updateOrder      │                     │
     │                  │    Status(id,       │                     │
     │                  │    "ready")         │                     │
     │                  │────────────────────>│                     │
     │                  │                     │ 7. PATCH            │
     │                  │                     │ /orders/{id}/status │
     │                  │                     │────────────────────>│
     │                  │                     │                     │
     │                  │                     │<────────────────────│
     │                  │              ┌──────┴──────┐              │
     │                  │              │ 8. Update   │              │
     │                  │              │ Local List  │              │
     │                  │              │ (Filter out │              │
     │                  │              │ changed     │              │
     │                  │              │ item)       │              │
     │                  │              └──────┬──────┘              │
     │                  │                     │                     │
     │<─────────────────│<────────────────────│                     │
     │ 9. Order removed │                     │                     │
     │    from list     │                     │                     │
```

---

## 📝 User Action

**Admin filters by "Pending", sees an order, and changes status to "Ready"**

- **Input**: Tap "Move to Ready" button
- **Result**: Order status updated on server, UI refreshes (order moves to Ready tab)

---

## 🔄 Code Flow

### Step 1: VIEW LAYER - `AdminOrdersScreen.kt`

**File**: `ui/screens/admin/AdminOrdersScreen.kt`

```kotlin
@Composable
fun AdminOrdersScreen(
    uiState: OrdersUiState,
    onFilterChange: (OrderStatus) -> Unit,
    onUpdateStatus: (String, String) -> Unit,
    ...
) {
    // Filter Tabs
    ScrollableTabRow(...) {
        Tab(text = "Pending", onClick = { onFilterChange(OrderStatus.PENDING) })
        Tab(text = "Processing", ...)
    }

    // Order List Item with Actions
    OrderCard(
        order = order,
        actions = {
            if (order.status == OrderStatus.PENDING) {
                Button(onClick = { onUpdateStatus(order.id, "preparing") }) {
                    Text("Prepare")
                }
            }
        }
    )
}
```

---

### Step 2: VIEWMODEL - Update Logic

**File**: `ui/viewmodel/OrderViewModel.kt`

```kotlin
fun updateOrderStatus(orderId: String, newStatus: String) {
    viewModelScope.launch {
        _uiState.update { it.copy(isUpdatingStatus = true) }
        
        when (val result = orderRepository.updateOrderStatus(orderId, newStatus)) {
            is Result.Success -> {
                _uiState.update { state ->
                    // Optimistic update or reload?
                    // Here we map over existing items to update the single item
                    val updatedOrders = state.orders.map { order ->
                        if (order.id == orderId) result.data else order
                    }.filter { order ->
                        // IMPORTANT: Remove it if it no longer matches current filter
                        state.filterStatus == null || order.status == state.filterStatus
                    }
                    
                    state.copy(
                        isUpdatingStatus = false,
                        orders = updatedOrders,
                        statusUpdateSuccess = true
                    )
                }
            }
            is Result.Error -> { ... }
        }
    }
}
```

---

### Step 3: REPOSITORY - PATCH Request

**File**: `data/repository/OrderRepository.kt`

```kotlin
suspend fun updateOrderStatus(orderId: String, status: String): Result<Order> {
    return try {
        val request = UpdateOrderStatusRequest(status)
        val response = apiService.updateOrderStatus(orderId, request)
        if (response.isSuccessful) {
            Result.Success(response.body()!!)
        } else {
            Result.Error(...)
        }
    } catch (e: Exception) { ... }
}
```

---

## 🎯 Key Points for Exam

1. **Role-Based Access**: This screen is only accessible if `user.role == "admin"`.
2. **Optimistic/Smart UI Updates**: When an order status is changed (e.g., Pending -> Ready), the ViewModel logic filters it out of the current list immediately so the UI feels responsive, without needing to reload the entire list from the server.
3. **PATCH Method**: We use HTTP PATCH because we are only modifying one field (status) of the resource.

---

## 🗣️ How to Explain This

> "In the Admin panel, the OrderViewModel handles status updates. When an admin taps 'Start Preparing' on a pending order, calling updateOrderStatus(), the app sends a PATCH request to the API. Upon success, the ViewModel doesn't just reload everything; instead, it finds the order in the current memory list, updates it, and then checks if it still belongs in the currently selected filter. If I'm viewing 'Pending' orders and I move one to 'Preparing', it is automatically removed from the screen because it no longer matches the filter. This provides immediate visual feedback."

---

*Duration: ~1 second*
