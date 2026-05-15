package com.example.vidyavahini.viewmodel

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vidyavahini.model.UserRole
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object OtpSent : AuthState()
    data class Success(val role: UserRole) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    var userRole by mutableStateOf(UserRole.NONE)
        private set

    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private var timerJob: Job? = null
    private val _resendTimer = MutableStateFlow(0)
    val resendTimer: StateFlow<Int> = _resendTimer

    init {
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        val user = auth.currentUser
        if (user != null) {
            fetchUserRole(user.uid)
        } else {
            _authState.value = AuthState.Idle
        }
    }

    private fun fetchUserRole(uid: String) {
        _authState.value = AuthState.Loading
        database.child("users").child(uid).child("role").get()
            .addOnSuccessListener { snapshot ->
                val roleStr = snapshot.getValue(String::class.java)
                userRole = when (roleStr?.lowercase()?.trim()) {
                    "admin" -> UserRole.ADMIN
                    "student", "user" -> UserRole.STUDENT
                    else -> {
                        // If role is missing but logged in via email, maybe it's an admin?
                        // For now, let's just stick to the database or default to STUDENT if not found
                        if (auth.currentUser?.email != null) UserRole.ADMIN else UserRole.STUDENT
                    }
                }
                _authState.value = AuthState.Success(userRole)
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error("Failed to fetch user role")
            }
    }

    fun startPhoneAuth(phoneNumber: String, activity: Activity) {
        _authState.value = AuthState.Loading
        val options = PhoneAuthOptions.newBuilder(auth)
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
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                result.user?.let { fetchUserRole(it.uid) }
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Invalid OTP")
            }
    }

    fun adminLogin(email: String, pass: String) {
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                result.user?.let { fetchUserRole(it.uid) }
            }
            .addOnFailureListener {
                _authState.value = AuthState.Error(it.message ?: "Login failed")
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
        auth.signOut()
        userRole = UserRole.NONE
        _authState.value = AuthState.Idle
    }
}
