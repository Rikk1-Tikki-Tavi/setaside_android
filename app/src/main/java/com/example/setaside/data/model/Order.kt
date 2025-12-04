package com.example.setaside.data.model

import com.google.gson.annotations.SerializedName

data class Order(
    val id: String = "",
    @SerializedName("customer_id")
    val customerId: String = "",
    val status: OrderStatus? = OrderStatus.PENDING,
    val notes: String? = null,
    @SerializedName("pickup_time")
    val pickupTime: String? = null,
    @SerializedName("total_amount")
    val totalAmount: Double = 0.0,
    val items: List<OrderItem> = emptyList(),
    val customer: User? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

enum class OrderStatus {
    @SerializedName("pending")
    PENDING,
    @SerializedName("preparing")
    PREPARING,
    @SerializedName("ready")
    READY,
    @SerializedName("pickedup")
    PICKEDUP,
    @SerializedName("completed")
    COMPLETED;

    // Convert enum to API-compatible string
    fun toApiString(): String = when (this) {
        PENDING -> "pending"
        PREPARING -> "preparing"
        READY -> "ready"
        PICKEDUP -> "pickedup"
        COMPLETED -> "completed"
    }
}

data class OrderItem(
    val id: String = "",
    @SerializedName("order_id")
    val orderId: String = "",
    @SerializedName("product_id")
    val productId: String = "",
    val quantity: Int = 1,
    @SerializedName("unit_price")
    val unitPrice: Double = 0.0,
    @SerializedName("special_instructions")
    val specialInstructions: String? = null,
    val product: Product? = null
)

data class OrdersResponse(
    @SerializedName("data")
    val orders: List<Order> = emptyList(),
    val meta: OrdersMeta? = null
)

data class OrdersMeta(
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 10,
    val totalPages: Int = 1
)

data class CreateOrderRequest(
    val notes: String? = null,
    @SerializedName("pickup_time")
    val pickupTime: String? = null,
    val items: List<CreateOrderItemRequest> = emptyList()
)

data class CreateOrderItemRequest(
    @SerializedName("product_id")
    val productId: String,
    val quantity: Int,
    @SerializedName("special_instructions")
    val specialInstructions: String? = null
)

data class UpdateOrderStatusRequest(
    val status: String
)

data class AddOrderItemRequest(
    @SerializedName("product_id")
    val productId: String,
    val quantity: Int,
    @SerializedName("special_instructions")
    val specialInstructions: String? = null
)

data class UpdateOrderItemRequest(
    val quantity: Int? = null,
    @SerializedName("special_instructions")
    val specialInstructions: String? = null
)