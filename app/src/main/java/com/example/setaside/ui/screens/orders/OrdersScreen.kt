package com.example.setaside.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.setaside.data.model.Order
import com.example.setaside.data.model.OrderStatus
import com.example.setaside.ui.screens.home.BottomNavigationBar
import com.example.setaside.ui.viewmodel.OrdersUiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    uiState: OrdersUiState,
    onNavigateBack: () -> Unit,
    onOrderClick: (Order) -> Unit,
    onRefresh: () -> Unit,
    onFilterChange: (OrderStatus?) -> Unit,
    selectedTab: Int = 1,
    onTabSelected: (Int) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var showCompleted by remember { mutableStateOf(false) }

    // Separate active and completed orders
    val activeOrders = uiState.orders.filter { order ->
        order.status != OrderStatus.PICKEDUP && order.status != OrderStatus.COMPLETED
    }

    val completedOrders = uiState.orders.filter { order ->
        order.status == OrderStatus.PICKEDUP || order.status == OrderStatus.COMPLETED
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(bottom = 77.dp)
        ) {
            // Green Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF618264))
                    .padding(start = 25.dp, end = 25.dp, top = 16.dp, bottom = 12.dp)
            ) {
                Text(
                    text = "My Orders",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Track your order status",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Tab Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                OrderTabButton(
                    title = "Active",
                    count = activeOrders.size,
                    isSelected = !showCompleted,
                    modifier = Modifier.weight(1f)
                ) {
                    showCompleted = false
                }

                OrderTabButton(
                    title = "Completed",
                    count = completedOrders.size,
                    isSelected = showCompleted,
                    modifier = Modifier.weight(1f)
                ) {
                    showCompleted = true
                }
            }

            // Orders List
            when {
                uiState.isLoading && uiState.orders.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = Color(0xFF618264),
                                strokeWidth = 3.dp
                            )
                            Text(
                                text = "Loading orders...",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
                (if (showCompleted) completedOrders else activeOrders).isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = if (showCompleted) Icons.Outlined.CheckCircle else Icons.Outlined.ShoppingBag,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp),
                                tint = Color.Gray.copy(alpha = 0.5f)
                            )
                            Text(
                                text = if (showCompleted) "No Completed Orders" else "No Active Orders",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                            Text(
                                text = if (showCompleted) "Your completed orders will appear here" else "Your active orders will appear here",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(if (showCompleted) completedOrders else activeOrders, key = { it.id }) { order ->
                            CustomerOrderCard(
                                order = order,
                                onClick = { onOrderClick(order) }
                            )
                        }

                        if (uiState.isLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = Color(0xFF618264),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Navigation
        BottomNavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            onOrdersClick = { /* Already on orders */ },
            onHomeClick = onHomeClick,
            onProfileClick = onProfileClick
        )
    }
}

@Composable
private fun OrderTabButton(
    title: String,
    count: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color(0xFF618264) else Color.Gray
            )

            if (count > 0) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) Color(0xFF618264) else Color.Gray.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = count.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color.Gray,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Underline indicator
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color(0xFF618264))
                    .padding(horizontal = 12.dp)
            )
        }
    }
}

@Composable
fun CustomerOrderCard(
    order: Order,
    onClick: () -> Unit
) {
    val customerStatus = when (order.status) {
        OrderStatus.PENDING, OrderStatus.PREPARING -> {
            Triple("Waiting for your order", Color(0xFFFF9800), Icons.Outlined.Schedule)
        }
        OrderStatus.READY -> {
            Triple("Ready for Pickup!", Color(0xFF4CAF50), Icons.Outlined.CheckCircle)
        }
        OrderStatus.PICKEDUP, OrderStatus.COMPLETED -> {
            Triple("Completed", Color.Gray, Icons.Outlined.ShoppingBag)
        }
        else -> {
            Triple("Processing", Color(0xFF2196F3), Icons.Outlined.Refresh)
        }
    }

    val isReady = order.status == OrderStatus.READY

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .then(
                if (isReady) {
                    Modifier.border(
                        width = 2.dp,
                        color = Color(0xFF4CAF50),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Order #${order.id.take(8).uppercase()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = formatDate(order.createdAt),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    if (order.totalAmount > 0) {
                        Text(
                            text = "$${String.format("%.2f", order.totalAmount)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF618264)
                        )
                    }
                }

                // Items Summary
                if (order.items.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${order.items.size} item${if (order.items.size > 1) "s" else ""}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = if (order.items.size > 1) {
                                "${order.items.first().product?.name ?: "Item"} + ${order.items.size - 1} more"
                            } else {
                                order.items.first().product?.name ?: "Item"
                            },
                            fontSize = 12.sp,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Pickup Time
                if (!order.pickupTime.isNullOrEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DateRange,
                            contentDescription = null,
                            modifier = Modifier.size(12.sp.value.dp),
                            tint = Color(0xFF9C27B0)
                        )
                        Text(
                            text = "Pickup: ${formatPickupTime(order.pickupTime)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF9C27B0)
                        )
                    }
                }
            }

            // Status Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(customerStatus.second)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = customerStatus.third,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )

                Text(
                    text = customerStatus.first,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                if (isReady) {
                    Text(
                        text = "TAP TO VIEW",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

private fun formatDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString.take(16)
    }
}

private fun formatPickupTime(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return ""
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val date = formatter.parse(dateString)

        if (date != null) {
            val outputFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            return outputFormat.format(date)
        }
        dateString
    } catch (e: Exception) {
        dateString
    }
}

fun OrderStatus?.displayName(): String = when (this) {
    OrderStatus.PENDING -> "Pending"
    OrderStatus.PREPARING -> "Preparing"
    OrderStatus.READY -> "Ready"
    OrderStatus.PICKEDUP -> "Picked Up"
    OrderStatus.COMPLETED -> "Completed"
    null -> "Unknown"
}

fun OrderStatus?.color(): Color = when (this) {
    OrderStatus.PENDING -> Color(0xFFFFA726)
    OrderStatus.PREPARING -> Color(0xFF42A5F5)
    OrderStatus.READY -> Color(0xFF66BB6A)
    OrderStatus.PICKEDUP -> Color(0xFF78909C)
    OrderStatus.COMPLETED -> Color(0xFF4CAF50)
    null -> Color.Gray
}
