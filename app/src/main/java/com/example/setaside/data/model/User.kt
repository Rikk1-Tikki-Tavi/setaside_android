package com.example.setaside.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val id: String = "",
    val email: String = "",
    @SerializedName("full_name")
    val fullName: String = "",
    val phone: String? = null,
    val role: String = "customer",
    @SerializedName("created_at")
    val createdAt: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    @SerializedName("full_name")
    val fullName: String,
    val phone: String? = null
)

data class AuthResponse(
    @SerializedName("access_token")
    val accessToken: String,
    val user: User? = null
)

data class UpdateProfileRequest(
    @SerializedName("full_name")
    val fullName: String? = null,
    val phone: String? = null
)
