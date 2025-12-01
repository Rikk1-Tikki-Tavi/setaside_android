package com.example.setaside.data.model

// Cart item for local cart management
data class CartItem(
    val product: Product,
    var quantity: Int = 1,
    var specialInstructions: String? = null
) {
    val totalPrice: Double
        get() = product.price * quantity
}
