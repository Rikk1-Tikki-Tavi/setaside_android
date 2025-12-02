package com.example.setaside.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.setaside.data.model.Order
import com.example.setaside.data.model.OrderItem
import com.example.setaside.data.model.OrderStatus
import com.example.setaside.ui.viewmodel.OrdersUiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    uiState: OrdersUiState,
    orderId: String,
    onNavigateBack: () -> Unit,
    onLoadOrder: (String) -> Unit
) {
    LaunchedEffect(orderId) {
        onLoadOrder(orderId)
    }

    val order = uiState.selectedOrder

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Order Details",
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
        if (uiState.isLoading || order == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF618264))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Order Status Card
                item {
                    OrderStatusCard(order)
                }

                // Order Info Card
                item {
                    OrderInfoCard(order)
                }

                // Order Items Header
                item {
                    Text(
                        text = "Order Items",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                // Order Items
                items(order.items) { item ->
                    OrderItemCard(item)
                }

                // Order Summary
                item {
                    OrderSummaryCard(order)
                }

                // Notes (if any)
                if (!order.notes.isNullOrEmpty()) {
                    item {
                        NotesCard(order.notes)
                    }
                }
            }
        }
    }
}

@Composable
private fun OrderStatusCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = order.status.color().copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = order.status.color()
            ) {
                Text(
                    text = order.status.displayName(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                )
            }
            
            Text(
                text = when (order.status) {
                    OrderStatus.PENDING -> "Your order is being processed"
                    OrderStatus.PREPARING -> "Your order is being prepared"
                    OrderStatus.READY -> "Your order is ready for pickup!"
                    OrderStatus.PICKEDUP -> "Your order has been picked up"
                    OrderStatus.COMPLETED -> "Order completed successfully!"
                    null -> "Status unknown"
                },
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun OrderInfoCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Order Information",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            HorizontalDivider()
            
            InfoRow("Order ID", "#${order.id.take(8)}")
            InfoRow("Date", formatDate(order.createdAt))
            if (order.pickupTime != null) {
                InfoRow("Pickup Time", formatDate(order.pickupTime))
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 14.sp, color = Color.Gray)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
    }
}

@Composable
private fun OrderItemCard(item: OrderItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product image
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFFD0E7D2), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (item.product?.imageUrl != null) {
                    AsyncImage(
                        model = item.product.imageUrl,
                        contentDescription = item.product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("🛍️", fontSize = 24.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.product?.name ?: "Product",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
                Text(
                    text = "Qty: ${item.quantity} × $${String.format("%.2f", item.unitPrice)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (!item.specialInstructions.isNullOrEmpty()) {
                    Text(
                        text = "Note: ${item.specialInstructions}",
                        fontSize = 11.sp,
                        color = Color(0xFF618264)
                    )
                }
            }

            Text(
                text = "$${String.format("%.2f", item.unitPrice * item.quantity)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF618264)
            )
        }
    }
}

@Composable
private fun OrderSummaryCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Order Summary",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            HorizontalDivider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Items (${order.items.size})", fontSize = 14.sp, color = Color.Gray)
                Text(
                    "$${String.format("%.2f", order.totalAmount)}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            
            HorizontalDivider()
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Text(
                    "$${String.format("%.2f", order.totalAmount)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF618264)
                )
            }
        }
    }
}

@Composable
private fun NotesCard(notes: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "📝 Order Notes",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFF57C00)
            )
            Text(
                text = notes,
                fontSize = 14.sp,
                color = Color(0xFF795548)
            )
        }
    }
}

private fun formatDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "N/A"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString.take(16)
    }
}
