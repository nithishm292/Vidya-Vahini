package com.example.vidyavahini

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import com.example.vidyavahini.ui.VidyaVahiniNavGraph
import com.example.vidyavahini.ui.theme.VidyaVahiniTheme
import com.example.vidyavahini.utils.AppLanguage
import com.example.vidyavahini.utils.LocalAppLanguage
import com.example.vidyavahini.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var currentLanguage by remember { mutableStateOf(AppLanguage.ENGLISH) }

            VidyaVahiniTheme {
                CompositionLocalProvider(LocalAppLanguage provides currentLanguage) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        VidyaVahiniNavGraph(
                            authViewModel = authViewModel,
                            onLanguageChange = { currentLanguage = it }
                        )
                    }
                }
            }
        }
    }
}
