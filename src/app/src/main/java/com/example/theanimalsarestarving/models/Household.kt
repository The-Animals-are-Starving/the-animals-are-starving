package com.example.theanimalsarestarving.models

data class Household(
    val householdId: Int,
    val managerId: String,
    val pets: List<Pet>,
    val users: List<User>
)
