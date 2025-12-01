package com.example.setaside.ui.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.setaside.data.model.CartItem
import com.example.setaside.ui.components.ErrorDialog
import com.example.setaside.ui.viewmodel.CartUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    uiState: CartUiState,
    onNavigateBack: () -> Unit,
    onCheckout: () -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onRemoveItem: (String) -> Unit,
    onClearError: () -> Unit
) {
    if (uiState.error != null) {
        ErrorDialog(message = uiState.error, onDismiss = onClearError)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Cart",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF618264)
                )
            )
        }
    ) { paddingValues ->
        if (uiState.isEmpty) {
            // Empty cart state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = Color.Gray
                    )
                    Text(
                        text = "Your cart is empty",
                        fontSize = 18.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Add some products to get started!",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF618264)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Browse Products")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Cart items list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.items, key = { it.product.id }) { item ->
                        CartItemCard(
                            item = item,
                            onQuantityChange = { newQty -> onUpdateQuantity(item.product.id, newQty) },
                            onRemove = { onRemoveItem(item.product.id) }
                        )
                    }
                }

                // Bottom checkout section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Items (${uiState.totalItems})",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "$${String.format("%.2f", uiState.totalPrice)}",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                        
                        HorizontalDivider()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "$${String.format("%.2f", uiState.totalPrice)}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF618264)
                            )
                        }

                        Button(
                            onClick = onCheckout,
                            enabled = !uiState.isCheckingOut,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF618264)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            if (uiState.isCheckingOut) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "PROCEED TO CHECKOUT",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CartItemCard(
    item: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
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
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Product image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color(0xFFD0E7D2), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (item.product.imageUrl != null) {
                    AsyncImage(
                        model = item.product.imageUrl,
                        contentDescription = item.product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("🛍️", fontSize = 32.sp)
                }
            }

            // Product details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$${String.format("%.2f", item.product.price)} each",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Quantity controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFD0E7D2),
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { if (item.quantity > 1) onQuantityChange(item.quantity - 1) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("-", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF618264))
                        }
                    }
                    
                    Text(
                        text = item.quantity.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(24.dp),
                        color = Color.Black
                    )
                    
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF618264),
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { onQuantityChange(item.quantity + 1) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("+", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Price and delete
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(80.dp)
            ) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = Color.Red.copy(alpha = 0.7f)
                    )
                }
                
                Text(
                    text = "$${String.format("%.2f", item.totalPrice)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF618264)
                )
            }
        }
    }
}
