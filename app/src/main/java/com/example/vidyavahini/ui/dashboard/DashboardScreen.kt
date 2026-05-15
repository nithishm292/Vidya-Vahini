package com.example.vidyavahini.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.painterResource
import com.example.vidyavahini.R
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.vidyavahini.model.BusRoute
import com.example.vidyavahini.model.UserRole
import com.example.vidyavahini.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userRole: UserRole,
    onRouteClick: (String) -> Unit,
    onAddClick: () -> Unit,
    onViewRequestsClick: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
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
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    val collapsedFraction = scrollBehavior.state.collapsedFraction
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        if (collapsedFraction < 0.5f) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                contentDescription = "App Logo",
                                modifier = Modifier
                                    .size(64.dp)
                                    .padding(end = 12.dp)
                                    .graphicsLayer {
                                        alpha = (1f - collapsedFraction * 2.5f).coerceIn(0f, 1f)
                                    }
                            )
                        }
                        Column {
                            if (collapsedFraction < 0.5f) {
                                Text(
                                    text = "Vidya-Vahini",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.graphicsLayer {
                                        alpha = (1f - collapsedFraction * 2.5f).coerceIn(0f, 1f)
                                    }
                                )
                                Text(
                                    text = if (userRole == UserRole.ADMIN) "logged in as admin" else "user",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Normal,
                                    modifier = Modifier.graphicsLayer {
                                        alpha = (1f - collapsedFraction * 2.5f).coerceIn(0f, 1f)
                                    }
                                )
                            }
                            Text(
                                "Bus Routes",
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(
                            Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(
                    onClick = { showMenu = true },
                    shape = RoundedCornerShape(16.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .padding(16.dp)
                        .bouncyClickable { showMenu = true }
                ) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
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
            OneUISearchBar(
                query = viewModel.searchQuery,
                onQueryChange = { viewModel.searchQuery = it },
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (routes.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
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
