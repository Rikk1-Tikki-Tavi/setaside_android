package com.example.setaside.data.repository

import com.example.setaside.data.model.*
import com.example.setaside.data.remote.ApiService

class OrderRepository(private val apiService: ApiService) {
    
    suspend fun createOrder(request: CreateOrderRequest): Result<Order> {
        return try {
            val response = apiService.createOrder(request)
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Failed to create order", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun getOrders(
        page: Int = 1,
        limit: Int = 20,
        status: String? = null,
        customerId: String? = null
    ): Result<OrdersResponse> {
        return try {
            val response = apiService.getOrders(page, limit, status, customerId)
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Failed to get orders", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun getOrder(id: String): Result<Order> {
        return try {
            val response = apiService.getOrder(id)
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Failed to get order", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun updateOrderStatus(orderId: String, status: String): Result<Order> {
        return try {
            val response = apiService.updateOrderStatus(orderId, UpdateOrderStatusRequest(status))
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Failed to update order status", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun deleteOrder(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteOrder(id)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("Failed to delete order", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun addOrderItem(orderId: String, request: AddOrderItemRequest): Result<OrderItem> {
        return try {
            val response = apiService.addOrderItem(orderId, request)
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Failed to add item to order", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun updateOrderItem(
        orderId: String,
        itemId: String,
        request: UpdateOrderItemRequest
    ): Result<OrderItem> {
        return try {
            val response = apiService.updateOrderItem(orderId, itemId, request)
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Failed to update order item", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun removeOrderItem(orderId: String, itemId: String): Result<Unit> {
        return try {
            val response = apiService.removeOrderItem(orderId, itemId)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("Failed to remove item from order", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
}
