package com.example.setaside.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ExperimentalMaterial3Api
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
    var showCancelConfirmation by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }
    var editedNotes by remember { mutableStateOf("") }

    LaunchedEffect(orderId) {
        onLoadOrder(orderId)
    }

    val order = uiState.selectedOrder

    LaunchedEffect(order) {
        if (order != null) {
            editedNotes = order.notes ?: ""
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Order Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF618264),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (uiState.isLoading || order == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = Color(0xFF618264))
                    Text("Loading order details...", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Card
                item {
                    StatusCard(order)
                }

                // Order Information Card
                item {
                    OrderInfoCard(order)
                }

                // Order Items
                if (order.items.isNotEmpty()) {
                    item {
                        Text(
                            text = "Order Items",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF618264)
                        )
                    }

                    items(order.items) { item ->
                        OrderItemCard(item)
                    }
                }

                // Action Buttons (only for pending orders)
                if (order.status == OrderStatus.PENDING) {
                    item {
                        ActionButtons(
                            onEditClick = { isEditing = true },
                            onCancelClick = { showCancelConfirmation = true }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Cancel Order Confirmation Dialog
        if (showCancelConfirmation) {
            AlertDialog(
                onDismissRequest = { showCancelConfirmation = false },
                title = { Text("Cancel Order") },
                text = { Text("Are you sure you want to cancel this order? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            showCancelConfirmation = false
                            // TODO: Implement cancel order action
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                    ) {
                        Text("Cancel Order", color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showCancelConfirmation = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                    ) {
                        Text("Keep Order", color = Color.Black)
                    }
                }
            )
        }

        // Edit Order Sheet
        if (isEditing) {
            EditOrderSheet(
                notes = editedNotes,
                onNotesChange = { editedNotes = it },
                onDismiss = { isEditing = false },
                onSave = {
                    // TODO: Implement save order changes
                    isEditing = false
                }
            )
        }
    }
}

@Composable
private fun StatusCard(order: Order) {
    val statusColor = order.status?.color() ?: Color.Gray
    val statusMessage = getStatusMessage(order.status)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Status Icon
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(statusColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (order.status) {
                        OrderStatus.PENDING -> Icons.Outlined.Schedule
                        OrderStatus.PREPARING -> Icons.Outlined.LocalFireDepartment
                        OrderStatus.READY -> Icons.Outlined.CheckCircle
                        OrderStatus.PICKEDUP, OrderStatus.COMPLETED -> Icons.Outlined.ShoppingBag
                        null -> Icons.Outlined.Info
                    },
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = statusColor
                )
            }

            // Status Title and Message
            Text(
                text = order.status?.displayName() ?: "Unknown",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
            Text(
                text = statusMessage,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            // Progress Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                if (index <= getStatusIndex(order.status)) statusColor
                                else Color.Gray.copy(alpha = 0.2f),
                                RoundedCornerShape(1.5.dp)
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderInfoCard(order: Order) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Order Information",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF618264)
            )

            // Order ID Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Order ID", fontSize = 12.sp, color = Color.Gray)
                Text(
                    text = "#${order.id.take(8).uppercase()}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }

            // Placed on Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Placed on", fontSize = 12.sp, color = Color.Gray)
                Text(
                    text = formatDate(order.createdAt),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            }

            // Payment Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Payment", fontSize = 12.sp, color = Color.Gray)
                Text(
                    text = if (order.status == OrderStatus.PICKEDUP || order.status == OrderStatus.COMPLETED)
                        "Paid" else "Pay at Pickup",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (order.status == OrderStatus.PICKEDUP || order.status == OrderStatus.COMPLETED)
                        Color(0xFF4CAF50) else Color(0xFF2196F3)
                )
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Total Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total Amount",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "$${String.format("%.2f", order.totalAmount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF618264)
                )
            }
        }
    }
}

@Composable
private fun OrderItemCard(item: OrderItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quantity Badge
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF618264)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "x${item.quantity}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Product Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.product?.name ?: "Product",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$${String.format("%.2f", item.unitPrice)} each",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                if (!item.specialInstructions.isNullOrEmpty()) {
                    Text(
                        text = item.specialInstructions,
                        fontSize = 10.sp,
                        color = Color(0xFFFF9800),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Price
            Text(
                text = "$${String.format("%.2f", item.unitPrice * item.quantity)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF618264)
            )
        }
    }
}

@Composable
private fun ActionButtons(onEditClick: () -> Unit, onCancelClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Edit Order Button
        Button(
            onClick = onEditClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color(0xFF618264)
            ),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.5.dp, Color(0xFF618264))
        ) {
            Text(text = "Edit Order", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }

        // Cancel Order Button
        Button(
            onClick = onCancelClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFEBEE),
                contentColor = Color(0xFFD32F2F)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Cancel Order",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFD32F2F)
            )
        }
    }
}

private fun getStatusIndex(status: OrderStatus?): Int {
    return when (status) {
        OrderStatus.PENDING -> 0
        OrderStatus.PREPARING -> 1
        OrderStatus.READY -> 2
        OrderStatus.PICKEDUP, OrderStatus.COMPLETED -> 3
        null -> 0
    }
}

private fun getStatusMessage(status: OrderStatus?): String {
    return when (status) {
        OrderStatus.PENDING -> "Your order is waiting to be prepared"
        OrderStatus.PREPARING -> "Your order is being prepared"
        OrderStatus.READY -> "Your order is ready for pickup!"
        OrderStatus.PICKEDUP -> "Order completed"
        OrderStatus.COMPLETED -> "Order completed"
        null -> "Status unknown"
    }
}

private fun formatDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return "N/A"
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString.take(16)
    }
}

@Composable
private fun EditOrderSheet(
    notes: String,
    onNotesChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Order") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Order Notes", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = notes,
                    onValueChange = onNotesChange,
                    placeholder = { Text("Add any special requests...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    maxLines = 5,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF618264))
            ) {
                Text("Save Changes", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            ) {
                Text("Cancel", color = Color.Black)
            }
        }
    )
}