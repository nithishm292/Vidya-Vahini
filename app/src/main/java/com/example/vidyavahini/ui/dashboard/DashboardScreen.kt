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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vidyavahini.model.BusRoute
import com.example.vidyavahini.model.UserRole
import com.example.vidyavahini.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopAppBar(
    userName: String,
    onLogoutClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        title = {
            Text(
                text = "Vidya-Vahini",
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
                // User Name displayed to the left of the profile icon
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
                            text = { Text("Log Out") },
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
    onRouteClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onViewRequestsClick: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val routes = viewModel.filteredRoutes
    var showMenu by remember { mutableStateOf(false) }
    var routeToDelete by remember { mutableStateOf<BusRoute?>(null) }

    if (routeToDelete != null) {
        AlertDialog(
            onDismissRequest = { routeToDelete = null },
            title = { Text("Delete Route") },
            text = { Text("Are you sure you want to delete Bus ${routeToDelete?.number}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        routeToDelete?.let { viewModel.deleteRoute(it.id) }
                        routeToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { routeToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            DashboardTopAppBar(
                userName = userName,
                onLogoutClick = onSignOut
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
                        text = { Text("Request New Bus") },
                        onClick = {
                            showMenu = false
                            onAddClick()
                        },
                        leadingIcon = { Icon(Icons.Default.AddBusiness, contentDescription = null) }
                    )
                    
                    if (userRole == UserRole.ADMIN) {
                        DropdownMenuItem(
                            text = { Text("View Requests") },
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
                placeholder = { Text("Search routes...") },
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
