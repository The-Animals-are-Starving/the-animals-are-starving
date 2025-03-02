package com.example.theanimalsarestarving.models

import org.bson.types.ObjectId
import java.util.Date

data class Pet(
    val petId: String,
    val name: String,
    val householdId: String? = null,
    val feedingTime: String,
    var fed: Boolean = false
)