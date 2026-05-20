package com.example.vidyavahini.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.vidyavahini.viewmodel.AuthState
import com.example.vidyavahini.viewmodel.AuthViewModel

@Composable
fun MainAuthScreen(viewModel: AuthViewModel) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    var isAdminLogin by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo Placeholder
            Icon(
                imageVector = Icons.Default.DirectionsBus,
                contentDescription = "App Logo",
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Vidya-Vahini",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "Secure Rural Student Transit Tracker",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (authState is AuthState.Loading) {
                CircularProgressIndicator()
            } else {
                GoogleSignInButton(
                    onClick = { viewModel.signInWithGoogle(context) },
                    isLoading = authState is AuthState.Loading
                )

                Spacer(modifier = Modifier.height(24.dp))

                TextButton(onClick = { isAdminLogin = !isAdminLogin }) {
                    Text(if (isAdminLogin) "Back to Student Login" else "Staff/Admin Login")
                }

                if (isAdminLogin) {
                    Spacer(modifier = Modifier.height(16.dp))
                    EmailAuthComponent(viewModel)
                }
            }

            if (authState is AuthState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
