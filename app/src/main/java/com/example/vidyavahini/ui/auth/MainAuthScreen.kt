package com.example.vidyavahini.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vidyavahini.viewmodel.AuthViewModel

@Composable
fun MainAuthScreen(viewModel: AuthViewModel) {
    var isAdminLogin by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "Vidya-Vahini",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Rural Student Transit Tracker",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(48.dp))

        TabRow(selectedTabIndex = if (isAdminLogin) 1 else 0) {
            Tab(
                selected = !isAdminLogin,
                onClick = { isAdminLogin = false },
                text = { Text("Student") }
            )
            Tab(
                selected = isAdminLogin,
                onClick = { isAdminLogin = true },
                text = { Text("Admin") }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isAdminLogin) {
            EmailAuthComponent(viewModel)
        } else {
            PhoneAuthComponent(viewModel)
        }
    }
}
