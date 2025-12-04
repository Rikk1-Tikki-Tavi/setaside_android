package com.example.setaside.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String? = null,
    val price: Double = 0.0,
    val category: String = "",
    @SerializedName("is_available")
    val isAvailable: Boolean = true,
    @SerializedName("stock_quantity")
    val stockQuantity: Int? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null
)

data class ProductsResponse(
    @SerializedName("data")
    val products: List<Product> = emptyList(),
    val meta: ProductsMeta? = null
)

data class ProductsMeta(
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 100,
    val totalPages: Int = 1
)

data class CategoriesResponse(
    @SerializedName("data")
    val categories: List<String> = emptyList()
)

data class CreateProductRequest(
    val name: String,
    val description: String? = null,
    val price: Double,
    val category: String,
    @SerializedName("is_available")
    val isAvailable: Boolean = true,
    @SerializedName("stock_quantity")
    val stockQuantity: Int? = null,
    @SerializedName("image_url")
    val imageUrl: String? = null
)
