package com.example.theanimalsarestarving.models

data class User(
    val email: String,
    val name: String,
    var householdId: String,
    val role: String = "normal", // Default role is "normal"
    val fcmToken: String? = null
)
