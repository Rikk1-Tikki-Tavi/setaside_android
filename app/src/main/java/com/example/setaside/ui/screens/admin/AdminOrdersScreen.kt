package com.example.setaside.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.AttachMoney
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material3.*
import androidx.compose.foundation.border
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.setaside.data.model.Order
import com.example.setaside.data.model.OrderStatus
//import com.example.setaside.ui.components.OrderCompletionDialog
import com.example.setaside.ui.screens.orders.color
import com.example.setaside.ui.screens.orders.displayName
import com.example.setaside.ui.viewmodel.OrdersUiState
import com.example.setaside.R
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(
    uiState: OrdersUiState,
    onOrderClick: (Order) -> Unit,
    onRefresh: () -> Unit,
    onFilterChange: (OrderStatus?) -> Unit,
    onUpdateStatus: (String, String) -> Unit,
    onDismissCompletionModal: () -> Unit = {},
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onProductsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var selectedStatus by remember { mutableStateOf("all") }
    var searchText by remember { mutableStateOf("") }
    var showStatusConfirmation by remember { mutableStateOf(false) }
    var pendingStatusUpdate by remember { mutableStateOf<Triple<String, String, Order>?>(null) }
    var showPaymentReceipt by remember { mutableStateOf(false) }
    var orderForReceipt by remember { mutableStateOf<Order?>(null) }
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }
    var isSuccess by remember { mutableStateOf(true) }

    // Filter and search orders
    val filteredOrders = when (selectedStatus) {
        "all" -> uiState.orders
        "pending" -> uiState.orders.filter { it.status == OrderStatus.PENDING }
        "preparing" -> uiState.orders.filter { it.status == OrderStatus.PREPARING }
        "ready" -> uiState.orders.filter { it.status == OrderStatus.READY }
        "pickedup" -> uiState.orders.filter { it.status == OrderStatus.PICKEDUP }
        "completed" -> uiState.orders.filter { it.status == OrderStatus.COMPLETED }
        else -> uiState.orders
    }

    val searchedOrders = if (searchText.isEmpty()) {
        filteredOrders
    } else {
        filteredOrders.filter { order ->
            order.id.lowercase().contains(searchText.lowercase()) ||
                    order.customer?.fullName?.lowercase()?.contains(searchText.lowercase()) == true ||
                    order.customer?.phone?.contains(searchText) == true ||
                    order.items.any { item ->
                        item.product?.name?.lowercase()?.contains(searchText.lowercase()) == true
                    }
        }
    }

//    // Show completion modal when order is picked up
//    if (uiState.showCompletionModal) {
//        OrderCompletionDialog(onDismiss = onDismissCompletionModal)
//    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with title and refresh button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3E5E40))
                    .padding(WindowInsets.statusBars.asPaddingValues())
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Orders",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "${uiState.orders.size} total orders",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Refresh",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Status Cards (Horizontally Scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OrderStatCard(
                    title = "Pending",
                    count = uiState.orders.count { it.status == OrderStatus.PENDING },
                    icon = "clock.fill",
                    color = Color(0xFFFF9800),
                    isSelected = selectedStatus == "pending"
                ) { selectedStatus = "pending" }

                OrderStatCard(
                    title = "Preparing",
                    count = uiState.orders.count { it.status == OrderStatus.PREPARING },
                    icon = "flame.fill",
                    color = Color(0xFF2196F3),
                    isSelected = selectedStatus == "preparing"
                ) { selectedStatus = "preparing" }

                OrderStatCard(
                    title = "Ready",
                    count = uiState.orders.count { it.status == OrderStatus.READY },
                    icon = "checkmark.circle.fill",
                    color = Color(0xFF4CAF50),
                    isSelected = selectedStatus == "ready"
                ) { selectedStatus = "ready" }

                OrderStatCard(
                    title = "Picked Up",
                    count = uiState.orders.count { it.status == OrderStatus.PICKEDUP },
                    icon = "bag.fill",
                    color = Color(0xFF9C27B0),
                    isSelected = selectedStatus == "pickedup"
                ) { selectedStatus = "pickedup" }

                OrderStatCard(
                    title = "Completed",
                    count = uiState.orders.count { it.status == OrderStatus.COMPLETED },
                    icon = "checkmark.seal.fill",
                    color = Color.Gray,
                    isSelected = selectedStatus == "completed"
                ) { selectedStatus = "completed" }
            }

            // Search & Filter Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search Field
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(18.sp.value.dp)
                    )

                    BasicTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 24.dp),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 12.sp,
                            color = Color.Black
                        ),
                        decorationBox = { innerTextField ->
                            if (searchText.isEmpty()) {
                                Text(
                                    "Search orders...",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            innerTextField()
                        }
                    )

                    if (searchText.isNotEmpty()) {
                        IconButton(
                            onClick = { searchText = "" },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Clear",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.sp.value.dp)
                            )
                        }
                    }
                }

                // All Orders Button
                Button(
                    onClick = { selectedStatus = "all" },
                    modifier = Modifier
                        .height(44.dp)
                        .background(
                            if (selectedStatus == "all") Color(0xFF3E5E40) else Color.White,
                            RoundedCornerShape(10.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedStatus == "all") Color(0xFF3E5E40) else Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    Text(
                        "All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (selectedStatus == "all") Color.White else Color(0xFF3E5E40)
                    )
                }
            }

            // Orders List
            if (uiState.isLoading && uiState.orders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 77.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = Color(0xFF3E5E40))
                        Text(
                            "Loading orders...",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else if (searchedOrders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 77.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Receipt,
                            contentDescription = null,
                            modifier = Modifier.size(50.dp),
                            tint = Color.Gray.copy(alpha = 0.5f)
                        )
                        Text(
                            "No orders found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Text(
                            if (searchText.isEmpty()) "Orders will appear here" else "Try different search terms",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 77.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(searchedOrders, key = { it.id }) { order ->
                        AdminOrderCard(
                            order = order,
                            onStatusUpdate = { orderId, newStatus ->
                                pendingStatusUpdate = Triple(orderId, newStatus, order)
                                showStatusConfirmation = true
                            }
                        )
                    }
                }
            }
        }

        // Status Update Confirmation Modal
        if (showStatusConfirmation && pendingStatusUpdate != null) {
            val (orderId, newStatus, order) = pendingStatusUpdate!!
            StatusUpdateConfirmationModal(
                order = order,
                newStatus = newStatus,
                onConfirm = {
                    showStatusConfirmation = false
                    onUpdateStatus(orderId, newStatus)
                    // Check if completed - show receipt
                    if (newStatus == "completed") {
                        orderForReceipt = order
                        showPaymentReceipt = true
                    } else {
                        // Show success alert for non-completed statuses
                        val statusDisplay = newStatus.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        alertMessage = "Order status updated to $statusDisplay"
                        isSuccess = true
                        showAlert = true
                    }
                    pendingStatusUpdate = null
                },
                onCancel = {
                    showStatusConfirmation = false
                    pendingStatusUpdate = null
                }
            )
        }

        // Success/Error Alert Dialog
        if (showAlert) {
            AlertDialog(
                onDismissRequest = { showAlert = false },
                title = {
                    Text(
                        if (isSuccess) "Success" else "Error",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSuccess) Color(0xFF3E5E40) else Color(0xFFE53935)
                    )
                },
                text = {
                    Text(
                        alertMessage,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showAlert = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSuccess) Color(0xFF3E5E40) else Color(0xFFE53935)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "OK",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(0.85f),
                shape = RoundedCornerShape(16.dp),
                containerColor = Color.White
            )
        }

        // Payment Receipt Modal
        if (showPaymentReceipt && orderForReceipt != null) {
            PaymentReceiptModal(
                order = orderForReceipt!!,
                onPaymentSelected = { paymentMethod ->
                    showPaymentReceipt = false
                    orderForReceipt = null
                    alertMessage = "Payment method: $paymentMethod. Order picked up successfully!"
                    isSuccess = true
                    showAlert = true
                },
                onDismiss = {
                    showPaymentReceipt = false
                    orderForReceipt = null
                }
            )
        }

        // Bottom Navigation
        AdminBottomNavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
            onHomeClick = { /* Already on home */ },
            onProductsClick = onProductsClick,
            onProfileClick = onProfileClick
        )
    }
}

@Composable
fun AdminOrderCard(
    order: Order,
    onStatusUpdate: (String, String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Row - Order ID and Total
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "#${order.id.take(8).uppercase()}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )

                            // Status Badge
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(
                                        order.status.color().copy(alpha = 0.12f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Receipt,
                                    contentDescription = null,
                                    tint = order.status.color(),
                                    modifier = Modifier.size(12.sp.value.dp)
                                )
                                Text(
                                    order.status.displayName(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = order.status.color()
                                )
                            }
                        }

                        order.createdAt?.let {
                            Text(
                                formatDate(it),
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            "$${String.format("%.2f", order.totalAmount)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF3E5E40)
                        )
                        Text(
                            "${order.items.size} items",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                // Customer Row
                order.customer?.let { customer ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    Color(0xFF3E5E40).copy(alpha = 0.15f),
                                    RoundedCornerShape(50)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                customer.fullName.take(1).uppercase(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3E5E40)
                            )
                        }

                        Column(
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                customer.fullName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                            customer.phone?.let {
                                if (it.isNotEmpty()) {
                                    Text(
                                        it,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        // Pickup Time
                        order.pickupTime?.let { pickupTime ->
                            if (pickupTime.isNotEmpty()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(
                                            Color(0xFF9C27B0).copy(alpha = 0.1f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Receipt,
                                        contentDescription = null,
                                        tint = Color(0xFF9C27B0),
                                        modifier = Modifier.size(12.sp.value.dp)
                                    )
                                    Text(
                                        formatPickupTime(pickupTime),
                                        fontSize = 11.sp,
                                        color = Color(0xFF9C27B0)
                                    )
                                }
                            }
                        }
                    }
                }

                // Expandable Items Section
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            isExpanded = !isExpanded
                        }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Order Details",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(18.sp.value.dp)
                    )
                }

                if (isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        order.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "${item.quantity}×",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.width(24.dp)
                                )

                                Text(
                                    item.product?.name ?: "Item",
                                    fontSize = 11.sp,
                                    color = Color.Black,
                                    modifier = Modifier.weight(1f)
                                )

                                Text(
                                    "$${String.format("%.2f", item.unitPrice * item.quantity)}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                        order.notes?.let { notes ->
                            if (notes.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Color(0xFFFF9800).copy(alpha = 0.08f),
                                            RoundedCornerShape(6.dp)
                                        )
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = null,
                                        tint = Color(0xFFFF9800),
                                        modifier = Modifier.size(12.sp.value.dp)
                                    )
                                    Text(
                                        notes,
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Action Button
            getNextStatusForOrder(order.status)?.let { nextStatus ->
                Button(
                    onClick = { onStatusUpdate(order.id, nextStatus) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = getNextStatusColor(order.status)
                    ),
                    shape = RoundedCornerShape(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Receipt,
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .alpha(0.8f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        getActionButtonLabel(order.status),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun OrderStatCard(
    title: String,
    count: Int,
    icon: String,
    color: Color,
    isSelected: Boolean,
    action: () -> Unit
) {
    Button(
        onClick = action,
        modifier = Modifier
            .width(80.dp)
            .height(92.dp)
            .background(
                if (isSelected) color.copy(alpha = 0.1f) else Color.White,
                RoundedCornerShape(12.dp)
            )
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, color, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                }
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) color.copy(alpha = 0.1f) else Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Receipt,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.sp.value.dp)
                )

                Text(
                    "$count",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Text(
                title,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun StatusUpdateConfirmationModal(
    order: Order,
    newStatus: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    val statusColor = when (newStatus) {
        "pending" -> Color(0xFFFF9800)
        "preparing" -> Color(0xFFFF9800)
        "ready" -> Color(0xFF2196F3)
        "pickedup" -> Color(0xFF9C27B0)
        "completed" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }

    val statusDisplayName = when (newStatus) {
        "pending" -> "Pending"
        "preparing" -> "Preparing"
        "ready" -> "Ready"
        "pickedup" -> "Picked Up"
        "completed" -> "Completed"
        else -> newStatus.replaceFirstChar { it.uppercase() }
    }

    val statusDescription = when (newStatus) {
        "preparing" -> "Start preparing this order?"
        "ready" -> "Mark order as ready for pickup?"
        "pickedup" -> "Confirm customer picked up order?"
        "completed" -> "Mark order as completed?"
        else -> "Update the order status?"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(enabled = true) { onCancel() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.75f)
                .clickable(enabled = false) { },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status Icon
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(
                            statusColor.copy(alpha = 0.2f),
                            RoundedCornerShape(50)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Receipt,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(28.sp.value.dp)
                    )
                }

                // Title
                Text(
                    "Update Status",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                // Order Info
                Text(
                    "Order #${order.id.take(8).uppercase()}",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                // Status Badge
                Text(
                    statusDisplayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    modifier = Modifier
                        .background(
                            statusColor.copy(alpha = 0.2f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Description
                Text(
                    statusDescription,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Black
                        ),
                        border = BorderStroke(width = 1.dp, color = Color(0xFFE0E0E0)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "Cancel",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = statusColor
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            "Confirm",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentReceiptModal(
    order: Order,
    onPaymentSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPaymentMethod by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(enabled = true) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(horizontal = 32.dp)
                .padding(vertical = 60.dp)
                .clickable(enabled = false) { },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                // Top decorative edge
                Spacer(modifier = Modifier.height(12.dp))
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = false) { },
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        // Store Header
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp)
                                .padding(bottom = 12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_green_logo),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(40.dp)
                            )

                            Text(
                                "SetAside",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "Fresh Groceries",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        // Dashed Divider
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            repeat(60) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(1.dp)
                                        .background(Color.Gray.copy(alpha = 0.5f))
                                )
                            }
                        }

                        // Order Info
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                "Order #${order.id.take(8).uppercase()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                getCurrentDateTime(),
                                fontSize = 10.sp,
                                color = Color.Gray
                            )

                            order.customer?.let {
                                Text(
                                    it.fullName,
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        // Dashed Divider
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            repeat(60) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(1.dp)
                                        .background(Color.Gray.copy(alpha = 0.5f))
                                )
                            }
                        }

                        // Items
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(vertical = 12.dp)
                        ) {
                            order.items?.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            "${item.quantity}x",
                                            fontSize = 10.sp,
                                            color = Color.Gray,
                                            modifier = Modifier.width(24.dp)
                                        )

                                        Text(
                                            item.product?.name ?: "Item",
                                            fontSize = 10.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 8.dp)
                                        )
                                    }

                                    Text(
                                        "$${String.format("%.2f", item.unitPrice * item.quantity)}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Dashed Divider
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            repeat(60) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(1.dp)
                                        .background(Color.Gray.copy(alpha = 0.5f))
                                )
                            }
                        }

                        // Total
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .padding(vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "TOTAL",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                "$${String.format("%.2f", order.totalAmount ?: 0.0)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Dashed Divider
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            repeat(60) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(1.dp)
                                        .background(Color.Gray.copy(alpha = 0.5f))
                                )
                            }
                        }

                        // Payment Method Selection
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Text(
                                "SELECT PAYMENT METHOD",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .padding(top = 12.dp)
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PaymentOption(
                                    icon = Icons.Outlined.AttachMoney,
                                    label = "Cash",
                                    color = Color(0xFF4CAF50),
                                    isSelected = selectedPaymentMethod == "Cash",
                                    modifier = Modifier.weight(1f)
                                ) {
                                    selectedPaymentMethod = "Cash"
                                }

                                PaymentOption(
                                    icon = Icons.Outlined.QrCode,
                                    label = "QRIS",
                                    color = Color(0xFF2196F3),
                                    isSelected = selectedPaymentMethod == "QRIS",
                                    modifier = Modifier.weight(1f)
                                ) {
                                    selectedPaymentMethod = "QRIS"
                                }

                                PaymentOption(
                                    icon = Icons.Outlined.CreditCard,
                                    label = "Card",
                                    color = Color(0xFF9C27B0),
                                    isSelected = selectedPaymentMethod == "Card",
                                    modifier = Modifier.weight(1f)
                                ) {
                                    selectedPaymentMethod = "Card"
                                }
                            }

                            // QRIS QR Code
                            if (selectedPaymentMethod == "QRIS") {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .background(
                                            Color(0xFF2196F3).copy(alpha = 0.05f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        "Scan to Pay",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF2196F3)
                                    )

                                    // QRIS QR Code Image
                                    Image(
                                        painter = painterResource(id = R.drawable.qris),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(140.dp)
                                            .background(Color.White, RoundedCornerShape(12.dp))
                                            .border(2.dp, Color(0xFF2196F3).copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    Text(
                                        "SetAside Payment",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            // Card EDC Message
                            if (selectedPaymentMethod == "Card") {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .background(
                                            Color(0xFF9C27B0).copy(alpha = 0.05f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CreditCard,
                                        contentDescription = null,
                                        tint = Color(0xFF9C27B0),
                                        modifier = Modifier.size(32.dp)
                                    )

                                    Text(
                                        "Continue to EDC Machine",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF9C27B0)
                                    )

                                    Text(
                                        "Please process the card payment\nusing the EDC terminal",
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            // Cash Message
                            if (selectedPaymentMethod == "Cash") {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .background(
                                            Color(0xFF4CAF50).copy(alpha = 0.05f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AttachMoney,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(32.dp)
                                    )

                                    Text(
                                        "Cash Payment",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50)
                                    )

                                    Text(
                                        "Collect $${String.format("%.2f", order.totalAmount ?: 0.0)} from customer",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        // Confirm Button
                        Button(
                            onClick = {
                                selectedPaymentMethod?.let { onPaymentSelected(it) }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .padding(horizontal = 16.dp),
                            enabled = selectedPaymentMethod != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedPaymentMethod != null) Color(0xFF3E5E40) else Color.Gray,
                                disabledContainerColor = Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                if (selectedPaymentMethod != null) "Confirm $selectedPaymentMethod Payment" else "Select Payment Method",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Cancel Button
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                        ) {
                            Text(
                                "Cancel",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }

                        // Thank you message
                        Text(
                            "Thank you for your order!",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        )
                    }
                }
            }

            item {
                // Bottom decorative space
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun PaymentOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    action: () -> Unit
) {
    Button(
        onClick = action,
        modifier = modifier
            .height(100.dp)
            .background(
                if (isSelected) color.copy(alpha = 0.08f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .then(
                if (isSelected) {
                    Modifier.border(1.5.dp, color, RoundedCornerShape(12.dp))
                } else {
                    Modifier
                }
            ),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) color.copy(alpha = 0.08f) else Color.Transparent,
            contentColor = if (isSelected) color else Color.Gray
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(8.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        if (isSelected) color.copy(alpha = 0.15f) else Color(0xFFF0F0F0),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) color else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) color else Color.Gray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

private fun getNextStatusColor(currentStatus: OrderStatus?): Color {
    return when (currentStatus) {
        OrderStatus.PENDING -> Color(0xFFFF9800)
        OrderStatus.PREPARING -> Color(0xFF2196F3)
        OrderStatus.READY -> Color(0xFF9C27B0)
        OrderStatus.PICKEDUP -> Color(0xFF4CAF50)
        OrderStatus.COMPLETED -> Color.Gray
        null -> Color.Gray
    }
}

private fun getNextStatusForOrder(currentStatus: OrderStatus?): String? {
    return when (currentStatus) {
        OrderStatus.PENDING -> "preparing"
        OrderStatus.PREPARING -> "ready"
        OrderStatus.READY -> "pickedup"
        OrderStatus.PICKEDUP -> "completed"
        OrderStatus.COMPLETED -> null
        null -> null
    }
}

private fun getActionButtonLabel(currentStatus: OrderStatus?): String {
    return when (currentStatus) {
        OrderStatus.PENDING -> "Start Preparing"
        OrderStatus.PREPARING -> "Mark Ready"
        OrderStatus.READY -> "Picked Up"
        OrderStatus.PICKEDUP -> "Complete"
        else -> "Update Status"
    }
}

private fun formatDate(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString.take(16)
    }
}

private fun formatPickupTime(dateString: String?): String {
    if (dateString.isNullOrEmpty()) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        date?.let { outputFormat.format(it) } ?: dateString
    } catch (e: Exception) {
        dateString.take(16)
    }
}

private fun getCurrentDateTime(): String {
    val formatter = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return formatter.format(Date())
}

@Composable
fun AdminBottomNavigationBar(
    modifier: Modifier = Modifier,
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    onHomeClick: () -> Unit = {},
    onProductsClick: () -> Unit = {},
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
                            Color(0xFF3E5E40).copy(alpha = 0.15f),
                            Color(0xFF3E5E40).copy(alpha = 0.35f)
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
            // Orders (Home for admin)
            AdminNavItem(
                icon = Icons.Outlined.Receipt,
                label = "Orders",
                isSelected = selectedTab == 0,
                onClick = {
                    onTabSelected(0)
                    onHomeClick()
                }
            )

            // Products
            AdminNavItem(
                icon = Icons.Outlined.AdminPanelSettings,
                label = "Products",
                isSelected = selectedTab == 1,
                onClick = {
                    onTabSelected(1)
                    onProductsClick()
                }
            )

            // Profile
            AdminNavItem(
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
fun AdminNavItem(
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
                    if (isSelected) Color(0xFF3E5E40) else Color.Transparent,
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
            color = if (isSelected) Color(0xFF3E5E40) else Color.Gray
        )
    }
}