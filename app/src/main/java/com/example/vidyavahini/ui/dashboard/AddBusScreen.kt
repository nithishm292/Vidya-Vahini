package com.example.vidyavahini.ui.dashboard

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.vidyavahini.model.BusRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.database

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBusScreen(onBack: () -> Unit) {
    var busNumber by remember { mutableStateOf("") }
    var fromStation by remember { mutableStateOf("") }
    var toStation by remember { mutableStateOf("") }
    var stops by remember { mutableStateOf(mutableStateListOf<String>()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request New Bus", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Text("Bus Details", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            // Photo Picker
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { launcher.launch("image/*") }
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Add Bus Photo")
                        }
                    }
                }
            }

            item {
                OneUIField(value = busNumber, onValueChange = { busNumber = it }, label = "Bus Number")
            }
            item {
                OneUIField(value = fromStation, onValueChange = { fromStation = it }, label = "From Station")
            }
            item {
                OneUIField(value = toStation, onValueChange = { toStation = it }, label = "To Station")
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Stops", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { stops.add("") }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("Add Stop")
                    }
                }
            }

            itemsIndexed(stops) { index, stop ->
                StopItem(
                    name = stop,
                    onNameChange = { stops[index] = it },
                    onDelete = { stops.removeAt(index) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        submitRequest(busNumber, fromStation, toStation, stops.toList(), imageUri, onBack)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Send Request to Admin", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun OneUIField(value: String, onValueChange: (String) -> Unit, label: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    )
}

@Composable
fun StopItem(name: String, onNameChange: (String) -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            placeholder = { Text("Stop name") },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp)
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
        }
    }
}

private fun submitRequest(number: String, from: String, to: String, stops: List<String>, imageUri: Uri?, onSuccess: () -> Unit) {
    val database = Firebase.database.getReference("bus_requests")
    val id = database.push().key ?: return
    val userId = Firebase.auth.currentUser?.uid ?: "anonymous"
    
    val request = BusRequest(
        id = id,
        requesterId = userId,
        busNumber = number,
        from = from,
        to = to,
        stops = stops,
        imageUrl = imageUri?.toString() ?: "",
        status = "pending"
    )
    
    database.child(id).setValue(request).addOnSuccessListener {
        onSuccess()
    }
}
