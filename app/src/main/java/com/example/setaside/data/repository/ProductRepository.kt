package com.example.setaside.data.repository

import com.example.setaside.data.model.*
import com.example.setaside.data.remote.ApiService

class ProductRepository(private val apiService: ApiService) {
    
    suspend fun getProducts(
        page: Int = 1,
        limit: Int = 20,
        category: String? = null,
        isAvailable: Boolean? = true,
        search: String? = null
    ): Result<ProductsResponse> {
        return try {
            val response = apiService.getProducts(page, limit, category, isAvailable, search)
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Failed to get products", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun getCategories(): Result<List<String>> {
        return try {
            val response = apiService.getCategories()
            if (response.isSuccessful) {
                Result.Success(response.body()?.categories ?: emptyList())
            } else {
                Result.Error("Failed to get categories", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun getProduct(id: String): Result<Product> {
        return try {
            val response = apiService.getProduct(id)
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Failed to get product", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun createProduct(request: CreateProductRequest): Result<Product> {
        return try {
            val response = apiService.createProduct(request)
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Failed to create product", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun updateProduct(id: String, updates: Map<String, Any>): Result<Product> {
        return try {
            val response = apiService.updateProduct(id, updates)
            if (response.isSuccessful) {
                Result.Success(response.body()!!)
            } else {
                Result.Error("Failed to update product", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
    
    suspend fun deleteProduct(id: String): Result<Unit> {
        return try {
            val response = apiService.deleteProduct(id)
            if (response.isSuccessful) {
                Result.Success(Unit)
            } else {
                Result.Error("Failed to delete product", response.code())
            }
        } catch (e: Exception) {
            Result.Error(e.message ?: "Unknown error")
        }
    }
}
