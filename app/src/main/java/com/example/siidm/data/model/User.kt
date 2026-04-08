package com.example.siidm.data.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: Int = 0,

    @SerializedName("username")
    val username: String = "",

    @SerializedName("email")
    val email: String = "",

    @SerializedName("rol")
    val rol: String = "",       // "admin" o "docente"

    @SerializedName("token")
    val token: String = ""
)

data class LoginRequest(
    @SerializedName("username")
    val username: String,

    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("user")
    val user: User?
)