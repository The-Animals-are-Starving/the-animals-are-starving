package com.example.theanimalsarestarving.models

import org.bson.types.ObjectId
import java.util.Date

data class Pet(
    val petId: Int,
    val name: String,
    val householdId: String? = null,
    val feedingTime: Date,
    val fed: Boolean = false
)