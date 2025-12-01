package com.example.setaside.data.remote

import com.example.setaside.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // ============ AUTH ============
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>
    
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
    
    @GET("auth/me")
    suspend fun getCurrentUser(): Response<User>
    
    // ============ USERS ============
    @GET("users/me")
    suspend fun getMyProfile(): Response<User>
    
    @PATCH("users/me")
    suspend fun updateMyProfile(@Body request: UpdateProfileRequest): Response<User>
    
    @GET("users")
    suspend fun listUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("role") role: String? = null,
        @Query("search") search: String? = null
    ): Response<List<User>>
    
    // ============ PRODUCTS ============
    @GET("products")
    suspend fun getProducts(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("category") category: String? = null,
        @Query("is_available") isAvailable: Boolean? = null,
        @Query("search") search: String? = null
    ): Response<ProductsResponse>
    
    @GET("products/categories")
    suspend fun getCategories(): Response<CategoriesResponse>
    
    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: String): Response<Product>
    
    @POST("products")
    suspend fun createProduct(@Body request: CreateProductRequest): Response<Product>
    
    @PATCH("products/{id}")
    suspend fun updateProduct(
        @Path("id") id: String,
        @Body request: Map<String, Any>
    ): Response<Product>
    
    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: String): Response<Unit>
    
    // ============ ORDERS ============
    @POST("orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<Order>
    
    @GET("orders")
    suspend fun getOrders(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @Query("status") status: String? = null,
        @Query("customer_id") customerId: String? = null
    ): Response<OrdersResponse>
    
    @GET("orders/{id}")
    suspend fun getOrder(@Path("id") id: String): Response<Order>
    
    @PATCH("orders/{id}")
    suspend fun updateOrder(
        @Path("id") id: String,
        @Body request: Map<String, Any>
    ): Response<Order>
    
    @PATCH("orders/{id}/status")
    suspend fun updateOrderStatus(
        @Path("id") id: String,
        @Body request: UpdateOrderStatusRequest
    ): Response<Order>
    
    @DELETE("orders/{id}")
    suspend fun deleteOrder(@Path("id") id: String): Response<Unit>
    
    // ============ ORDER ITEMS ============
    @GET("orders/{orderId}/items")
    suspend fun getOrderItems(@Path("orderId") orderId: String): Response<List<OrderItem>>
    
    @POST("orders/{orderId}/items")
    suspend fun addOrderItem(
        @Path("orderId") orderId: String,
        @Body request: AddOrderItemRequest
    ): Response<OrderItem>
    
    @PATCH("orders/{orderId}/items/{itemId}")
    suspend fun updateOrderItem(
        @Path("orderId") orderId: String,
        @Path("itemId") itemId: String,
        @Body request: UpdateOrderItemRequest
    ): Response<OrderItem>
    
    @DELETE("orders/{orderId}/items/{itemId}")
    suspend fun removeOrderItem(
        @Path("orderId") orderId: String,
        @Path("itemId") itemId: String
    ): Response<Unit>
}
