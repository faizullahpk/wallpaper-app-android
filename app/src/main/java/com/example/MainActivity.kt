package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.analytics.AnalyticsScreen
import com.example.ui.screens.analytics.AnalyticsViewModel
import com.example.ui.screens.downloads.DownloadsScreen
import com.example.ui.screens.downloads.DownloadsViewModel
import com.example.ui.screens.favorites.FavoritesScreen
import com.example.ui.screens.favorites.FavoritesViewModel
import com.example.ui.screens.feed.FeedScreen
import com.example.ui.screens.feed.FeedViewModel
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.screens.subreddits.SubredditScreen
import com.example.ui.screens.subreddits.SubredditViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as WallpaperApplication).container

        // Instantiate modern ViewModels via local Clean Architecture factories
        val feedViewModel = ViewModelProvider(
            this,
            FeedViewModel.Factory(
                container.repository,
                container.getFeedUseCase,
                container.favoritesUseCase,
                container.viewedUseCase,
                container.settingsManager
            )
        )[FeedViewModel::class.java]

        val favoritesViewModel = ViewModelProvider(
            this,
            FavoritesViewModel.Factory(container.favoritesUseCase)
        )[FavoritesViewModel::class.java]

        val downloadsViewModel = ViewModelProvider(
            this,
            DownloadsViewModel.Factory(container.repository, container.downloadManagerOrchestrator)
        )[DownloadsViewModel::class.java]

        val subredditViewModel = ViewModelProvider(
            this,
            SubredditViewModel.Factory(container.subredditUseCase)
        )[SubredditViewModel::class.java]

        val analyticsViewModel = ViewModelProvider(
            this,
            AnalyticsViewModel.Factory(container.repository)
        )[AnalyticsViewModel::class.java]

        val settingsViewModel = ViewModelProvider(
            this,
            SettingsViewModel.Factory(this, container.settingsManager)
        )[SettingsViewModel::class.java]

        setContent {
            val isDark by settingsViewModel.darkMode.collectAsState()
            val isAmoled by settingsViewModel.amoledMode.collectAsState()
            val useDynamicColors by settingsViewModel.dynamicColors.collectAsState()

            MyApplicationTheme(
                darkTheme = isDark,
                dynamicColor = useDynamicColors,
                amoledMode = isAmoled
            ) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "feed"

                val navigationItems = listOf(
                    NavigationItem("feed", "Explore", Icons.Default.PhotoLibrary),
                    NavigationItem("favorites", "Bookmarks", Icons.Default.Favorite),
                    NavigationItem("downloads", "Downloads", Icons.Default.Download),
                    NavigationItem("subreddits", "Manager", Icons.Default.List),
                    NavigationItem("analytics", "Stats", Icons.Default.BarChart),
                    NavigationItem("settings", "Settings", Icons.Default.Settings)
                )

                // Adaptive Scaffold detecting width constraints for phones (bottom nav) vs foldables/tablets (rail nav)
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isTablet = maxWidth >= 600.dp

                    Scaffold(
                        bottomBar = {
                            if (!isTablet) {
                                NavigationBar {
                                    navigationItems.forEach { item ->
                                        NavigationBarItem(
                                            selected = currentRoute == item.route,
                                            onClick = {
                                                navController.navigate(item.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                            icon = { Icon(item.icon, contentDescription = item.label) },
                                            label = { Text(item.label) }
                                        )
                                    }
                                }
                            }
                        }
                    ) { innerPadding ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    bottom = if (isTablet) 0.dp else innerPadding.calculateBottomPadding(),
                                    top = innerPadding.calculateTopPadding()
                                )
                        ) {
                            if (isTablet) {
                                NavigationRail(
                                    modifier = Modifier.fillMaxHeight(),
                                    header = {
                                        Icon(
                                            Icons.Default.Wallpaper,
                                            contentDescription = "Reddit Wallpapers Logo",
                                            modifier = Modifier.padding(vertical = 12.dp)
                                        )
                                    }
                                ) {
                                    navigationItems.forEach { item ->
                                        NavigationRailItem(
                                            selected = currentRoute == item.route,
                                            onClick = {
                                                navController.navigate(item.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                            icon = { Icon(item.icon, contentDescription = item.label) },
                                            label = { Text(item.label) }
                                        )
                                    }
                                }
                            }

                            // Hosting Screens with explicit non-blocking callback navigators
                            NavHost(
                                navController = navController,
                                startDestination = "feed",
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                            ) {
                                composable("feed") {
                                    FeedScreen(
                                        viewModel = feedViewModel,
                                        wallpaperSetter = container.wallpaperSetter,
                                        onDownloadRequested = { item ->
                                            downloadsViewModel.startDownload(
                                                postId = item.id,
                                                mediaUrl = item.mediaUrl,
                                                title = item.title,
                                                subreddit = item.subreddit
                                            )
                                            Toast.makeText(this@MainActivity, "Download initiated in background!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                composable("favorites") {
                                    FavoritesScreen(
                                        viewModel = favoritesViewModel,
                                        wallpaperSetter = container.wallpaperSetter,
                                        onDownloadRequested = { item ->
                                            downloadsViewModel.startDownload(
                                                postId = item.id,
                                                mediaUrl = item.mediaUrl,
                                                title = item.title,
                                                subreddit = item.subreddit
                                            )
                                            Toast.makeText(this@MainActivity, "Download enqueued!", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                                composable("downloads") {
                                    DownloadsScreen(viewModel = downloadsViewModel)
                                }
                                composable("subreddits") {
                                    SubredditScreen(viewModel = subredditViewModel)
                                }
                                composable("analytics") {
                                    AnalyticsScreen(viewModel = analyticsViewModel)
                                }
                                composable("settings") {
                                    SettingsScreen(viewModel = settingsViewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
