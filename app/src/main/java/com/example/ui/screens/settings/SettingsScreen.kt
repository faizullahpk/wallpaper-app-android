package com.example.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val nsfwMode by viewModel.nsfwMode.collectAsState()
    val amoledMode by viewModel.amoledMode.collectAsState()
    val darkMode by viewModel.darkMode.collectAsState()
    val dynamicColors by viewModel.dynamicColors.collectAsState()
    val gridSize by viewModel.gridSize.collectAsState()
    val blurViewed by viewModel.blurViewed.collectAsState()

    val autoWallpaperEnabled by viewModel.autoWallpaperEnabled.collectAsState()
    val autoWallpaperSource by viewModel.autoWallpaperSource.collectAsState()
    val autoWallpaperIntervalHours by viewModel.autoWallpaperIntervalHours.collectAsState()

    var showNsfwMenu by remember { mutableStateOf(false) }
    var showSourceMenu by remember { mutableStateOf(false) }
    var showIntervalMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Preferences", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("PREFERENCES", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)

            // Dark Theme Settings
            SettingsRow(
                title = "Dark Theme Mode",
                description = "Activate application dark backgrounds",
                icon = Icons.Default.DarkMode
            ) {
                Switch(checked = darkMode, onCheckedChange = { viewModel.setDarkMode(it) })
            }

            // AMOLED Theme Settings
            SettingsRow(
                title = "AMOLED Black",
                description = "Deep absolute pitch-black theme",
                icon = Icons.Default.Brightness2
            ) {
                Switch(checked = amoledMode, onCheckedChange = { viewModel.setAmoledMode(it) })
            }

            // Material 3 Custom Dynamic colors
            SettingsRow(
                title = "Dynamic Colors",
                description = "Match UI palette with system system background",
                icon = Icons.Default.Palette
            ) {
                Switch(checked = dynamicColors, onCheckedChange = { viewModel.setDynamicColors(it) })
            }

            // Blur Viewed posts
            SettingsRow(
                title = "Blur Viewed History",
                description = "Blur previously viewed media thumbnails",
                icon = Icons.Default.VisibilityOff
            ) {
                Switch(checked = blurViewed, onCheckedChange = { viewModel.setBlurViewed(it) })
            }

            // Grid size selection (2 vs 3 columns)
            SettingsRow(
                title = "Grid Layout Sizing",
                description = "Show 2 or 3 items in masonry layouts",
                icon = Icons.Default.GridView
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = gridSize == 2,
                        onClick = { viewModel.setGridSize(2) },
                        label = { Text("2 Cols") }
                    )
                    FilterChip(
                        selected = gridSize == 3,
                        onClick = { viewModel.setGridSize(3) },
                        label = { Text("3 Cols") }
                    )
                }
            }

            // NSFW Dropdown Settings
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color.Gray)
                    Column {
                        Text("NSFW Content Display", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Select safety overlay mode", fontSize = 11.sp, color = Color.Gray)
                    }
                }
                Box {
                    TextButton(onClick = { showNsfwMenu = true }) {
                        Text(nsfwMode)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                    DropdownMenu(expanded = showNsfwMenu, onDismissRequest = { showNsfwMenu = false }) {
                        listOf("HIDDEN" to "Hide Raw NSFW", "BLUR" to "Apply Safety Blur", "VISIBLE" to "Show Plain NSFW").forEach { (key, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.setNsfwMode(key)
                                    showNsfwMenu = false
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            Text("BACKGROUND AUTO CHANGER", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)

            // Auto Wallpaper setting toggle
            SettingsRow(
                title = "Enable Auto Changer",
                description = "WorkManager periodic wallpaper updates",
                icon = Icons.Default.Loop
            ) {
                Switch(checked = autoWallpaperEnabled, onCheckedChange = { viewModel.setAutoWallpaperEnabled(it) })
            }

            if (autoWallpaperEnabled) {
                // Auto changer Source
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Source, contentDescription = null, tint = Color.Gray)
                        Column {
                            Text("Changer Source", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Pull candidate images from folder", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                    Box {
                        TextButton(onClick = { showSourceMenu = true }) {
                            Text(autoWallpaperSource)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(expanded = showSourceMenu, onDismissRequest = { showSourceMenu = false }) {
                            listOf("SUBREDDITS" to "Active Subreddits", "FAVORITES" to "Your Bookmarks", "DOWNLOADS" to "Downloaded Folder").forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.setAutoWallpaperSource(key)
                                        showSourceMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Intervals
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = Color.Gray)
                        Column {
                            Text("Frequency Scheduler", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text("Define changer execution intervals", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                    Box {
                        TextButton(onClick = { showIntervalMenu = true }) {
                            val rep = when (autoWallpaperIntervalHours) {
                                1 -> "Every Hour"
                                12 -> "Every 12 Hours"
                                24 -> "Every 24 Hours"
                                168 -> "Every 1 Week"
                                else -> "$autoWallpaperIntervalHours Hours"
                            }
                            Text(rep)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(expanded = showIntervalMenu, onDismissRequest = { showIntervalMenu = false }) {
                            listOf(1 to "1 Hour", 12 to "12 Hours", 24 to "24 Hours", 168 to "1 Week").forEach { (hours, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.setAutoWallpaperIntervalHours(hours)
                                        showIntervalMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun SettingsRow(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    action: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.Gray)
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(description, fontSize = 11.sp, color = Color.Gray)
            }
        }
        action()
    }
}
