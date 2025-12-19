# Scenario: Update User Profile

**Path:** `View (ProfileScreen)` → `ViewModel (AuthViewModel)` → `Repository (AuthRepository)` → `API (Retrofit)` & `Local (DataStore)`

## 📊 Flow Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                        UPDATE PROFILE FLOW                                    │
└──────────────────────────────────────────────────────────────────────────────┘

┌─────────┐      ┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│  USER   │      │    VIEW      │      │  VIEWMODEL   │      │  REPOSITORY  │
│         │      │ ProfileScreen│      │ AuthViewModel│      │AuthRepository│
└────┬────┘      └──────┬───────┘      └──────┬───────┘      └──────┬───────┘
     │                  │                     │                     │
     │ 1. Edit Name     │                     │                     │
     │    or Phone      │                     │                     │
     │─────────────────>│                     │                     │
     │                  │ 2. onUpdateProfile  │                     │
     │                  │    (name, phone)    │                     │
     │                  │────────────────────>│                     │
     │                  │                     │ 3. PATCH            │
     │                  │                     │ /users/me           │
     │                  │                     │────────────────────>│
     │                  │                     │                     │
     │                  │                     │                     │ 4. Update DB
     │                  │                     │                     │ (Server)
     │                  │                     │                     │
     │                  │                     │<────────────────────│
     │                  │                     │ 5. Result.Success   │
     │                  │                     │    (Updated User)   │
     │                  │                     │                     │
     │                  │                     │ 6. Update local     │
     │                  │                     │    DataStore        │
     │                  │                     │    (TokenManager)   │
     │                  │                     │                     │
     │                  │              ┌──────┴──────┐              │
     │                  │              │ 7. Update   │              │
     │                  │              │ State:      │              │
     │                  │              │ Success=True│              │
     │                  │              └──────┬──────┘              │
     │                  │                     │                     │
     │<─────────────────│<────────────────────│                     │
     │ 8. Show Success  │                     │                     │
     │    Message       │                     │                     │
```

---

## 📝 User Action

**User changes their phone number in Profile screen and taps "Save Changes"**

- **Input**: New phone number
- **Result**: Profile updated on server and locally

---

## 🔄 Code Flow

### Step 1: VIEW LAYER - `ProfileScreen.kt`

**File**: `ui/screens/profile/ProfileScreen.kt`

```kotlin
@Composable
fun ProfileScreen(
    uiState: AuthUiState,
    onUpdateProfile: (String, String) -> Unit,
    ...
) {
    var phone by remember { mutableStateOf(uiState.userPhone) }

    // Success Snackbar/Dialog
    if (uiState.profileUpdateSuccess) {
        // Show success message
    }

    TextField(value = phone, onValueChange = { phone = it })

    Button(onClick = { onUpdateProfile(name, phone) }) {
        Text("Save Changes")
    }
}
```

---

### Step 2: VIEWMODEL - Update Logic

**File**: `ui/viewmodel/AuthViewModel.kt`

```kotlin
fun updateProfile(fullName: String?, phone: String?) {
    viewModelScope.launch {
        _uiState.update { it.copy(isUpdatingProfile = true) }
        
        when (val result = authRepository.updateProfile(fullName, phone)) {
            is Result.Success -> {
                _uiState.update {
                    it.copy(
                        isUpdatingProfile = false,
                        userName = fullName ?: it.userName,
                        userPhone = phone ?: it.userPhone,
                        profileUpdateSuccess = true
                    )
                }
            }
            is Result.Error -> { ... }
        }
    }
}
```

---

### Step 3: REPOSITORY - Persistence

**File**: `data/repository/AuthRepository.kt`

```kotlin
suspend fun updateProfile(fullName: String?, phone: String?): Result<User> {
    val response = apiService.updateMyProfile(UpdateProfileRequest(fullName, phone))
    
    if (response.isSuccessful) {
        val user = response.body()!!
        
        // CRITICAL: Update local storage too!
        tokenManager.updateUserInfo(name = fullName, phone = phone)
        
        Result.Success(user)
    } else {
        Result.Error(...)
    }
}
```

**File**: `data/local/TokenManager.kt`
```kotlin
suspend fun updateUserInfo(name: String? = null, phone: String? = null) {
    context.dataStore.edit { preferences ->
        name?.let { preferences[USER_NAME_KEY] = it }
        phone?.let { preferences[USER_PHONE_KEY] = it }
    }
}
```

---

## 🎯 Key Points for Exam

1. **Synchronization**: When updating profile, we must update BOTH the server (via API) and the local persistence (DataStore). If we only updated the server, the app would show old data next time it restarts.
2. **PATCH Request**: Used for partial updates.

---

## 🗣️ How to Explain This

> "When updating the profile, the app sends a PATCH request to /users/me. The critical part here is data synchronization. Upon a successful API response, the AuthRepository doesn't just return the data; it also updates the local TokenManager (DataStore). This ensures that if the user restarts the app, their new name or phone number is loaded correctly from local storage without needing a network call."

