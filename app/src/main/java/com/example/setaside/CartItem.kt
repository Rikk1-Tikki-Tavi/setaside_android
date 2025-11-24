package com.example.setaside

data class CartItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val price: Double,
    var quantity: Int,
    val imageRes: Int // Use drawable resource ID
)