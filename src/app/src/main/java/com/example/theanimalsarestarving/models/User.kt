package com.example.theanimalsarestarving.models

data class User(
    val email: String,
    val name: String,
    val householdId: String? = null,
    val role: UserRole = UserRole.REGULAR // Default role is "REGULAR"
)
