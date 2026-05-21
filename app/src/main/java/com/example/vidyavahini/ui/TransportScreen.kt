package com.example.vidyavahini.ui

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.vidyavahini.data.repository.DiscordStorageRepository
import com.example.vidyavahini.model.UserRole
import com.example.vidyavahini.utils.AppLanguage
import com.example.vidyavahini.utils.AppStrings
import com.example.vidyavahini.utils.LocalAppLanguage
import com.example.vidyavahini.utils.TranslatorManager
import com.example.vidyavahini.viewmodel.Ping
import com.example.vidyavahini.viewmodel.TransportViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransportScreen(
    userName: String,
    userRole: UserRole,
    onSignOut: () -> Unit,
    onLanguageChange: (AppLanguage) -> Unit,
    viewModel: TransportViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val discordRepository = remember { DiscordStorageRepository() }
    var isUploading by remember { mutableStateOf(false) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    val currentLanguage = LocalAppLanguage.current

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempUri?.let { uri ->
                isUploading = true
                scope.launch {
                    val uploadResult = discordRepository.uploadBusImage(context, uri)
                    isUploading = false
                    if (uploadResult != null) {
                        viewModel.reportPing(viewModel.selectedLocationForPing, userName, uploadResult.url, uploadResult.messageId, true)
                    }
                }
            }
        }
    }

    if (viewModel.showLocationDialog) {
        LocationSelectionDialog(
            landmarks = viewModel.landmarks,
            onLocationSelected = { location ->
                viewModel.selectedLocationForPing = location
                viewModel.showLocationDialog = false
                viewModel.showSnapPrompt = true
            },
            onDismiss = { viewModel.showLocationDialog = false }
        )
    }

    if (viewModel.showSnapPrompt) {
        AlertDialog(
            onDismissRequest = { viewModel.showSnapPrompt = false },
            title = { Text(AppStrings.takeSnapTitle(currentLanguage)) },
            text = { Text(AppStrings.takeSnapText(currentLanguage)) },
            confirmButton = {
                Button(
                    onClick = {
                        val values = ContentValues().apply {
                            put(MediaStore.Images.Media.TITLE, "Bus Ping")
                        }
                        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                        tempUri = uri
                        if (uri != null) {
                            cameraLauncher.launch(uri)
                        }
                    },
                    enabled = !isUploading
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isUploading) "Uploading..." else AppStrings.yesTakePhoto(currentLanguage))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.reportPing(viewModel.selectedLocationForPing, userName, confirmed = false)
                }) {
                    Text(AppStrings.noJustReport(currentLanguage))
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = AppStrings.appTitle(currentLanguage),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = AppStrings.loggedInAs(currentLanguage, userRole.name.lowercase()),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Language Toggle
                    IconButton(onClick = {
                        onLanguageChange(if (currentLanguage == AppLanguage.ENGLISH) AppLanguage.KANNADA else AppLanguage.ENGLISH)
                    }) {
                        Text(
                            text = if (currentLanguage == AppLanguage.ENGLISH) "KN" else "EN",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    TextButton(onClick = onSignOut) {
                        Text(AppStrings.logOut(currentLanguage))
                    }
                }
            }
        }

        item {
            RouteStatusCard(
                busNumber = viewModel.busNumber,
                isBreakdown = viewModel.isBreakdown,
                isLive = viewModel.isLive,
                progress = viewModel.progress,
                currentLocation = viewModel.currentLocation,
                busStatus = viewModel.busStatus,
                lastPingTime = viewModel.lastPingTime,
                imageUrl = viewModel.imageUrl
            )
        }

        item {
            ActionButtons(
                onPingClick = { viewModel.showLocationDialog = true },
                onBreakdownClick = { viewModel.reportBreakdown() },
                onSafeReachClick = { sendSafeReachSms(context) }
            )
        }

        if (userRole == UserRole.ADMIN) {
            item {
                OutlinedButton(
                    onClick = { viewModel.resetRoute() },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Text(AppStrings.resetRouteAdmin(currentLanguage), fontSize = 12.sp)
                }
            }
        }

        item {
            RecentPingsSection(
                pings = viewModel.recentPings,
                userRole = userRole,
                onDeletePing = { viewModel.deletePing(it) }
            )
        }
    }
}

@Composable
fun RecentPingsSection(
    pings: List<Ping>,
    userRole: UserRole,
    onDeletePing: (Ping) -> Unit
) {
    val currentLanguage = LocalAppLanguage.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text(
                text = AppStrings.recentPings(currentLanguage),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (pings.isEmpty()) {
            Text(
                text = AppStrings.noPingsYet(currentLanguage),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        pings.forEach { ping ->
            PingCard(
                ping = ping,
                userRole = userRole,
                onDelete = { onDeletePing(ping) }
            )
        }
    }
}

@Composable
fun PingCard(ping: Ping, userRole: UserRole, onDelete: () -> Unit) {
    val sdf = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val currentLanguage = LocalAppLanguage.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Column {
                        Text(
                            text = ping.user,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = sdf.format(Date(ping.timestamp)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (ping.confirmed) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Confirmed",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                AppStrings.confirmed(currentLanguage),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    } else {
                        Text(
                            AppStrings.unconfirmed(currentLanguage),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    if (userRole == UserRole.ADMIN) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Ping", tint = Color.Red, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            val translatedLocation = remember(ping.location, currentLanguage) {
                if (currentLanguage == AppLanguage.KANNADA) {
                    TranslatorManager.translateEnglishToKannada(ping.location)
                } else {
                    ping.location
                }
            }
            Text(
                text = "📍 $translatedLocation",
                style = MaterialTheme.typography.bodyMedium
            )

            if (ping.imageUrl != null) {
                Spacer(modifier = Modifier.height(8.dp))
                AsyncImage(
                    model = ping.imageUrl,
                    contentDescription = "Ping Photo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun RouteStatusCard(
    busNumber: String,
    isBreakdown: Boolean,
    isLive: Boolean,
    progress: Float,
    currentLocation: String,
    busStatus: String,
    lastPingTime: String,
    imageUrl: String = ""
) {
    val currentLanguage = LocalAppLanguage.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isBreakdown) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column {
            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Bus Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = busNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))
                
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = when {
                        isBreakdown -> Color.Red
                        isLive -> Color(0xFF4CAF50)
                        else -> Color.Gray
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                val translatedCurrentLocation = remember(currentLocation, currentLanguage) {
                    if (currentLanguage == AppLanguage.KANNADA) {
                        TranslatorManager.translateEnglishToKannada(currentLocation)
                    } else {
                        currentLocation
                    }
                }
                Text(text = "${AppStrings.current(currentLanguage)}: $translatedCurrentLocation", fontSize = 14.sp)
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                val translatedBusStatus = remember(busStatus, currentLanguage) {
                    if (currentLanguage == AppLanguage.KANNADA) {
                        // busStatus is usually "normal", "breakdown", or some time string
                        if (busStatus.lowercase() == "breakdown") {
                            AppStrings.breakdown(currentLanguage)
                        } else if (busStatus.lowercase() == "normal") {
                            "ಸಾಮಾನ್ಯ"
                        } else {
                            // It might be a time string from LiveStatusTicker
                            busStatus
                        }
                    } else {
                        busStatus
                    }
                }
                Text(
                    text = translatedBusStatus,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isBreakdown -> Color.Red
                        isLive -> Color(0xFF4CAF50)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = lastPingTime,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun ActionButtons(
    onPingClick: () -> Unit,
    onBreakdownClick: () -> Unit,
    onSafeReachClick: () -> Unit
) {
    val currentLanguage = LocalAppLanguage.current
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onPingClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text(AppStrings.iSeeTheBus(currentLanguage), fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = onBreakdownClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            border = BorderStroke(2.dp, Color.Red),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
        ) {
            Text(AppStrings.reportBreakdown(currentLanguage), fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = onSafeReachClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text(AppStrings.notifySafeReach(currentLanguage), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LocationSelectionDialog(
    landmarks: List<String>,
    onLocationSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val currentLanguage = LocalAppLanguage.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(AppStrings.whereIsTheBus(currentLanguage), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                landmarks.forEach { location ->
                    val translatedLocation = if (currentLanguage == AppLanguage.KANNADA) {
                        TranslatorManager.translateEnglishToKannada(location)
                    } else {
                        location
                    }
                    TextButton(
                        onClick = { onLocationSelected(location) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(translatedLocation, fontSize = 18.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(AppStrings.cancel(currentLanguage))
            }
        }
    )
}

private fun sendSafeReachSms(context: Context) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("smsto:")
        putExtra("sms_body", "I have reached the college safely! - Vidya-Vahini")
    }
    context.startActivity(intent)
}
