# SetAside Android - MVVM Documentation

## 📚 Documentation Index

This folder contains scenario-based documentation for the SetAside Android app using MVVM architecture.

---

## 🏗️ Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                         USER                                 │
│                    (Taps, Types, Scrolls)                   │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      VIEW LAYER                              │
│         (Jetpack Compose Screens & Components)              │
│                                                              │
│   SignInScreen │ HomeScreen │ CartScreen │ OrdersScreen    │
│                                                              │
│   • Displays UI based on State                              │
│   • Sends user events to ViewModel                          │
│   • Observes StateFlow using collectAsState()               │
└─────────────────────────────────────────────────────────────┘
                    │                    ▲
            Events  │                    │  State (StateFlow)
                    ▼                    │
┌─────────────────────────────────────────────────────────────┐
│                    VIEWMODEL LAYER                           │
│                                                              │
│   AuthViewModel │ ProductViewModel │ CartViewModel │ OrderVM│
│                                                              │
│   • Holds UI State (MutableStateFlow<UiState>)              │
│   • Processes user events                                    │
│   • Calls Repository methods                                 │
│   • Uses viewModelScope for coroutines                      │
└─────────────────────────────────────────────────────────────┘
                    │                    ▲
          Requests  │                    │  Result<T>
                    ▼                    │
┌─────────────────────────────────────────────────────────────┐
│                   REPOSITORY LAYER                           │
│                                                              │
│   AuthRepository │ ProductRepository │ OrderRepository      │
│                                                              │
│   • Single source of truth                                   │
│   • Handles API calls                                        │
│   • Returns Result<T> (Success/Error)                       │
└─────────────────────────────────────────────────────────────┘
                    │                    ▲
         API Calls  │                    │  Response
                    ▼                    │
┌─────────────────────────────────────────────────────────────┐
│                     DATA LAYER                               │
│                                                              │
│   ┌─────────────────┐      ┌─────────────────────────┐     │
│   │   ApiService    │      │     TokenManager        │     │
│   │   (Retrofit)    │      │     (DataStore)         │     │
│   │                 │      │                         │     │
│   │   Remote API    │      │   Local Storage         │     │
│   └─────────────────┘      └─────────────────────────┘     │
└─────────────────────────────────────────────────────────────┘
```

---

## 📁 Documentation Files

| File | Scenario |
|------|----------|
| [01_REGISTER.md](./01_REGISTER.md) | User Registration Flow |
| [02_LOGIN.md](./02_LOGIN.md) | User Login Flow |
| [03_BROWSE_PRODUCTS.md](./03_BROWSE_PRODUCTS.md) | Browse & Search Products |
| [04_ADD_TO_CART.md](./04_ADD_TO_CART.md) | Add Product to Cart |
| [05_CHECKOUT.md](./05_CHECKOUT.md) | Checkout & Place Order |
| [06_VIEW_ORDERS.md](./06_VIEW_ORDERS.md) | View Order History |
| [07_ADMIN_MANAGE_ORDERS.md](./07_ADMIN_MANAGE_ORDERS.md) | Admin Order Management |
| [08_PROFILE_UPDATE.md](./08_PROFILE_UPDATE.md) | Update User Profile |
| [09_LOGOUT.md](./09_LOGOUT.md) | User Logout Flow |

---

## 🔑 Key Concepts

### StateFlow Pattern
```kotlin
// ViewModel
private val _uiState = MutableStateFlow(UiState())
val uiState: StateFlow<UiState> = _uiState.asStateFlow()

// View
val state by viewModel.uiState.collectAsState()
```

### Result Sealed Class
```kotlin
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}
```

---

## 📱 App Screens

| Screen | Purpose | ViewModel |
|--------|---------|-----------|
| SignInScreen | User login | AuthViewModel |
| SignUpScreen | User registration | AuthViewModel |
| HomeScreen | Browse products | ProductViewModel |
| CartScreen | View/edit cart | CartViewModel |
| CheckoutScreen | Place order | CartViewModel |
| OrdersScreen | Order history | OrderViewModel |
| ProfileScreen | User profile | AuthViewModel |
| AdminOrdersScreen | Manage orders | OrderViewModel |
| ProductsManagementScreen | CRUD products | ProductViewModel |

---

*SetAside Android - University Project Documentation*
