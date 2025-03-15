package com.example.theanimalsarestarving.models

//TODO: change the names in the backend to householdId and householdName
data class Household(
    val _id: String,
    val name: String,
    val managerId: String,
    val pets: List<Pet>,
    val users: List<String>
)
