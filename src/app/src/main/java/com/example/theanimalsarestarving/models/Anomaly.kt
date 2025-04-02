package com.example.theanimalsarestarving.models

data class FeedingAnomalyResponse(
        val anomalies: List<Anomaly>
)

data class Anomaly(
        val pet: String,
        val largeDeviation: Boolean,
        val significantlyLate: Boolean,
        val averageAmount: Double,
        val feedingCount: Int
)
