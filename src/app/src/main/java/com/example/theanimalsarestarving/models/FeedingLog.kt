package com.example.theanimalsarestarving.models

data class FeedingLog(
    val householdId: String,
    val petId: Pet,
    val userId: User,
    val timestamp: String
)
