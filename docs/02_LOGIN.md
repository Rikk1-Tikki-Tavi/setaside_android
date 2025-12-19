# Scenario: User Login

**Path:** `View (SignInScreen)` → `ViewModel (AuthViewModel)` → `Repository (AuthRepository)` → `API (Retrofit)` → `Local (DataStore)`

## 📊 Flow Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                              USER LOGIN FLOW                                  │
└──────────────────────────────────────────────────────────────────────────────┘

┌─────────┐      ┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│  USER   │      │    VIEW      │      │  VIEWMODEL   │      │  REPOSITORY  │
│         │      │ SignInScreen │      │ AuthViewModel│      │AuthRepository│
└────┬────┘      └──────┬───────┘      └──────┬───────┘      └──────┬───────┘
     │                  │                     │                     │
     │ 1. Enter email   │                     │                     │
     │    & password    │                     │                     │
     │─────────────────>│                     │                     │
     │                  │                     │                     │
     │ 2. Tap "Sign In" │                     │                     │
     │─────────────────>│                     │                     │
     │                  │                     │                     │
     │                  │ 3. onLogin(email,   │                     │
     │                  │    password)        │                     │
     │                  │────────────────────>│                     │
     │                  │                     │                     │
     │                  │              ┌──────┴──────┐              │
     │                  │              │ 4. Update   │              │
     │                  │              │ isLoading   │              │
     │                  │              │ = true      │              │
     │                  │              └──────┬──────┘              │
     │                  │                     │                     │
     │                  │                     │ 5. login(email,     │
     │                  │                     │    password)        │
     │                  │                     │────────────────────>│
     │                  │                     │                     │
     │                  │                     │              ┌──────┴──────┐
     │                  │                     │              │ 6. API POST │
     │                  │                     │              │/auth/login  │
     │                  │                     │              └──────┬──────┘
     │                  │                     │                     │
     │                  │                     │              ┌──────┴──────┐
     │                  │                     │              │ 7. Save     │
     │                  │                     │              │ Token to    │
     │                  │                     │              │ DataStore   │
     │                  │                     │              └──────┬──────┘
     │                  │                     │                     │
     │                  │                     │<────────────────────│
     │                  │                     │ 8. Result.Success   │
     │                  │                     │    (User)           │
     │                  │                     │                     │
     │                  │              ┌──────┴──────┐              │
     │                  │              │ 9. Update:  │              │
     │                  │              │ isLoggedIn  │              │
     │                  │              │ = true      │              │
     │                  │              │ userName    │              │
     │                  │              │ isAdmin     │              │
     │                  │              └──────┬──────┘              │
     │                  │                     │                     │
     │                  │<────────────────────│                     │
     │                  │ 10. StateFlow emits │                     │
     │                  │     new state       │                     │
     │                  │                     │                     │
     │<─────────────────│                     │                     │
     │ 11. Navigate to  │                     │                     │
     │  home/admin_home │                     │                     │
     │                  │                     │                     │
     ▼                  ▼                     ▼                     ▼
```

---

## 📝 User Action

**User enters email and password, then taps "Sign In" button**

- **Input**: Email, Password
- **Expected Result**: Authenticate user, navigate to Home (or Admin Home)

---

## 🔄 Code Flow

### Step 1: VIEW LAYER - `SignInScreen.kt`

**File**: `ui/screens/auth/SignInScreen.kt`

```kotlin
@Composable
fun SignInScreen(
    uiState: AuthUiState,
    onLogin: (String, String) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onClearError: () -> Unit
) {
    // Local state for input fields
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Error dialog
    if (uiState.error != null) {
        ErrorDialog(message = uiState.error, onDismiss = onClearError)
    }

    Column(...) {
        // Email input
        InputField(
            label = "Email",
            value = email,
            onValueChange = { email = it },
            enabled = !uiState.isLoading
        )
        
        // Password input
        InputField(
            label = "Password",
            value = password,
            onValueChange = { password = it },
            isPassword = true,
            enabled = !uiState.isLoading
        )
        
        // Sign In button
        LoadingButton(
            text = "Sign In",
            onClick = { onLogin(email, password) },
            isLoading = uiState.isLoading,
            enabled = email.isNotBlank() && password.isNotBlank()
        )
    }
}
```

**Key Points:**
- Input fields disabled when `isLoading = true` (prevents duplicate submissions)
- Button shows loading spinner during API call
- Error dialog auto-shows when `uiState.error` is not null

---

### Step 2: MAIN ACTIVITY - Navigation & Routing

**File**: `MainActivity.kt`

```kotlin
composable("signin") {
    SignInScreen(
        uiState = authState,
        onLogin = { email, password ->
            authViewModel.login(email, password)  // Call ViewModel
        },
        onNavigateToSignUp = { navController.navigate("signup") },
        onClearError = { authViewModel.clearError() }
    )

    // Navigate when logged in
    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) {
            val destination = if (authState.isAdmin) "admin_home" else "home"
            navController.navigate(destination) {
                popUpTo("signin") { inclusive = true }  // Remove signin from backstack
            }
        }
    }
}
```

**Key Points:**
- `LaunchedEffect` watches `authState.isLoggedIn`
- Navigates to different screens based on user role
- `popUpTo` removes login screen from back stack (can't go back after login)

---

### Step 3: VIEWMODEL LAYER - `AuthViewModel.kt`

**File**: `ui/viewmodel/AuthViewModel.kt`

```kotlin
class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            // Step 1: Set loading state
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            // Step 2: Call repository
            when (val result = authRepository.login(email, password)) {
                is Result.Success -> {
                    // Step 3: Update state on success
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
                    // Step 4: Update state on error
                    _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
                is Result.Loading -> {}
            }
        }
    }
}
```

**Key Points:**
- Uses `viewModelScope` for coroutine lifecycle management
- `_uiState.update { }` is atomic state update
- `isAdmin` check includes both "admin" and "cashier" roles

---

### Step 4: REPOSITORY LAYER - `AuthRepository.kt`

**File**: `data/repository/AuthRepository.kt`

```kotlin
suspend fun login(email: String, password: String): Result<User> {
    return try {
        // 1. Make login API call
        val response = apiService.login(LoginRequest(email, password))
        
        if (response.isSuccessful) {
            val authResponse = response.body()!!
            
            // 2. Save token FIRST (needed for next API call)
            tokenManager.saveAccessToken(authResponse.accessToken)
            
            // 3. Fetch user details (now authenticated)
            val userResponse = apiService.getCurrentUser()
            
            if (userResponse.isSuccessful) {
                val user = userResponse.body()!!
                
                // 4. Save complete auth data
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
            Result.Error("Login failed: ${response.message()}", response.code())
        }
    } catch (e: Exception) {
        Result.Error(e.message ?: "Unknown error")
    }
}
```

**Key Points:**
- Token saved immediately after login response
- Second API call (`getCurrentUser`) uses the saved token
- All user data persisted to DataStore

---

### Step 5: API SERVICE - Retrofit Interface

**File**: `data/remote/ApiService.kt`

```kotlin
interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @GET("auth/me")
    suspend fun getCurrentUser(): Response<User>
}
```

**Request/Response Models:**
```kotlin
data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthResponse(
    @SerializedName("access_token")
    val accessToken: String,
    val user: User? = null
)
```

---

## ❌ Error Scenarios

```
┌────────────────────────────────────────────────────────────────┐
│                      ERROR HANDLING FLOW                        │
└────────────────────────────────────────────────────────────────┘

Wrong Password (401):
    API returns 401 Unauthorized
           │
           ▼
    Repository returns Result.Error("Login failed: Unauthorized")
           │
           ▼
    ViewModel updates: _uiState.update { it.copy(error = "...") }
           │
           ▼
    View shows ErrorDialog with message
           │
           ▼
    User taps "OK" → onClearError() → error = null → Dialog dismissed

Network Error:
    Exception thrown during API call
           │
           ▼
    catch block: Result.Error(e.message)
           │
           ▼
    Same flow as above...
```

---

## 🔐 Token Storage

```
┌────────────────────────────────────────────────────────────────┐
│                     TOKEN PERSISTENCE                           │
└────────────────────────────────────────────────────────────────┘

DataStore Keys:
┌──────────────────┬────────────────────────────────────────────┐
│ Key              │ Value                                       │
├──────────────────┼────────────────────────────────────────────┤
│ access_token     │ "eyJhbGciOiJIUzI1NiIsInR5cCI6..."         │
│ user_id          │ "uuid-1234-5678"                           │
│ user_email       │ "user@example.com"                         │
│ user_name        │ "John Doe"                                 │
│ user_role        │ "customer" | "admin" | "cashier"           │
│ user_phone       │ "+1234567890"                              │
└──────────────────┴────────────────────────────────────────────┘

Token used in all subsequent API calls via AuthInterceptor:
┌────────────────────────────────────────────────────────────────┐
│ Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6...         │
└────────────────────────────────────────────────────────────────┘
```

---

## 🎯 Key Points for Exam

1. **Two API Calls**: Login returns token only, then fetch user details
2. **Token First**: Must save token before calling authenticated endpoints
3. **Role-Based Navigation**: Admin/Cashier → admin_home, Customer → home
4. **StateFlow Observation**: View automatically recomposes on state change
5. **Back Stack Management**: Login screen removed after successful login

---

## 🗣️ How to Explain This

> "The login flow starts when the user enters their credentials and taps Sign In. The SignInScreen calls the onLogin callback which triggers authViewModel.login(). The ViewModel sets isLoading to true, showing a loading spinner, then calls the repository. The repository makes a POST request to /auth/login. If successful, it immediately saves the JWT token to DataStore, then makes a second call to /auth/me to get the complete user profile. All this data is saved locally. The ViewModel then updates the StateFlow with isLoggedIn = true. The View observes this change via collectAsState, and a LaunchedEffect detects the login state change and navigates to the appropriate home screen based on the user's role."

---

*Duration: ~1-2 seconds for API calls*
