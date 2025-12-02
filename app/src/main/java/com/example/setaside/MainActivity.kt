package com.example.setaside

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.setaside.data.local.TokenManager
import com.example.setaside.data.remote.RetrofitClient
import com.example.setaside.data.repository.AuthRepository
import com.example.setaside.data.repository.OrderRepository
import com.example.setaside.data.repository.ProductRepository
import com.example.setaside.ui.screens.admin.AdminOrdersScreen
import com.example.setaside.ui.screens.admin.ProductsManagementScreen
import com.example.setaside.ui.screens.auth.SignInScreen
import com.example.setaside.ui.screens.auth.SignUpScreen
import com.example.setaside.ui.screens.cart.CartScreen
import com.example.setaside.ui.screens.cart.CheckoutScreen
import com.example.setaside.ui.screens.home.HomeScreen
import com.example.setaside.ui.screens.home.ProductDetailDialog
import com.example.setaside.ui.screens.orders.OrderDetailScreen
import com.example.setaside.ui.screens.orders.OrdersScreen
import com.example.setaside.ui.screens.profile.ProfileScreen
import com.example.setaside.ui.theme.SetAsideTheme
import com.example.setaside.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize RetrofitClient first
        RetrofitClient.init(applicationContext)

        // Initialize repositories
        val tokenManager = TokenManager(applicationContext)
        val apiService = RetrofitClient.apiService
        val authRepository = AuthRepository(apiService, tokenManager)
        val productRepository = ProductRepository(apiService)
        val orderRepository = OrderRepository(apiService)

        setContent {
            SetAsideTheme {
                val navController = rememberNavController()

                // ViewModels
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModel.Factory(authRepository)
                )
                val productViewModel: ProductViewModel = viewModel(
                    factory = ProductViewModel.Factory(productRepository)
                )
                val cartViewModel: CartViewModel = viewModel(
                    factory = CartViewModel.Factory(orderRepository)
                )
                val orderViewModel: OrderViewModel = viewModel(
                    factory = OrderViewModel.Factory(orderRepository)
                )

                // States
                val authState by authViewModel.uiState.collectAsState()
                val productsState by productViewModel.uiState.collectAsState()
                val cartState by cartViewModel.uiState.collectAsState()
                val ordersState by orderViewModel.uiState.collectAsState()

                var selectedTab by remember { mutableIntStateOf(0) }

                // Sync selectedTab with current route
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                LaunchedEffect(currentBackStackEntry?.destination?.route) {
                    selectedTab = when (currentBackStackEntry?.destination?.route) {
                        "home", "admin_home" -> 0
                        "orders", "admin_products" -> 1
                        "profile", "admin_profile" -> 2
                        else -> selectedTab
                    }
                }

                // Determine start destination based on login status and role
                val isAdmin = authState.isAdmin
                val startDestination = when {
                    !authState.isLoggedIn -> "signin"
                    isAdmin -> "admin_home"
                    else -> "home"
                }

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    // Auth Screens
                    composable("signin") {
                        SignInScreen(
                            uiState = authState,
                            onLogin = { email, password ->
                                authViewModel.login(email, password)
                            },
                            onNavigateToSignUp = { navController.navigate("signup") },
                            onClearError = { authViewModel.clearError() }
                        )

                        // Navigate to home when logged in
                        LaunchedEffect(authState.isLoggedIn) {
                            if (authState.isLoggedIn) {
                                val destination = if (authState.isAdmin) "admin_home" else "home"
                                navController.navigate(destination) {
                                    popUpTo("signin") { inclusive = true }
                                }
                            }
                        }
                    }

                    composable("signup") {
                        SignUpScreen(
                            uiState = authState,
                            onRegister = { email, password, fullName, phone ->
                                authViewModel.register(email, password, fullName, phone)
                            },
                            onNavigateToSignIn = { navController.popBackStack() },
                            onClearError = { authViewModel.clearError() }
                        )

                        // Navigate to home when logged in
                        LaunchedEffect(authState.isLoggedIn) {
                            if (authState.isLoggedIn) {
                                val destination = if (authState.isAdmin) "admin_home" else "home"
                                navController.navigate(destination) {
                                    popUpTo("signup") { inclusive = true }
                                }
                            }
                        }
                    }

                    // Home Screen
                    composable("home") {
                        HomeScreen(
                            userName = authState.userName,
                            productsUiState = productsState,
                            cartUiState = cartState,
                            onProductClick = { product ->
                                productViewModel.selectProduct(product)
                            },
                            onAddToCart = { product ->
                                cartViewModel.addToCart(product)
                            },
                            onCategoryClick = { category ->
                                productViewModel.setSelectedCategory(category)
                            },
                            onSearchChange = { query ->
                                productViewModel.setSearchQuery(query)
                            },
                            onCartClick = { navController.navigate("cart") },
//                            onBuyNow = { p, quantity, instructions ->
//                                cartViewModel.addToCart(p, quantity, instructions)
//                                navController.navigate("checkout")
//                            },
                            onBuyNow = { product, quantity, instructions ->
                                cartViewModel.addToCart(product, quantity, instructions)
                                navController.navigate("checkout")
                            },
                            onOrdersClick = { navController.navigate("orders") },
                            onProfileClick = { navController.navigate("profile") },
                            onRefresh = { productViewModel.loadProducts() },
                            selectedTab = selectedTab,
                            onTabSelected = { tab ->
                                // Only update tab state, navigation handled by dedicated callbacks
                                if (tab == 0) selectedTab = tab
                            }
                        )

                        // Product detail dialog
                        productsState.selectedProduct?.let { product ->
                            ProductDetailDialog(
                                product = product,
                                onDismiss = { productViewModel.selectProduct(null) },
                                onAddToCart = { quantity, specialInstructions ->
                                    cartViewModel.addToCart(product, quantity, specialInstructions)
                                },
                                onBuyNow = { product, quantity, instructions ->
                                    cartViewModel.addToCart(product, quantity, instructions)
                                    navController.navigate("checkout")
                                },
                                )
                        }
                    }

                    // Cart Screen
                    composable("cart") {
                        CartScreen(
                            uiState = cartState,
                            onNavigateBack = { navController.popBackStack() },
                            onCheckout = { navController.navigate("checkout") },
                            onUpdateQuantity = { productId, quantity ->
                                cartViewModel.updateQuantity(productId, quantity)
                            },
                            onRemoveItem = { productId ->
                                cartViewModel.removeFromCart(productId)
                            },
                            onClearError = { cartViewModel.clearError() }
                        )
                    }

                    // Checkout Screen
                    composable("checkout") {
                        CheckoutScreen(
                            uiState = cartState,
                            onNavigateBack = { navController.popBackStack() },
                            onPlaceOrder = { notes ->
                                cartViewModel.checkout(notes)
                            },
                            onClearError = { cartViewModel.clearError() },
                            onViewOrder = {
                                cartState.lastOrderId?.let { orderId ->
                                    cartViewModel.resetCheckoutState()
                                    navController.navigate("order/$orderId") {
                                        popUpTo("home")
                                    }
                                }
                            },
                            onContinueShopping = {
                                cartViewModel.resetCheckoutState()
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }

                    // Orders Screen
                    composable("orders") {
                        OrdersScreen(
                            uiState = ordersState,
                            onNavigateBack = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onOrderClick = { order ->
                                navController.navigate("order/${order.id}")
                            },
                            onRefresh = { orderViewModel.loadOrders() },
                            onFilterChange = { status ->
                                orderViewModel.setFilterStatus(status)
                            },
                            selectedTab = selectedTab,
                            onTabSelected = { _: Int -> },
                            onHomeClick = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onProfileClick = { navController.navigate("profile") }
                        )

                        LaunchedEffect(Unit) {
                            orderViewModel.loadOrders()
                        }
                    }

                    // Order Detail Screen
                    composable(
                        route = "order/{orderId}",
                        arguments = listOf(navArgument("orderId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
                        OrderDetailScreen(
                            uiState = ordersState,
                            orderId = orderId,
                            onNavigateBack = { navController.popBackStack() },
                            onLoadOrder = { orderViewModel.loadOrderDetails(it) }
                        )
                    }

                    // Profile Screen
                    composable("profile") {
                        ProfileScreen(
                            uiState = authState,
                            isAdmin = false,
                            onNavigateBack = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate("signin") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onOrdersClick = { navController.navigate("orders") },
                            onUpdateProfile = { fullName, phone ->
                                authViewModel.updateProfile(fullName, phone)
                            },
                            onClearProfileUpdateSuccess = { authViewModel.clearProfileUpdateSuccess() },
                            selectedTab = selectedTab,
                            onTabSelected = { _: Int -> },
                            onHomeClick = {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        )
                    }

                    // Admin Home Screen (Orders)
                    composable("admin_home") {
                        AdminOrdersScreen(
                            uiState = ordersState,
                            onOrderClick = { order ->
                                navController.navigate("order/${order.id}")
                            },
                            onRefresh = { orderViewModel.loadOrders() },
                            onFilterChange = { status ->
                                orderViewModel.setFilterStatus(status)
                            },
                            onUpdateStatus = { orderId, newStatus ->
                                orderViewModel.updateOrderStatus(orderId, newStatus)
                            },
                            onDismissCompletionModal = { orderViewModel.dismissCompletionModal() },
                            selectedTab = selectedTab,
                            onTabSelected = { tab ->
                                // Only update tab state for current screen, navigation handled by dedicated callbacks
                                if (tab == 0) selectedTab = tab
                            },
                            onProductsClick = { navController.navigate("admin_products") },
                            onProfileClick = { navController.navigate("admin_profile") }
                        )

                        LaunchedEffect(Unit) {
                            orderViewModel.loadOrders()
                        }
                    }

                    // Admin Products Screen
                    composable("admin_products") {
                        LaunchedEffect(Unit) {
                            productViewModel.loadAllProducts()
                        }
                        ProductsManagementScreen(
                            uiState = productsState,
                            onRefresh = { productViewModel.loadAllProducts() },
                            onCreateProduct = { name, description, price, category, isAvailable, stockQuantity, imageUrl ->
                                productViewModel.createProduct(name, description, price, category, isAvailable, stockQuantity, imageUrl)
                            },
                            onUpdateProduct = { id, name, description, price, category, isAvailable, stockQuantity, imageUrl ->
                                productViewModel.updateProduct(id, name, description, price, category, isAvailable, stockQuantity, imageUrl)
                            },
                            onDeleteProduct = { id ->
                                productViewModel.deleteProduct(id)
                            },
                            selectedTab = 1,
                            onTabSelected = { _: Int -> },
                            onHomeClick = {
                                navController.navigate("admin_home") {
                                    popUpTo("admin_home") { inclusive = true }
                                }
                            },
                            onProfileClick = { navController.navigate("admin_profile") }
                        )
                    }

                    // Admin Profile Screen
                    composable("admin_profile") {
                        ProfileScreen(
                            uiState = authState,
                            isAdmin = true,
                            onNavigateBack = { navController.popBackStack() },
                            onLogout = {
                                authViewModel.logout()
                                navController.navigate("signin") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onOrdersClick = { /* Not needed for admin */ },
                            onUpdateProfile = { fullName, phone ->
                                authViewModel.updateProfile(fullName, phone)
                            },
                            onClearProfileUpdateSuccess = { authViewModel.clearProfileUpdateSuccess() },
                            selectedTab = selectedTab,
                            onTabSelected = { _: Int -> },
                            onHomeClick = {
                                navController.navigate("admin_home") {
                                    popUpTo("admin_home") { inclusive = true }
                                }
                            },
                            onProductsClick = {
                                navController.navigate("admin_products") {
                                    popUpTo("admin_home") { inclusive = false }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
