package com.example.vidyavahini.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidyavahini.data.repository.DiscordStorageRepository
import com.example.vidyavahini.model.BusRoute
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {
    private val database = Firebase.database.getReference("routes")
    private val discordRepository = DiscordStorageRepository()
    
    private val _allRoutes = mutableStateListOf<BusRoute>()
    var searchQuery by mutableStateOf("")

    val filteredRoutes by derivedStateOf {
        if (searchQuery.isEmpty()) {
            _allRoutes
        } else {
            _allRoutes.filter {
                it.number.contains(searchQuery, ignoreCase = true) ||
                it.from.contains(searchQuery, ignoreCase = true) ||
                it.to.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    private val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val newList = mutableListOf<BusRoute>()
            snapshot.children.forEach { child ->
                val id = child.key ?: ""
                val number = child.child("number").getValue(String::class.java) ?: "R-$id"
                val from = child.child("from").getValue(String::class.java) ?: "Origin"
                val to = child.child("to").getValue(String::class.java) ?: "Destination"
                val location = child.child("location").getValue(String::class.java) ?: ""
                val status = child.child("status").getValue(String::class.java) ?: "normal"
                val timestamp = child.child("last_ping").getValue(Long::class.java) 
                    ?: child.child("timestamp").getValue(Long::class.java) 
                    ?: System.currentTimeMillis()
                val imageUrl = child.child("imageUrl").getValue(String::class.java) ?: ""
                val discordMessageId = child.child("discordMessageId").getValue(String::class.java) ?: ""
                
                val stops = mutableListOf<String>()
                child.child("stops").children.forEach {
                    it.getValue(String::class.java)?.let { stop -> stops.add(stop) }
                }

                val allPoints = (listOf(from) + stops + listOf(to)).distinct()
                val progress = when {
                    status == "breakdown" -> 0f
                    allPoints.size > 1 -> {
                        val index = allPoints.indexOf(location).coerceAtLeast(0)
                        index.toFloat() / (allPoints.size - 1)
                    }
                    else -> 0f
                }

                newList.add(BusRoute(
                    id = id,
                    number = number,
                    from = from,
                    to = to,
                    progress = progress,
                    lastSeen = location,
                    status = status,
                    timestamp = timestamp,
                    stops = stops,
                    imageUrl = imageUrl,
                    discordMessageId = discordMessageId
                ))
            }
            _allRoutes.clear()
            _allRoutes.addAll(newList)
        }
        override fun onCancelled(error: DatabaseError) {}
    }

    init {
        database.addValueEventListener(listener)
        // Add dummy data if database is empty for demo purposes
        if (_allRoutes.isEmpty()) {
            _allRoutes.addAll(listOf(
                BusRoute("101", "101", "Rural Gate", "City College", 0.6f, "Temple Stop"),
                BusRoute("102", "102", "North Side", "University", 0.3f, "Forest Cross"),
                BusRoute("103", "103", "East Gate", "Bus Stand", 0.9f, "The Bridge")
            ))
        }
    }

    fun deleteRoute(routeId: String) {
        database.child(routeId).get().addOnSuccessListener { snapshot ->
            val discordMessageId = snapshot.child("discordMessageId").getValue(String::class.java)
            viewModelScope.launch {
                if (!discordMessageId.isNullOrEmpty()) {
                    discordRepository.deleteBusImage(discordMessageId)
                }
                database.child(routeId).removeValue()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        database.removeEventListener(listener)
    }
}
