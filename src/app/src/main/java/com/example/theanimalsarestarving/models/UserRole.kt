package com.example.theanimalsarestarving.models

enum class UserRole(val roleName: String) {
    RESTRICTED("restricted"),  // Backend 'restricted' -> Frontend 'RESTRICTED'
    REGULAR("normal"),         // Backend 'normal' -> Frontend 'REGULAR'
    ADMIN("manager");          // Backend 'manager' -> Frontend 'ADMIN'

    companion object {
        // Translate the role from the backend value to the corresponding enum value
        fun fromBackendRole(backendRole: String): UserRole {
            return when (backendRole) {
                "restricted" -> RESTRICTED
                "normal" -> REGULAR
                "manager" -> ADMIN
                else -> throw IllegalArgumentException("Unknown backend role: $backendRole")
            }
        }
    }
}
