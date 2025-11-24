package com.example.setaside

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@Composable
fun CartScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToCheckout: (Double, Int) -> Unit = { _, _ -> }
) {
    var cartItems by remember {
        mutableStateOf(
            listOf(
                CartItem(name = "Fresh Tomatoes", price = 3.99, quantity = 2, imageRes = R.drawable.ic_launcher_foreground),
                CartItem(name = "Premium Beef", price = 12.99, quantity = 1, imageRes = R.drawable.ic_launcher_foreground),
                CartItem(name = "Potato Chips", price = 2.49, quantity = 3, imageRes = R.drawable.ic_launcher_foreground)
            )
        )
    }

    val subtotal = cartItems.sumOf { it.price * it.quantity }
    val deliveryFee = 2.99
    val total = subtotal + deliveryFee

    Scaffold(
        containerColor = Color(0xFFF2F7F2),
        topBar = {
            CartHeader(itemCount = cartItems.size, onNavigateBack = onNavigateBack)
        },
        bottomBar = {
            CartBottomSection(
                subtotal = subtotal,
                deliveryFee = deliveryFee,
                total = total,
                onCheckout = { onNavigateToCheckout(total, cartItems.size) }
            )
        }
    ) { paddingValues ->
        if (cartItems.isEmpty()) {
            EmptyCartState(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                itemsIndexed(cartItems) { index, item ->
                    CartItemRow(
                        item = item,
                        onQuantityChange = { newQuantity ->
                            cartItems = cartItems.toMutableList().apply {
                                this[index] = this[index].copy(quantity = newQuantity)
                            }
                        },
                        onDelete = {
                            cartItems = cartItems.toMutableList().apply {
                                removeAt(index)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartHeader(itemCount: Int, onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Cart",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF618264)
                )
                Text(
                    text = "$itemCount items",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF618264)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

@Composable
fun CartItemRow(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(Color(0xFFD9EED9), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingBasket,
                    contentDescription = null,
                    tint = Color(0xFF618264),
                    modifier = Modifier.size(40.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = "$${String.format(Locale.US, "%.2f", item.price)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF618264)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (item.quantity > 1) onQuantityChange(item.quantity - 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RemoveCircle,
                            contentDescription = "Decrease",
                            tint = Color(0xFF618264)
                        )
                    }

                    Text(
                        text = "${item.quantity}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.widthIn(min = 20.dp)
                    )

                    IconButton(
                        onClick = { onQuantityChange(item.quantity + 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Increase",
                            tint = Color(0xFF618264)
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFFF6B6B)
                )
            }
        }
    }
}

@Composable
fun CartBottomSection(
    subtotal: Double,
    deliveryFee: Double,
    total: Double,
    onCheckout: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Subtotal", fontSize = 14.sp, color = Color.Gray)
                Text(
                    "$${String.format(Locale.US,"%.2f", subtotal)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Delivery Fee", fontSize = 14.sp, color = Color.Gray)
                Text(
                    "$${String.format(Locale.US,"%.2f", deliveryFee)}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Total",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF618264)
                )
                Text(
                    "$${String.format(Locale.US,"%.2f", total)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF618264)
                )
            }

            Button(
                onClick = {
                    // TODO: Implement checkout navigation
                    println("Checkout clicked: $$${String.format(Locale.US,"%.2f", total)}")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF618264)
                )
            ) {
                Text(
                    "Proceed to Checkout",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun EmptyCartState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = Color(0xFFD0E7D2)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Your cart is empty",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF618264)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Add items to get started",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}
