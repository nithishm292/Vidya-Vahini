package com.example.vidyavahini.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vidyavahini.model.UserRole
import com.example.vidyavahini.viewmodel.TransportViewModel

@Composable
fun TransportScreen(
    userRole: UserRole,
    onSignOut: () -> Unit,
    viewModel: TransportViewModel = viewModel()
) {
    val context = LocalContext.current

    if (viewModel.showLocationDialog) {
        LocationSelectionDialog(
            landmarks = viewModel.landmarks,
            onLocationSelected = { viewModel.reportPing(it) },
            onDismiss = { viewModel.showLocationDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Vidya-Vahini",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Logged in as ${userRole.name.lowercase()}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            TextButton(onClick = onSignOut) {
                Text("Logout")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        RouteStatusCard(
            busNumber = viewModel.busNumber,
            isBreakdown = viewModel.isBreakdown,
            isLive = viewModel.isLive,
            progress = viewModel.progress,
            currentLocation = viewModel.currentLocation,
            busStatus = viewModel.busStatus,
            lastPingTime = viewModel.lastPingTime
        )

        Spacer(modifier = Modifier.height(24.dp))

        ActionButtons(
            onPingClick = { viewModel.showLocationDialog = true },
            onBreakdownClick = { viewModel.reportBreakdown() },
            onSafeReachClick = { sendSafeReachSms(context) }
        )

        if (userRole == UserRole.ADMIN) {
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = { viewModel.resetRoute() },
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Text("RESET ROUTE (ADMIN)", fontSize = 12.sp)
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
    lastPingTime: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isBreakdown) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = busNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = when {
                    isBreakdown -> Color.Red
                    isLive -> Color(0xFF4CAF50)
                    else -> Color.Gray
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Current: $currentLocation", fontSize = 14.sp)
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            
            Text(
                text = busStatus,
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

@Composable
fun ActionButtons(
    onPingClick: () -> Unit,
    onBreakdownClick: () -> Unit,
    onSafeReachClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(
            onClick = onPingClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            Text("I SEE THE BUS!", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = onBreakdownClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            border = BorderStroke(2.dp, Color.Red),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
        ) {
            Text("REPORT BREAKDOWN", fontWeight = FontWeight.Bold)
        }

        Button(
            onClick = onSafeReachClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("NOTIFY SAFE REACH 🏠", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LocationSelectionDialog(
    landmarks: List<String>,
    onLocationSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Where is the bus?", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                landmarks.forEach { location ->
                    TextButton(
                        onClick = { onLocationSelected(location) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(location, fontSize = 18.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
