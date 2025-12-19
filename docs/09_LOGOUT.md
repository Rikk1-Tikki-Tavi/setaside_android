# Scenario: Logout

**Path:** `View (ProfileScreen)` → `ViewModel (AuthViewModel)` → `Repository (AuthRepository)` → `Local (DataStore)`

## 📊 Flow Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                              LOGOUT FLOW                                      │
└──────────────────────────────────────────────────────────────────────────────┘

┌─────────┐      ┌──────────────┐      ┌──────────────┐      ┌──────────────┐
│  USER   │      │    VIEW      │      │  VIEWMODEL   │      │  REPOSITORY  │
│         │      │ ProfileScreen│      │ AuthViewModel│      │AuthRepository│
└────┬────┘      └──────┬───────┘      └──────┬───────┘      └──────┬───────┘
     │                  │                     │                     │
     │ 1. Tap "Logout"  │                     │                     │
     │─────────────────>│                     │                     │
     │                  │ 2. onLogout()       │                     │
     │                  │────────────────────>│                     │
     │                  │                     │                     │
     │                  │                     │ 3. logout()         │
     │                  │                     │────────────────────>│
     │                  │                     │                     │
     │                  │                     │              ┌──────┴──────┐
     │                  │                     │              │ 4. Clear    │
     │                  │                     │              │ DataStore   │
     │                  │                     │              │ (Remove all │
     │                  │                     │              │ tokens)     │
     │                  │                     │              └──────┬──────┘
     │                  │                     │                     │
     │                  │              ┌──────┴──────┐              │
     │                  │              │ 5. Reset    │              │
     │                  │              │ UiState to  │              │
     │                  │              │ Default     │              │
     │                  │              └──────┬──────┘              │
     │                  │                     │                     │
     │<─────────────────│<────────────────────│                     │
     │ 6. Navigate to   │                     │                     │
     │    SignInScreen  │                     │                     │
     │    (Clear stack) │                     │                     │
```

---

## 📝 User Action

**User taps "Logout" button in Profile screen**

- **Result**: User session cleared, redirected to Login screen

---

## 🔄 Code Flow

### Step 1: VIEW LAYER - Event Trigger

**File**: `ui/screens/profile/ProfileScreen.kt`

```kotlin
Button(
    onClick = onLogout,
    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
) {
    Text("Logout")
}
```

---

### Step 2: MAIN ACTIVITY - Navigation Logic

**File**: `MainActivity.kt`

```kotlin
ProfileScreen(
    onLogout = {
        authViewModel.logout()  // 1. Clear data
        
        // 2. Navigate away
        navController.navigate("signin") {
            popUpTo(0) { inclusive = true }  // 3. Clear entire backstack
        }
    }
)
```

---

### Step 3: VIEWMODEL - State & Data Clearing

**File**: `ui/viewmodel/AuthViewModel.kt`

```kotlin
fun logout() {
    viewModelScope.launch {
        authRepository.logout()  // Clear persistent data
        
        // Reset state to empty default
        _uiState.update {
            AuthUiState()
        }
    }
}
```

---

### Step 4: REPOSITORY - DataStore Cleanup

**File**: `data/repository/AuthRepository.kt`

```kotlin
suspend fun logout() {
    tokenManager.clearAuthData()
}
```

**File**: `data/local/TokenManager.kt`
```kotlin
suspend fun clearAuthData() {
    context.dataStore.edit { preferences ->
        preferences.remove(ACCESS_TOKEN_KEY)
        preferences.remove(USER_ID_KEY)
        preferences.remove(USER_EMAIL_KEY)
        ...
    }
}
```

---

## 🎯 Key Points for Exam

1. **Security**: Logout must simply remove the token from the client. Since we use JWT (stateless auth), we don't necessarily *need* to call a server logout endpoint, though some implementations might invalidate the token on blacklists. In this app, we just delete the local token.
2. **Back Stack Clearing**: `popUpTo(0)` is crucial. It removes all activities/fragments from the stack so the user cannot press the "Back" button to return to the authenticated portion of the app.
3. **State Reset**: We reset `AuthUiState` to its default values to ensure no old user data remains in memory.

---

## 🗣️ How to Explain This

> "Logout is a local operation in this architecture. When the user taps logout, the AuthViewModel calls the repository to clear the DataStore. This physically removes the JWT access token and user info from the device. Simultaneously, the ViewModel resets its state to the default empty state. Finally, the MainActivity navigates the user back to the Sign-In screen and uses popUpTo(0) to wipe the navigation history, ensuring the user cannot navigate back to the app without logging in again."

