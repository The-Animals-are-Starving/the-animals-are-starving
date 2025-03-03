package com.example.theanimalsarestarving.models

data class User(
    val email: String,
    val name: String,
    var householdId: String,
    val role: UserRole = UserRole.REGULAR // Default role is "REGULAR"
)
