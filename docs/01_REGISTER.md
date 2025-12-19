# Scenario: User Registration

**Path:** `View (SignUpScreen)` → `ViewModel (AuthViewModel)` → `Repository (AuthRepository)` → `API (Retrofit)`

## 📊 Flow Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                           USER REGISTRATION FLOW                              │
└──────────────────────────────────────────────────────────────────────────────┘

┌─────────┐      ┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│  USER   │      │    VIEW      │      │  VIEWMODEL   │      │  REPOSITORY  │
│         │      │ SignUpScreen │      │ AuthViewModel│      │AuthRepository│
└────┬────┘      └──────┬───────┘      └──────┬───────┘      └──────┬───────┘
     │                  │                     │                     │
     │ 1. Fill form     │                     │                     │
     │ (email, password,│                     │                     │
     │  name, phone)    │                     │                     │
     │─────────────────>│                     │                     │
     │                  │                     │                     │
     │ 2. Tap "Sign Up" │                     │                     │
     │─────────────────>│                     │                     │
     │                  │                     │                     │
     │                  │ 3. onRegister()     │                     │
     │                  │────────────────────>│                     │
     │                  │                     │                     │
     │                  │                     │ 4. Set isLoading    │
     │                  │                     │    = true           │
     │                  │                     │                     │
     │                  │                     │ 5. register()       │
     │                  │                     │────────────────────>│
     │                  │                     │                     │
     │                  │                     │                     │ 6. POST /auth/register
     │                  │                     │                     │──────────────────────>
     │                  │                     │                     │         API
     │                  │                     │                     │<──────────────────────
     │                  │                     │                     │ 7. AuthResponse
     │                  │                     │                     │    (token + user)
     │                  │                     │                     │
     │                  │                     │                     │ 8. Save to DataStore
     │                  │                     │                     │    (TokenManager)
     │                  │                     │                     │
     │                  │                     │<────────────────────│
     │                  │                     │ 9. Result.Success   │
     │                  │                     │                     │
     │                  │                     │ 10. Update State:   │
     │                  │                     │     isLoggedIn=true │
     │                  │                     │     userName=...    │
     │                  │                     │                     │
     │                  │<────────────────────│                     │
     │                  │ 11. State emitted   │                     │
     │                  │     via StateFlow   │                     │
     │                  │                     │                     │
     │<─────────────────│                     │                     │
     │ 12. Navigate to  │                     │                     │
     │     HomeScreen   │                     │                     │
     │                  │                     │                     │
```

---

## 📝 User Action

**User fills registration form and taps "Sign Up" button**

- **Input Fields**: Email, Password, Full Name, Phone (optional)
- **Expected Result**: Account created, auto-login, navigate to Home

---

## 🔄 Code Flow

### Step 1: VIEW LAYER - `SignUpScreen.kt`

**File**: `ui/screens/auth/SignUpScreen.kt`

```kotlin
@Composable
fun SignUpScreen(
    uiState: AuthUiState,
    onRegister: (String, String, String, String?) -> Unit,  // Callback
    onNavigateToSignIn: () -> Unit,
    onClearError: () -> Unit
) {
    // Local state for form fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    
    // Show error dialog if error exists
    if (uiState.error != null) {
        ErrorDialog(message = uiState.error, onDismiss = onClearError)
    }
    
    // Form fields...
    
    // Sign Up button
    LoadingButton(
        text = "Sign Up",
        onClick = { 
            onRegister(email, password, fullName, phone.ifBlank { null }) 
        },
        isLoading = uiState.isLoading,
        enabled = email.isNotBlank() && password.isNotBlank() && fullName.isNotBlank()
    )
}
```

**What happens here?**
- User types in form fields (stored in local Compose state)
- Button click triggers `onRegister` callback with form data
- Button shows loading spinner when `uiState.isLoading` is true
- Error dialog appears when `uiState.error` is not null

---

### Step 2: MAIN ACTIVITY - Routes to ViewModel

**File**: `MainActivity.kt`

```kotlin
composable("signup") {
    SignUpScreen(
        uiState = authState,
        onRegister = { email, password, fullName, phone ->
            authViewModel.register(email, password, fullName, phone)
        },
        onNavigateToSignIn = { navController.popBackStack() },
        onClearError = { authViewModel.clearError() }
    )
    
    // Auto-navigate when logged in
    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) {
            val destination = if (authState.isAdmin) "admin_home" else "home"
            navController.navigate(destination) {
                popUpTo("signup") { inclusive = true }
            }
        }
    }
}
```

**What happens here?**
- `onRegister` callback calls `authViewModel.register()`
- `LaunchedEffect` observes `authState.isLoggedIn`
- When login succeeds, navigates to appropriate home screen

---

### Step 3: VIEWMODEL LAYER - `AuthViewModel.kt`

**File**: `ui/viewmodel/AuthViewModel.kt`

```kotlin
fun register(email: String, password: String, fullName: String, phone: String?) {
    viewModelScope.launch {
        // 1. Set loading state
        _uiState.update { it.copy(isLoading = true, error = null) }
        
        // 2. Call repository
        when (val result = authRepository.register(email, password, fullName, phone)) {
            is Result.Success -> {
                // 3. Update state with user data
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        user = result.data,
                        userName = result.data.fullName,
                        userEmail = result.data.email,
                        userPhone = result.data.phone ?: "",
                        userRole = result.data.role,
                        isAdmin = result.data.role == "admin" || result.data.role == "cashier",
                        error = null
                    )
                }
            }
            is Result.Error -> {
                // 4. Update state with error
                _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
            }
            is Result.Loading -> {}
        }
    }
}
```

**What happens here?**
1. Sets `isLoading = true` to show loading spinner
2. Calls `authRepository.register()` in a coroutine
3. On success: updates state with user info, sets `isLoggedIn = true`
4. On error: sets `error` message to show in dialog

---

### Step 4: REPOSITORY LAYER - `AuthRepository.kt`

**File**: `data/repository/AuthRepository.kt`

```kotlin
suspend fun register(email: String, password: String, fullName: String, phone: String?): Result<User> {
    return try {
        // 1. Make API call
        val response = apiService.register(RegisterRequest(email, password, fullName, phone))
        
        if (response.isSuccessful) {
            val authResponse = response.body()!!
            
            // 2. Save token first
            tokenManager.saveAccessToken(authResponse.accessToken)
            
            // 3. Fetch complete user info
            val userResponse = apiService.getCurrentUser()
            
            if (userResponse.isSuccessful) {
                val user = userResponse.body()!!
                
                // 4. Save all auth data to DataStore
                tokenManager.saveAuthData(
                    token = authResponse.accessToken,
                    userId = user.id,
                    email = user.email,
                    name = user.fullName,
                    role = user.role,
                    phone = user.phone
                )
                
                Result.Success(user)
            } else {
                Result.Error("Failed to get user info", userResponse.code())
            }
        } else {
            val errorMessage = response.errorBody()?.string()
            Result.Error(errorMessage ?: "Registration failed", response.code())
        }
    } catch (e: Exception) {
        Result.Error(e.message ?: "Unknown error")
    }
}
```

**What happens here?**
1. Makes POST request to `/auth/register`
2. Saves JWT token immediately after successful registration
3. Fetches complete user profile
4. Persists all auth data to DataStore for future app launches

---

### Step 5: DATA LAYER - API & Storage

**File**: `data/remote/ApiService.kt`
```kotlin
@POST("auth/register")
suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

@GET("auth/me")
suspend fun getCurrentUser(): Response<User>
```

**File**: `data/local/TokenManager.kt`
```kotlin
suspend fun saveAuthData(
    token: String,
    userId: String,
    email: String,
    name: String,
    role: String,
    phone: String? = null
) {
    context.dataStore.edit { preferences ->
        preferences[ACCESS_TOKEN_KEY] = token
        preferences[USER_ID_KEY] = userId
        preferences[USER_EMAIL_KEY] = email
        preferences[USER_NAME_KEY] = name
        preferences[USER_ROLE_KEY] = role
        phone?.let { preferences[USER_PHONE_KEY] = it }
    }
}
```

---

## ❌ Error Handling

```
┌─────────────────────────────────────────────────────────┐
│                    ERROR SCENARIOS                       │
└─────────────────────────────────────────────────────────┘

1. Network Error
   └─> Repository catches Exception
       └─> Returns Result.Error("Network error message")
           └─> ViewModel sets uiState.error = message
               └─> View shows ErrorDialog

2. Email Already Exists (400/409)
   └─> API returns error response
       └─> Repository parses errorBody
           └─> Returns Result.Error("Email already registered")
               └─> ViewModel sets uiState.error
                   └─> View shows ErrorDialog

3. Invalid Input (400)
   └─> API validation fails
       └─> Repository returns Result.Error
           └─> ViewModel updates state
               └─> View shows error + clears on dismiss
```

---

## 🎯 Key Points for Exam

1. **Unidirectional Data Flow**: User action → ViewModel → Repository → API → Back up the chain
2. **StateFlow**: Reactive state updates trigger UI recomposition automatically
3. **Token Persistence**: Token saved to DataStore survives app restart
4. **Error Propagation**: Errors wrapped in `Result.Error` and displayed via dialog
5. **Coroutines**: `viewModelScope.launch` for async operations
6. **Form Validation**: Button disabled until required fields are filled

---

## 🗣️ How to Explain This

> "When the user fills the registration form and taps Sign Up, the SignUpScreen composable triggers the onRegister callback. This routes through MainActivity to the AuthViewModel's register function. The ViewModel first sets isLoading to true, then calls the AuthRepository. The repository makes a POST request to the API, and if successful, saves the JWT token and user data to DataStore using TokenManager. The Result is returned to the ViewModel, which updates the StateFlow. Because the View observes this StateFlow with collectAsState(), the UI automatically recomposes. When isLoggedIn becomes true, a LaunchedEffect navigates the user to the home screen."

---

*Duration: API call ~1-3 seconds depending on network*
