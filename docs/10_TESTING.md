# Testing Strategy & Implementation

## 🛡️ Testing Overview

For the oral exam, you should be able to explain **Unit Testing**. Specifically, we focus on testing the **ViewModel** because it contains the business logic.

We use **JUnit** for tests and **Mockk** for mocking dependencies (like Repositories).

---

## 🧪 What to Test?

| Component | What we test | How? |
|-----------|--------------|------|
| **ViewModel** | State updates, Business logic | **UNIT TEST** (Main focus) |
| **Repository** | API Mapping, Error handling | Unit Test |
| **UI (Compose)** | Visuals, Navigation | UI Test (Espresso/ComposeTest) |

---

## 📝 Example: Testing AuthViewModel

We want to verify that when `login()` is called:
1. `isLoading` becomes true.
2. Repository `login()` is called.
3. `isLoggedIn` becomes true on success.

### Test Class Structure

```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    // 1. Rule to allow Coroutines in tests
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // 2. Mocks
    private val authRepository = mockk<AuthRepository>()
    
    // 3. Subject under test
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        viewModel = AuthViewModel(authRepository)
    }

    @Test
    fun `login success updates state to logged in`() = runTest {
        // GIVEN
        val email = "test@example.com"
        val password = "password"
        val mockUser = User(id = "1", email = email, fullName = "Test User")
        
        // Mock the repository response
        coEvery { 
            authRepository.login(email, password) 
        } returns Result.Success(mockUser)

        // WHEN
        viewModel.login(email, password)

        // THEN
        // Verify state flow updates
        val state = viewModel.uiState.value
        
        assertEquals(false, state.isLoading)
        assertEquals(true, state.isLoggedIn)
        assertEquals("Test User", state.userName)
        assertEquals(null, state.error)
    }
    
    @Test
    fun `login error updates state with error message`() = runTest {
        // GIVEN
        coEvery { 
            authRepository.login(any(), any()) 
        } returns Result.Error("Invalid credentials")

        // WHEN
        viewModel.login("wrong", "pass")

        // THEN
        val state = viewModel.uiState.value
        
        assertEquals(false, state.isLoading)
        assertEquals(false, state.isLoggedIn)
        assertEquals("Invalid credentials", state.error)
    }
}
```

---

## 🧩 Key Concepts for Exam

### 1. Mocking (`mockk`)
We don't want to make real API calls during tests. We "mock" the repository to return fake data immediately.
- `coEvery { ... } returns ...` : Defines checking logic for Coroutines (suspend functions).

### 2. Coroutines in Tests (`runTest`)
Coroutines run asynchronously. In tests, we need them to run synchronously so we can check results immediately.
- `runTest`: A special coroutine builder for tests that skips delays.
- `MainDispatcherRule`: A helper to swap the implementation of `Dispatchers.Main` with a test dispatcher.

### 3. Assertions (`assertEquals`)
We verify the outcome matches our expectations.
- `assertEquals(expected, actual)`

---

## 🗣️ How to Explain This

> "For testing, I focused on Unit Testing the ViewModels using JUnit and Mockk. Since ViewModels use Coroutines, I used `runTest` to execute them synchronously in the test environment. I mocked the repository layer so that I could isolate the ViewModel logic and simulate success or error responses from the API without actually hitting the network. For example, in the AuthViewModel test, I verified that a successful login response correctly updates the UI state's isLoggedIn property to true."

