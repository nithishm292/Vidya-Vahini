package com.example.vidyavahini.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vidyavahini.model.BusRoute
import com.example.vidyavahini.model.UserRole
import com.example.vidyavahini.utils.AppLanguage
import com.example.vidyavahini.utils.AppStrings
import com.example.vidyavahini.utils.LocalAppLanguage
import com.example.vidyavahini.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopAppBar(
    userName: String,
    onLogoutClick: () -> Unit,
    onLanguageChange: (AppLanguage) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val currentLanguage = LocalAppLanguage.current

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        title = {
            Text(
                text = AppStrings.appTitle(currentLanguage),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
        },
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                // Language Toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Text(
                        text = if (currentLanguage == AppLanguage.ENGLISH) "EN" else "KN",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Switch(
                        checked = currentLanguage == AppLanguage.KANNADA,
                        onCheckedChange = { isChecked ->
                            onLanguageChange(if (isChecked) AppLanguage.KANNADA else AppLanguage.ENGLISH)
                        },
                        modifier = Modifier.scale(0.7f)
                    )
                }

                // User Name
                Text(
                    text = userName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(end = 8.dp)
                )

                // Interactive Profile Action Button
                Box {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User Profile Menu",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Professional Profile Dropdown Menu
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.clip(MaterialTheme.shapes.medium)
                    ) {
                        DropdownMenuItem(
                            text = { Text(AppStrings.logOut(currentLanguage)) },
                            onClick = {
                                showMenu = false
                                onLogoutClick()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userName: String,
    userRole: UserRole,
    onLanguageChange: (AppLanguage) -> Unit,
    onRouteClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onViewRequestsClick: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val routes = viewModel.filteredRoutes
    var showMenu by remember { mutableStateOf(false) }
    var routeToDelete by remember { mutableStateOf<BusRoute?>(null) }
    val currentLanguage = LocalAppLanguage.current

    if (routeToDelete != null) {
        AlertDialog(
            onDismissRequest = { routeToDelete = null },
            title = { Text(AppStrings.deleteRoute(currentLanguage)) },
            text = { Text(AppStrings.deleteConfirm(currentLanguage, routeToDelete?.number ?: "")) },
            confirmButton = {
                TextButton(
                    onClick = {
                        routeToDelete?.let { viewModel.deleteRoute(it.id) }
                        routeToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text(AppStrings.delete(currentLanguage))
                }
            },
            dismissButton = {
                TextButton(onClick = { routeToDelete = null }) {
                    Text(AppStrings.cancel(currentLanguage))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            DashboardTopAppBar(
                userName = userName,
                onLogoutClick = onSignOut,
                onLanguageChange = onLanguageChange
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { showMenu = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(AppStrings.requestNewBus(currentLanguage)) },
                        onClick = {
                            showMenu = false
                            onAddClick()
                        },
                        leadingIcon = { Icon(Icons.Default.AddBusiness, contentDescription = null) }
                    )
                    
                    if (userRole == UserRole.ADMIN) {
                        DropdownMenuItem(
                            text = { Text(AppStrings.viewRequests(currentLanguage)) },
                            onClick = {
                                showMenu = false
                                onViewRequestsClick()
                            },
                            leadingIcon = { Icon(Icons.Default.Notifications, contentDescription = null) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                placeholder = { Text(AppStrings.searchPlaceholder(currentLanguage)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (routes.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(routes) { route ->
                        BusRouteCard(
                            route = route,
                            onClick = { onRouteClick(route.id) },
                            onDeleteClick = if (userRole == UserRole.ADMIN) {
                                { routeToDelete = route }
                            } else null
                        )
                    }
                }
            }
        }
    }
}
