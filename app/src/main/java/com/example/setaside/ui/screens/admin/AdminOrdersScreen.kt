package com.example.setaside.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.setaside.data.model.Order
import com.example.setaside.data.model.OrderStatus
import com.example.setaside.ui.screens.orders.color
import com.example.setaside.ui.screens.orders.displayName
import com.example.setaside.ui.viewmodel.OrdersUiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    uiState: OrdersUiState,
    onNavigateBack: () -> Unit,
    onOrderClick: (Order) -> Unit,
    onRefresh: () -> Unit,
    onFilterChange: (OrderStatus?) -> Unit,
    onUpdateStatus: (String, String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf<OrderStatus?>(null) }
    var showStatusDialog by remember { mutableStateOf<Order?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            "Admin Orders",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
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
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3E5E40) // Darker green for admin
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Status filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = {
                        selectedFilter = null
                        onFilterChange(null)
                    },
                    label = { Text("All", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF3E5E40),
                        selectedLabelColor = Color.White
                    )
                )
                OrderStatus.entries.forEach { status ->
                    FilterChip(
                        selected = selectedFilter == status,
                        onClick = {
                            selectedFilter = status
                            onFilterChange(status)
                        },
                        label = { Text(status.displayName(), fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = status.color(),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Order count
            if (!uiState.isLoading && uiState.orders.isNotEmpty()) {
                Text(
                    text = "${uiState.orders.size} order(s)",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF3E5E40))
                }
            } else if (uiState.orders.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AdminPanelSettings,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.Gray
                        )
                        Text(
                            text = "No orders found",
                            fontSize = 18.sp,
                            color = Color.Gray
                        )
                        TextButton(onClick = onRefresh) {
                            Text("Refresh", color = Color(0xFF3E5E40))
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.orders, key = { it.id }) { order ->
                        AdminOrderCard(
                            order = order,
                            onClick = { onOrderClick(order) },
                            onUpdateStatus = { showStatusDialog = order }
                        )
                    }
                }
            }
        }

        // Status update dialog
        showStatusDialog?.let { order ->
            StatusUpdateDialog(
                order = order,
                isUpdating = uiState.isUpdatingStatus,
                onDismiss = { showStatusDialog = null },
                onConfirm = { newStatus ->
                    onUpdateStatus(order.id, newStatus)
                    showStatusDialog = null
                }
            )
        }
    }
}

@Composable
fun AdminOrderCard(
    order: Order,
    onClick: () -> Unit,
    onUpdateStatus: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with order ID and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.id.take(8)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = formatDate(order.createdAt),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = order.status.color()
                ) {
                    Text(
                        text = order.status.displayName(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE0E0E0))

            // Customer info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "👤 Customer",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = order.customer?.fullName ?: "Customer #${order.customerId.take(6)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "📧 Email",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = order.customer?.email ?: "N/A",
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
            }

            // Order items summary
            Column {
                Text(
                    text = "📦 Items (${order.items.size})",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                order.items.take(3).forEach { item ->
                    Text(
                        text = "• ${item.quantity}x ${item.product?.name ?: "Item"}",
                        fontSize = 13.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                if (order.items.size > 3) {
                    Text(
                        text = "... and ${order.items.size - 3} more",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE0E0E0))

            // Total and action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "$${String.format("%.2f", order.totalAmount)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF618264)
                    )
                }

                if (order.status != OrderStatus.PICKED_UP) {
                    Button(
                        onClick = onUpdateStatus,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = getNextStatusColor(order.status)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Update Status",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusUpdateDialog(
    order: Order,
    isUpdating: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(order.status) }

    AlertDialog(
        onDismissRequest = { if (!isUpdating) onDismiss() },
        title = {
            Text(
                "Update Order Status",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Order #${order.id.take(8)}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                
                Text(
                    text = "Current Status: ${order.status.displayName()}",
                    fontSize = 14.sp
                )

                Text(
                    text = "Select new status:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    getAvailableNextStatuses(order.status).forEach { status ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedStatus = status }
                                .background(
                                    if (selectedStatus == status) status.color().copy(alpha = 0.1f) 
                                    else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            RadioButton(
                                selected = selectedStatus == status,
                                onClick = { selectedStatus = status },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = status.color()
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = status.displayName(),
                                fontWeight = if (selectedStatus == status) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedStatus == status) status.color() else Color.Black
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedStatus.name.lowercase()) },
                enabled = !isUpdating && selectedStatus != order.status,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF618264))
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Update")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isUpdating
            ) {
                Text("Cancel")
            }
        }
    )
}

private fun getNextStatusColor(currentStatus: OrderStatus): Color {
    return when (currentStatus) {
        OrderStatus.PENDING -> OrderStatus.PREPARING.color()
        OrderStatus.PREPARING -> OrderStatus.READY.color()
        OrderStatus.READY -> OrderStatus.PICKED_UP.color()
        OrderStatus.PICKED_UP -> Color.Gray
    }
}

private fun getAvailableNextStatuses(currentStatus: OrderStatus): List<OrderStatus> {
    return when (currentStatus) {
        OrderStatus.PENDING -> listOf(OrderStatus.PREPARING, OrderStatus.READY)
        OrderStatus.PREPARING -> listOf(OrderStatus.READY, OrderStatus.PICKED_UP)
        OrderStatus.READY -> listOf(OrderStatus.PICKED_UP)
        OrderStatus.PICKED_UP -> emptyList()
    }
}

private fun formatDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString.take(16)
    }
}
