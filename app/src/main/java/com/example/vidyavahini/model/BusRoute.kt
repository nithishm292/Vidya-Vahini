package com.example.vidyavahini.model

data class BusRoute(
    val id: String = "",
    val number: String = "",
    val from: String = "",
    val to: String = "",
    val progress: Float = 0f,
    val lastSeen: String = "",
    val status: String = "normal",
    val timestamp: Long = System.currentTimeMillis(),
    val stops: List<String> = emptyList(),
    val imageUrl: String = "",
    val discordMessageId: String = ""
)

data class BusRequest(
    val id: String = "",
    val requesterId: String = "",
    val requesterName: String = "Unknown User",
    val busNumber: String = "",
    val from: String = "",
    val to: String = "",
    val stops: List<String> = emptyList(),
    val imageUrl: String = "",
    val discordMessageId: String = "",
    val status: String = "pending" // pending, approved, rejected
)
