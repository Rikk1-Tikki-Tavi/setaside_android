# 🎓 Oral Exam Q&A Preparation

This document consolidates common questions you might face during the oral exam regarding the SetAside MVVM architecture.

---

## 🏛️ Architecture Questions

### Q1: Why did you choose MVVM over MVC or MVP?
**Answer:** 
> "I chose MVVM (Model-View-ViewModel) because it provides the best separation of concerns for modern Android development, especially with Jetpack Compose.
> 1.  **State Management**: MVVM works perfectly with Compose's reactive UI. The ViewModel exposes a `StateFlow` that the UI observes.
> 2.  **Lifecycle Awareness**: ViewModels survive configuration changes (like screen rotation), preserving data that would be lost in MVC.
> 3.  **Testability**: Since logic is in the ViewModel (decoupled from Android views), it's easy to unit test."

### Q2: Explain the Data Flow in your app.
**Answer:**
> "It follows **Unidirectional Data Flow (UDF)**:
> 1.  **Events** go UP: User clicks a button in the **View**, calling a function in the **ViewModel**.
> 2.  **Logic**: ViewModel handles the event, calling the **Repository**.
> 3.  **State** goes DOWN: The ViewModel updates its `StateFlow`.
> 4.  **UI Updates**: The View observes the state and recomposes automatically."

---

## 🛠️ Technical Implementation Questions

### Q3: What is the role of the Repository?
**Answer:**
> "The Repository acts as the **Single Source of Truth**. It abstracts the data sources from the rest of the app. The ViewModel doesn't care if data comes from the API (Retrofit) or local storage (DataStore); it simply asks the Repository. This allows me to easily swap data sources or add caching without changing the ViewModel code."

### Q4: How do you handle Asynchronous operations?
**Answer:**
> "I use **Kotlin Coroutines**. In the ViewModel, I launch a `viewModelScope`, which automatically cancels operations if the ViewModel is cleared (preventing memory leaks). For the API calls, I use `suspend` functions in Retrofit, which suspends execution without blocking the main thread until the network result returns."

### Q5: How is the Shopping Cart implemented?
**Answer:**
> "The Shopping Cart is currently implemented as **local state** within the `CartViewModel`. When a user adds an item, it's stored in a list in memory. I use the `copy()` method to create a new immutable list for StateFlow updates. The cart data is only sent to the server when the user completes the 'Checkout' process, where I transform the local cart items into the `CreateOrderRequest` format required by the API."

### Q6: How do you handle Token Authentication?
**Answer:**
> "I use **JWT (JSON Web Tokens)**.
> 1.  **Login**: The server returns a token.
> 2.  **Storage**: I save it securely in **DataStore** (TokenManager).
> 3.  **Usage**: An OkHttp Interceptor adds `Authorization: Bearer <token>` to every subsequent API request header.
> 4.  **Logout**: I simply delete the token from DataStore."

### Q7: Why use Sealed Classes for Result?
**Answer:**
> "A **Sealed Class** (`Result<T>`) allows me to model the state of a network call exhaustively. I have specific subclasses for `Success`, `Error`, and `Loading`. This forces the compiler to ensure I handle all cases (like showing an error dialog or a loading spinner) in my ViewModel, making the code more robust."

---

## 🐛 Troubleshooting Questions

### Q8: What happens if the network fails?
**Answer:**
> "The Repository catches the exception and returns `Result.Error`. The ViewModel sees this, sets `isLoading = false`, and updates the `error` property in the UI state. The View then displays an `ErrorDialog` or a Snackbar with the error message so the user knows what happened."

### Q9: How do you persist data locally?
**Answer:**
> "I use **Jetpack DataStore** to persist the user's session (Authentication Token, Name, Role). This is lighter and more modern than SharedPreferences. For the cart, I chose transient state (memory) for this version, but I could easily extend the Repository to save it to a Room database if offline persistence was required."

---

## 📱 Platform Specifics

### Q10: What is Recomposition in Jetpack Compose?
**Answer:**
> "Recomposition is how Compose updates the UI. It's the process of re-running the composable functions when their state changes. Because I use `collectAsState()` with my ViewModel's StateFlow, any change in data automatically triggers recomposition of only the UI elements that depend on that data."

