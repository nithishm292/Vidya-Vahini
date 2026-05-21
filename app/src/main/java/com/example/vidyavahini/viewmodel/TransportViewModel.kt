package com.example.vidyavahini.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidyavahini.data.repository.DiscordStorageRepository
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class Ping(
    val id: String = "",
    val user: String = "",
    val location: String = "",
    val timestamp: Long = 0,
    val imageUrl: String? = null,
    val discordMessageId: String? = null,
    val confirmed: Boolean = false
)

class TransportViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val routeId: String = checkNotNull(savedStateHandle["routeId"])
    private val database = Firebase.database.getReference("routes/$routeId")
    private val discordRepository = DiscordStorageRepository()
    
    var landmarks by mutableStateOf(listOf("Rural Gate", "The Bridge", "Forest Cross", "Temple Stop", "City College"))
        private set

    var busNumber by mutableStateOf("...")
        private set

    var imageUrl by mutableStateOf("")
        private set

    var busStatus by mutableStateOf("Waiting for updates...")
    var lastPingTime by mutableStateOf("")
    var isBreakdown by mutableStateOf(false)
    var isLive by mutableStateOf(false)
    var showLocationDialog by mutableStateOf(false)
    var showSnapPrompt by mutableStateOf(false)
    var selectedLocationForPing by mutableStateOf("")
    var currentLocationIndex by mutableIntStateOf(0)

    var recentPings by mutableStateOf<List<Ping>>(emptyList())
        private set

    private var lastPingTimestamp: Long? = null
    private var lastPingLocation: String? = null

    private val historyDatabase = Firebase.database.getReference("routes/$routeId/history")

    val progress: Float
        get() = if (landmarks.size > 1) currentLocationIndex.toFloat() / (landmarks.size - 1) else 0f

    val currentLocation: String
        get() = landmarks.getOrElse(currentLocationIndex) { landmarks.first() }

    private val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            updateFromSnapshot(snapshot)
        }
        override fun onCancelled(error: DatabaseError) {}
    }

    init {
        database.addValueEventListener(listener)
        
        // Listen to recent pings (last 5)
        historyDatabase.orderByChild("timestamp").limitToLast(5).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pings = mutableListOf<Ping>()
                snapshot.children.forEach { child ->
                    val ping = child.getValue(Ping::class.java)?.copy(id = child.key ?: "")
                    if (ping != null) pings.add(ping)
                }
                recentPings = pings.sortedByDescending { it.timestamp }
            }
            override fun onCancelled(error: DatabaseError) {}
        })

        viewModelScope.launch {
            while (true) {
                updateStatusStrings()
                delay(30000) // Update every 30 seconds
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        database.removeEventListener(listener)
    }

    private fun updateFromSnapshot(snapshot: DataSnapshot) {
        val number = snapshot.child("number").getValue(String::class.java) ?: "R-$routeId"
        val from = snapshot.child("from").getValue(String::class.java) ?: "Origin"
        val to = snapshot.child("to").getValue(String::class.java) ?: "Destination"
        val timestamp = snapshot.child("last_ping").getValue(Long::class.java)
        val location = snapshot.child("location").getValue(String::class.java) ?: ""
        val status = snapshot.child("status").getValue(String::class.java) ?: "normal"
        val imgUrl = snapshot.child("imageUrl").getValue(String::class.java) ?: ""
        
        // Fetch stops if available
        val stopsList = mutableListOf<String>()
        snapshot.child("stops").children.forEach {
            it.getValue(String::class.java)?.let { stop -> stopsList.add(stop) }
        }
        
        val allPoints = (listOf(from) + stopsList + listOf(to)).distinct()
        landmarks = allPoints

        busNumber = "Bus $number"
        imageUrl = imgUrl
        isBreakdown = (status == "breakdown")
        lastPingTimestamp = timestamp
        lastPingLocation = location
        currentLocationIndex = landmarks.indexOf(location).coerceAtLeast(0)

        updateStatusStrings()
    }

    private fun updateStatusStrings() {
        val timestamp = lastPingTimestamp
        val location = lastPingLocation ?: ""
        
        if (isBreakdown) {
            isLive = false
            busStatus = "⚠️ BREAKDOWN REPORTED!"
            lastPingTime = "Find alternative transport immediately."
            return
        }

        if (timestamp == null) {
            busStatus = "Waiting for updates..."
            lastPingTime = ""
            return
        }

        val now = System.currentTimeMillis()
        val diffMillis = now - timestamp
        
        if (diffMillis < 10 * 60 * 1000) { // 10 minutes
            // Live state
            isLive = true
            busStatus = "🟢 LIVE: Bus at $location"
            val diffSeconds = diffMillis / 1000
            val diffMinutes = diffSeconds / 60
            
            lastPingTime = if (diffSeconds < 60) {
                "Seen $diffSeconds seconds ago"
            } else {
                "Seen $diffMinutes minutes ago"
            }
        } else {
            // Stale state
            isLive = false
            busStatus = "⚪ STALE: No recent updates"
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            lastPingTime = "Last seen at $location, ${sdf.format(Date(timestamp))}"
        }
    }

    fun reportPing(location: String, userName: String, imageUrl: String? = null, discordMessageId: String? = null, confirmed: Boolean = true) {
        val timestamp = System.currentTimeMillis()
        
        // Update current bus status
        database.child("last_ping").setValue(timestamp)
        database.child("location").setValue(location)
        database.child("status").setValue("normal")
        if (imageUrl != null) {
            database.child("imageUrl").setValue(imageUrl)
            database.child("discordMessageId").setValue(discordMessageId)
        }
        
        // Add to history
        val pingId = historyDatabase.push().key ?: return
        val ping = Ping(
            user = userName,
            location = location,
            timestamp = timestamp,
            imageUrl = imageUrl,
            discordMessageId = discordMessageId,
            confirmed = confirmed
        )
        historyDatabase.child(pingId).setValue(ping)
        
        showLocationDialog = false
        showSnapPrompt = false
        selectedLocationForPing = ""
    }

    fun deletePing(ping: Ping) {
        viewModelScope.launch {
            // Delete from Discord if image exists
            ping.discordMessageId?.let {
                discordRepository.deleteBusImage(it)
            }
            // Delete from Firebase history
            historyDatabase.child(ping.id).removeValue()
        }
    }

    fun reportBreakdown() {
        database.child("status").setValue("breakdown")
    }

    fun resetRoute() {
        database.child("last_ping").setValue(System.currentTimeMillis())
        database.child("location").setValue(landmarks.first())
        database.child("status").setValue("normal")
    }

    fun updateImageUrl(url: String) {
        database.child("imageUrl").setValue(url)
    }
}
