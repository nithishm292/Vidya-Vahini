package com.example.vidyavahini.ui.auth

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.vidyavahini.viewmodel.AuthState
import com.example.vidyavahini.viewmodel.AuthViewModel

@Composable
fun PhoneAuthComponent(viewModel: AuthViewModel) {
    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    val resendTimer by viewModel.resendTimer.collectAsState()
    val activity = LocalContext.current as Activity

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Student Login",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        if (authState is AuthState.OtpSent || authState is AuthState.Loading) {
            // OTP Input
            OutlinedTextField(
                value = otp,
                onValueChange = { if (it.length <= 6) otp = it },
                label = { Text("Enter 6-digit OTP") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                enabled = authState !is AuthState.Loading
            )

            Button(
                onClick = { viewModel.verifyOtp(otp) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = otp.length == 6 && authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("VERIFY OTP")
                }
            }

            TextButton(
                onClick = { viewModel.startPhoneAuth(phoneNumber, activity) },
                enabled = resendTimer == 0 && authState !is AuthState.Loading
            ) {
                Text(if (resendTimer > 0) "Resend OTP in ${resendTimer}s" else "RESEND OTP")
            }
        } else {
            // Phone Number Input
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number (+91...)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )

            Button(
                onClick = { viewModel.startPhoneAuth(phoneNumber, activity) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = phoneNumber.isNotEmpty() && authState !is AuthState.Loading
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("SEND OTP")
                }
            }
        }

        if (authState is AuthState.Error) {
            Text(
                text = (authState as AuthState.Error).message,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp
            )
        }
    }
}
