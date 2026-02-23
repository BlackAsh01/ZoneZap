package com.zonezapapp.data

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f,
    val speed: Float = 0f,
    val heading: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)
