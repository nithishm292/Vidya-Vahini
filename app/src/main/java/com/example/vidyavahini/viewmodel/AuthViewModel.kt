package com.example.vidyavahini.viewmodel

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidyavahini.model.UserRole
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object OtpSent : AuthState()
    data class Success(val role: UserRole) : AuthState()
    data class Error(val message: String) : AuthState()
}

open class AuthViewModel : ViewModel() {
    protected open val auth: FirebaseAuth? by lazy { 
        try { FirebaseAuth.getInstance() } catch (e: Exception) { null } 
    }
    protected open val database: DatabaseReference? by lazy { 
        try { FirebaseDatabase.getInstance().reference } catch (e: Exception) { null } 
    }

    protected open val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    open val authState: StateFlow<AuthState> = _authState

    var userRole by mutableStateOf(UserRole.NONE)
        protected set

    val userName: String
        get() = auth?.currentUser?.displayName ?: if (userRole == UserRole.ADMIN) "Admin" else "Student"

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private var timerJob: Job? = null
    private val _resendTimer = MutableStateFlow(0)
    val resendTimer: StateFlow<Int> = _resendTimer

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val user = auth?.currentUser
        if (user != null) {
            fetchUserRole(user.uid)
        } else {
            _authState.value = AuthState.Idle
        }
    }

    private fun fetchUserRole(uid: String) {
        _authState.value = AuthState.Loading
        database?.child("users")?.child(uid)?.child("role")?.get()
            ?.addOnSuccessListener { snapshot ->
                val roleStr = snapshot.getValue(String::class.java)
                userRole = when (roleStr?.lowercase()?.trim()) {
                    "admin" -> UserRole.ADMIN
                    "student", "user" -> UserRole.STUDENT
                    else -> {
                        // If role is missing in DB, check sign-in method
                        val isPasswordLogin = auth?.currentUser?.providerData?.any { it.providerId == "password" } == true
                        if (isPasswordLogin) UserRole.ADMIN else UserRole.STUDENT
                    }
                }
                _authState.value = AuthState.Success(userRole)
            }
            ?.addOnFailureListener {
                _authState.value = AuthState.Error("Failed to fetch user role")
            }
    }

    fun startPhoneAuth(phoneNumber: String, activity: Activity) {
        val authInstance = auth ?: return
        _authState.value = AuthState.Loading
        val options = PhoneAuthOptions.newBuilder(authInstance)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    _authState.value = AuthState.Error(e.message ?: "Verification failed")
                }

                override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                    verificationId = id
                    resendToken = token
                    _authState.value = AuthState.OtpSent
                    startResendTimer()
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(otp: String) {
        val id = verificationId ?: return
        val credential = PhoneAuthProvider.getCredential(id, otp)
        signInWithPhoneCredential(credential)
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        _authState.value = AuthState.Loading
        auth?.signInWithCredential(credential)
            ?.addOnSuccessListener { result ->
                result.user?.let { fetchUserRole(it.uid) }
            }
            ?.addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Invalid OTP")
            }
    }

    fun adminLogin(email: String, pass: String) {
        _authState.value = AuthState.Loading
        auth?.signInWithEmailAndPassword(email, pass)
            ?.addOnSuccessListener { result ->
                result.user?.let { fetchUserRole(it.uid) }
            }
            ?.addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Login failed")
            }
    }

    fun signInWithGoogle(context: Context) {
        val webClientId = "246286893500-irbiai1dria9ahhj1bmkdfalc8m6fk31.apps.googleusercontent.com"
        
        Log.i("AUTH_HANDSHAKE_FAILED", "Starting Google Sign-In handshake")
        Log.i("AUTH_HANDSHAKE_FAILED", "Web Client ID Prefix: ${webClientId.take(12)}... (Total Length: ${webClientId.length})")

        // Dynamic Keystore Reflection for diagnostic purposes
        logAppSignature(context)

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credentialManager = CredentialManager.create(context)

                // 1. Try with GetGoogleIdOption (Primary path - looks for authorized accounts)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(true)
                    .setServerClientId(webClientId)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                try {
                    val result = credentialManager.getCredential(
                        context = context,
                        request = request
                    )
                    handleGoogleCredential(result.credential)
                } catch (e: GetCredentialException) {
                    Log.e("AUTH_HANDSHAKE_FAILED", "Primary path GetCredentialException: Type=${e.type}, Message=${e.message}")
                    
                    if (e is NoCredentialException || e.message?.contains("No credentials available", ignoreCase = true) == true) {
                        Log.w("AUTH_HANDSHAKE_FAILED", "No existing credentials found. Attempting fallback with GetSignInWithGoogleOption.")
                        
                        // 2. Fallback to GetSignInWithGoogleOption
                        val fallbackOption = GetSignInWithGoogleOption.Builder(webClientId).build()
                        val fallbackRequest = GetCredentialRequest.Builder()
                            .addCredentialOption(fallbackOption)
                            .build()

                        try {
                            val result = credentialManager.getCredential(
                                context = context,
                                request = fallbackRequest
                            )
                            handleGoogleCredential(result.credential)
                        } catch (fallbackError: GetCredentialException) {
                            Log.e("AUTH_HANDSHAKE_FAILED", "Fallback GetCredentialException: Type=${fallbackError.type}, Message=${fallbackError.message}")
                            handleAuthError(fallbackError)
                        }
                    } else {
                        handleAuthError(e)
                    }
                }
            } catch (e: Exception) {
                Log.e("AUTH_HANDSHAKE_FAILED", "Unexpected Error during Google Sign-In", e)
                _authState.value = AuthState.Error("An unexpected error occurred. Please try again.")
            }
        }
    }

    private fun handleAuthError(e: GetCredentialException) {
        // Handle User Cancellation or Error 16 (Signature Mismatch) gracefully
        if (e.type == "android.credentials.GetCredentialException.TYPE_USER_CANCELED" || e.message?.contains("16") == true) {
            Log.w("AUTH_HANDSHAKE_FAILED", "Authentication cancelled or signature mismatch (Error 16 detected). Resetting state.")
            _authState.value = AuthState.Idle
            return
        }

        val errorMessage = when {
            e.message?.contains("10") == true || e.message?.contains("developer", ignoreCase = true) == true -> {
                Log.e("AUTH_HANDSHAKE_FAILED", "Developer Error (10) / Reauth failed. Check SHA-1 and Client ID in Firebase Console.")
                "Server validation failed. Please check your developer configuration."
            }
            e is NoCredentialException -> "No Google account selected."
            else -> e.message ?: "Google Sign-In failed"
        }
        _authState.value = AuthState.Error(errorMessage)
    }

    private suspend fun handleGoogleCredential(credential: androidx.credentials.Credential) {
        // This block extracts the token correctly from BOTH the modern and fallback API options
        val idToken = when (credential.type) {
            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                try {
                    GoogleIdTokenCredential.createFrom(credential.data).idToken
                } catch (e: Exception) {
                    Log.e("AUTH_HANDSHAKE_FAILED", "Failed to create GoogleIdTokenCredential from data", e)
                    credential.data.getString("androidx.credentials.BUNDLE_KEY_ID_TOKEN")
                }
            }
            else -> {
                // Fallback extractor for standard custom credential responses
                credential.data.getString("androidx.credentials.BUNDLE_KEY_ID_TOKEN")
            }
        }

        if (!idToken.isNullOrEmpty()) {
            val googleCredential = GoogleAuthProvider.getCredential(idToken, null)
            try {
                val authResult = auth?.signInWithCredential(googleCredential)?.await()
                Log.i("AUTH_HANDSHAKE_FAILED", "Firebase Auth successful for UID: ${authResult?.user?.uid}")
                authResult?.user?.let { fetchUserRole(it.uid) }
            } catch (e: Exception) {
                Log.e("AUTH_HANDSHAKE_FAILED", "Firebase Auth Sign-In failed with ID Token", e)
                _authState.value = AuthState.Error("Server validation failed. Please check your network or developer configuration.")
            }
        } else {
            Log.e("AUTH_HANDSHAKE_FAILED", "Security token was empty or null. Type: ${credential.type}")
            _authState.value = AuthState.Error("Security token was empty or null.")
        }
    }

    private fun startResendTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            _resendTimer.value = 60
            while (_resendTimer.value > 0) {
                delay(1000)
                _resendTimer.value -= 1
            }
        }
    }

    fun signOut() {
        auth?.signOut()
        userRole = UserRole.NONE
        _authState.value = AuthState.Idle
    }

    /**
     * Dynamically reflects the application's signing certificate to diagnose signature mismatches.
     * Output is sent to Logcat under the tag "CRITICAL_APP_SIGNATURE".
     */
    private fun logAppSignature(context: Context) {
        try {
            val packageName = context.packageName
            val packageManager = context.packageManager
            val sha1 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                val signingInfo = packageInfo.signingInfo
                if (signingInfo?.hasMultipleSigners() == true) {
                    signingInfo.apkContentsSigners.firstOrNull()
                } else {
                    signingInfo?.signingCertificateHistory?.firstOrNull()
                }
            } else {
                @Suppress("DEPRECATION")
                val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
                @Suppress("DEPRECATION")
                packageInfo.signatures?.firstOrNull()
            }?.let { signature ->
                val md = MessageDigest.getInstance("SHA-1")
                val digest = md.digest(signature.toByteArray())
                digest.joinToString(":") { "%02X".format(it) }
            } ?: "NOT_FOUND"

            Log.e("CRITICAL_APP_SIGNATURE", "================================================================")
            Log.e("CRITICAL_APP_SIGNATURE", "ACTIVE RUNTIME SHA-1: $sha1")
            Log.e("CRITICAL_APP_SIGNATURE", "INSTRUCTION: Add this exact SHA-1 to your Firebase Console settings")
            Log.e("CRITICAL_APP_SIGNATURE", "to resolve Google Sign-In Error 16 / Account reauth failed.")
            Log.e("CRITICAL_APP_SIGNATURE", "================================================================")
        } catch (e: Exception) {
            Log.e("CRITICAL_APP_SIGNATURE", "Failed to compute runtime signature", e)
        }
    }
}
