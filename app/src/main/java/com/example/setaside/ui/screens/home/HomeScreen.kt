package com.example.setaside.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.setaside.R
import com.example.setaside.data.model.Product
import com.example.setaside.ui.viewmodel.ProductsUiState
import com.example.setaside.ui.viewmodel.CartUiState

@Composable
fun HomeScreen(
    userName: String,
    productsUiState: ProductsUiState,
    cartUiState: CartUiState,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
    onBuyNow: (Product, Int, String?) -> Unit,  // Changed to accept 3 parameters
    onCategoryClick: (String?) -> Unit,
    onSearchChange: (String) -> Unit,
    onCartClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onProfileClick: () -> Unit,
    onRefresh: () -> Unit,
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(bottom = 77.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFF618264))
                    .padding(top = 40.dp, start = 25.dp, end = 25.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Hello,",
                        fontSize = 20.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = userName.ifEmpty { "Guest" },
                        fontSize = 28.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                // Cart icon with badge
                Box(modifier = Modifier.size(38.dp)) {
                    IconButton(
                        onClick = onCartClick,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = Color.Black.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(5.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ShoppingCart,
                            contentDescription = "Cart",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    if (cartUiState.totalItems > 0) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(
                                    color = Color(0xFFFF993B),
                                    shape = RoundedCornerShape(9.dp)
                                )
                                .align(Alignment.TopEnd)
                                .offset(x = 4.dp, y = (-4).dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cartUiState.totalItems.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp, vertical = 15.dp),
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearchChange(it)
                    },
                    placeholder = { Text("Search products...", color = Color.Gray) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = Color.Gray
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFFD0E7D2),
                        focusedBorderColor = Color(0xFF618264),
                        unfocusedContainerColor = Color(0xFFD0E7D2).copy(alpha = 0.3f)
                    ),
                    singleLine = true
                )
            }

            // Categories
            CategorySection(
                categories = productsUiState.categories,
                selectedCategory = productsUiState.selectedCategory,
                onCategoryClick = onCategoryClick
            )

            // Products Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (productsUiState.selectedCategory != null)
                        productsUiState.selectedCategory
                    else "All Products",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                if (productsUiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color(0xFF618264),
                        strokeWidth = 2.dp
                    )
                }
            }

            // Products Grid
            if (productsUiState.products.isEmpty() && !productsUiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Inventory,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = "No products found",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                        TextButton(onClick = onRefresh) {
                            Text("Refresh", color = Color(0xFF618264))
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(horizontal = 25.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(productsUiState.products) { product ->
                        ProductCard(
                            product = product,
                            onClick = { onProductClick(product) },
                            onAddToCart = { onAddToCart(product) },
                            onBuyNow = { onBuyNow(product, 1, null) }  // Pass default values
                        )
                    }
                }
            }
        }

        // Bottom Navigation
        BottomNavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            onOrdersClick = onOrdersClick,
            onProfileClick = onProfileClick
        )
    }
}

@Composable
private fun CategorySection(
    categories: List<String>,
    selectedCategory: String?,
    onCategoryClick: (String?) -> Unit
) {
    // Default categories to show if API doesn't return them
    val defaultCategories = listOf("Vegetables", "Beverages")
    val displayCategories = if (categories.isEmpty()) defaultCategories else categories

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 25.dp)
    ) {
        Text(
            text = "Categories",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // All button
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategoryClick(null) },
                label = { Text("All", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF618264),
                    selectedLabelColor = Color.White
                )
            )

            displayCategories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategoryClick(category) },
                    label = { Text(category, fontSize = 12.sp, maxLines = 1) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF618264),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(0.5.dp, Color(0xFFC6C6C6), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Product Image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(Color(0xFFD0E7D2), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (product.imageUrl != null) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(text = "🛍️", fontSize = 40.sp)
            }
        }

        // Product Details
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = product.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$${String.format("%.2f", product.price)}",
                    fontSize = 12.sp,
                    color = Color(0xFF618264),
                    fontWeight = FontWeight.SemiBold
                )
                if (product.category.isNotEmpty()) {
                    Text(
                        text = product.category,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            // Add to Cart Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(
                        if (product.isAvailable) Color(0xFF618264) else Color.Gray,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable(enabled = product.isAvailable) { onAddToCart() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (product.isAvailable) "ADD TO CART" else "OUT OF STOCK",
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            // BUY NOW BUTTON
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .background(
                        if (product.isAvailable) Color(0xFF618264) else Color.Gray,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable(enabled = product.isAvailable) { onBuyNow() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "BUY NOW",
                    fontSize = 10.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    modifier: Modifier = Modifier,
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOrdersClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Shadow gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF618264).copy(alpha = 0.15f),
                            Color(0xFF618264).copy(alpha = 0.35f)
                        )
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .background(Color.White)
                .padding(horizontal = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home
            NavItem(
                icon = Icons.Outlined.Home,
                label = "Home",
                isSelected = selectedTab == 0,
                onClick = {
                    onTabSelected(0)
                    onHomeClick()
                }
            )

            // Orders
            NavItem(
                icon = Icons.Outlined.Receipt,
                label = "Orders",
                isSelected = selectedTab == 1,
                onClick = {
                    onTabSelected(1)
                    onOrdersClick()
                }
            )

            // Profile
            NavItem(
                icon = Icons.Outlined.Person,
                label = "Profile",
                isSelected = selectedTab == 2,
                onClick = {
                    onTabSelected(2)
                    onProfileClick()
                }
            )
        }
    }
}

@Composable
private fun NavItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (isSelected) Color(0xFF618264) else Color.Transparent,
                    RoundedCornerShape(20.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isSelected) Color(0xFF618264) else Color.Gray
        )
    }
}