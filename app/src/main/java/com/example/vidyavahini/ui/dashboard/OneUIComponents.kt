package com.example.vidyavahini.ui.dashboard

import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vidyavahini.data.repository.DiscordStorageRepository
import com.example.vidyavahini.model.BusRoute
import com.example.vidyavahini.utils.AppLanguage
import com.example.vidyavahini.utils.AppStrings
import com.example.vidyavahini.utils.LocalAppLanguage
import com.example.vidyavahini.utils.TranslatorManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LiveStatusTicker(lastPingTimestamp: Long) {
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val currentLanguage = LocalAppLanguage.current

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            currentTime = System.currentTimeMillis()
        }
    }

    val diffSeconds = (currentTime - lastPingTimestamp) / 1000
    val diffMinutes = diffSeconds / 60

    val (color, text) = when {
        diffMinutes >= 10 -> Color.Gray to AppStrings.statusStale(currentLanguage)
        else -> Color(0xFF4CAF50) to formatTickerTime(diffSeconds, currentLanguage) // Green
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (diffMinutes < 10) {
            PulseIndicator(color)
        }
        
        AnimatedContent(
            targetState = text,
            transitionSpec = {
                (slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialOffsetY = { it }
                ) + fadeIn()).togetherWith(
                    slideOutVertically(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        targetOffsetY = { -it }
                    ) + fadeOut()
                )
            },
            label = "LiveTickerAnimation"
        ) { targetText ->
            LiveTickerText(targetText)
        }
    }
}

private fun formatTickerTime(seconds: Long, lang: AppLanguage): String {
    return when {
        seconds < 60 -> AppStrings.live(lang)
        else -> AppStrings.minutesAgo(lang, seconds / 60)
    }
}

@Composable
private fun LiveTickerText(text: String) {
    val currentLanguage = LocalAppLanguage.current
    val isStatusStale = text == AppStrings.statusStale(currentLanguage)

    if (text.contains(" ") && !isStatusStale) {
        // Handle cases like "Live 5m ago" or "ಲೈವ್ 5ನಿಮಿಷದ ಹಿಂದೆ"
        // This is a bit tricky with different languages, so let's just display it simply
        // if we want complex styling we might need more logic.
        Text(
            text = text,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    } else {
        Text(
            text = text,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = if (isStatusStale) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PulseIndicator(color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Box(
        modifier = Modifier.size(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .scale(scale)
                .background(color.copy(alpha = alpha), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
    }
}

@Composable
fun BusRouteCard(
    route: BusRoute,
    onClick: () -> Unit,
    onDeleteClick: (() -> Unit)? = null
) {
    val currentLanguage = LocalAppLanguage.current
    var translatedRoute by remember(route.from, route.to, currentLanguage) {
        mutableStateOf("${route.from} ➔ ${route.to}")
    }

    LaunchedEffect(currentLanguage, route.from, route.to) {
        if (currentLanguage == AppLanguage.KANNADA) {
            val from = TranslatorManager.translateEnglishToKannada(route.from)
            val to = TranslatorManager.translateEnglishToKannada(route.to)
            translatedRoute = "$from ➔ $to"
        } else {
            translatedRoute = "${route.from} ➔ ${route.to}"
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${AppStrings.bus(currentLanguage)} ${route.number}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (route.status == "breakdown") {
                        Text(
                            text = "⚠️ ${AppStrings.breakdown(currentLanguage)}",
                            fontSize = 12.sp,
                            color = Color.Red,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    
                    if (onDeleteClick != null) {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Route",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = translatedRoute,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { route.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = if (route.status == "breakdown") Color.Red else Color(0xFF4CAF50),
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val translatedLastSeen = remember(route.lastSeen, currentLanguage) {
                    if (currentLanguage == AppLanguage.KANNADA) {
                        TranslatorManager.translateEnglishToKannada(route.lastSeen)
                    } else {
                        route.lastSeen
                    }
                }
                Text(
                    text = "${AppStrings.lastSeen(currentLanguage)}: $translatedLastSeen",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                LiveStatusTicker(lastPingTimestamp = route.timestamp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusProofUploadSection(busId: String, onUrlGenerated: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val discordRepository = remember { DiscordStorageRepository() }
    var isUploading by remember { mutableStateOf(false) }
    var showSheet by remember { mutableStateOf(false) }
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    val sheetState = rememberModalBottomSheetState()
    val currentLanguage = LocalAppLanguage.current

    // ... (galleryLauncher and cameraLauncher stay same)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            isUploading = true
            scope.launch {
                val permanentCdnUrl = discordRepository.uploadBusImage(context, selectedUri)
                isUploading = false
                
                if (permanentCdnUrl != null) {
                    onUrlGenerated(permanentCdnUrl.url)
                }
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempUri?.let { uri ->
                isUploading = true
                scope.launch {
                    val permanentCdnUrl = discordRepository.uploadBusImage(context, uri)
                    isUploading = false
                    
                    if (permanentCdnUrl != null) {
                        onUrlGenerated(permanentCdnUrl.url)
                    }
                }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = AppStrings.addBusPhoto(currentLanguage),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(16.dp)
                )
                
                Card(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            showSheet = false
                            val values = ContentValues().apply {
                                put(MediaStore.Images.Media.TITLE, "Bus Photo")
                                put(MediaStore.Images.Media.DESCRIPTION, "From Camera")
                            }
                            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                            tempUri = uri
                            if (uri != null) {
                                cameraLauncher.launch(uri)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(AppStrings.openCamera(currentLanguage), style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Card(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            showSheet = false
                            galleryLauncher.launch("image/*")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(AppStrings.chooseFromGallery(currentLanguage), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Button(
            onClick = { showSheet = true },
            enabled = !isUploading,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(if (isUploading) AppStrings.uploadingPhoto(currentLanguage) else AppStrings.addBusPhoto(currentLanguage))
        }

        if (isUploading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun EmptyState() {
    val currentLanguage = LocalAppLanguage.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Search,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.LightGray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = AppStrings.noRoutesFound(currentLanguage),
            fontSize = 18.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
    }
}
