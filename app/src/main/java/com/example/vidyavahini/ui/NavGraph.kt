package com.example.vidyavahini.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.vidyavahini.model.UserRole
import com.example.vidyavahini.ui.auth.MainAuthScreen
import com.example.vidyavahini.ui.dashboard.AddBusScreen
import com.example.vidyavahini.ui.dashboard.AdminRequestsScreen
import com.example.vidyavahini.ui.dashboard.DashboardScreen
import com.example.vidyavahini.viewmodel.AuthState
import com.example.vidyavahini.viewmodel.AuthViewModel

@Composable
fun VidyaVahiniNavGraph(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                navController.navigate("dashboard") {
                    popUpTo("auth") { inclusive = true }
                }
            }
            is AuthState.Idle -> {
                navController.navigate("auth") {
                    popUpTo(0)
                }
            }
            else -> {}
        }
    }

    NavHost(navController = navController, startDestination = "auth") {
        composable("auth") {
            MainAuthScreen(authViewModel)
        }
        composable("dashboard") {
            DashboardScreen(
                userRole = authViewModel.userRole,
                onRouteClick = { routeId ->
                    navController.navigate("transport/$routeId")
                },
                onAddClick = {
                    navController.navigate("add_bus")
                },
                onViewRequestsClick = {
                    navController.navigate("view_requests")
                },
                onSignOut = { authViewModel.signOut() }
            )
        }
        composable("add_bus") {
            AddBusScreen(onBack = { navController.popBackStack() })
        }
        composable("view_requests") {
            if (authViewModel.userRole == UserRole.ADMIN) {
                AdminRequestsScreen(onBack = { navController.popBackStack() })
            } else {
                LaunchedEffect(Unit) {
                    navController.popBackStack()
                }
            }
        }
        composable("transport/{routeId}") {
            TransportScreen(
                userRole = authViewModel.userRole,
                onSignOut = { authViewModel.signOut() }
            )
        }
    }
}
