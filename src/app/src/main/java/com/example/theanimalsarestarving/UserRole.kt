package com.example.theanimalsarestarving

enum class UserRole {
    ADMIN, REGULAR, RESTRICTED;

    // You can add extra methods or properties to the enum if needed
    fun getRoleDescription(): String {
        return when (this) {
            ADMIN -> "Administrator with full privileges"
            REGULAR -> "Regular user with limited access"
            RESTRICTED -> "Guest with restricted access"
        }
    }
}