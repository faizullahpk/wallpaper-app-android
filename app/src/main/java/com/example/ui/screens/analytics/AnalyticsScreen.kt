package com.example.ui.screens.analytics

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.analyticsState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Local Analytics Hub", fontWeight = FontWeight.Bold) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Metrics grid cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Total Viewed",
                    count = state.totalViewed.toString(),
                    icon = Icons.Default.RemoveRedEye,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Saved Favorites",
                    count = state.totalFavorites.toString(),
                    icon = Icons.Default.Favorite,
                    tint = Color.Red,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = "Files Saved",
                    count = state.totalDownloads.toString(),
                    icon = Icons.Default.Download,
                    tint = Color.Green,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Storage Used",
                    count = String.format("%.1f MB", state.storageUsageMb),
                    icon = Icons.Default.SdCard,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Text info cards
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                    Column {
                        Text("Highly Browse Community", fontSize = 12.sp, color = Color.Gray)
                        Text("r/${state.mostViewedSubreddit}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Custom draw stats card for bookmarks distribution
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Saved media categories spread", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    if (state.totalFavorites == 0) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text("No bookmark distributions to visualize currently.", color = Color.Gray, fontSize = 12.sp)
                        }
                    } else {
                        // Custom Canvas pie/donut diagram
                        val distribution = state.favoriteCategoriesDistribution
                        val images = distribution["IMAGE"] ?: 0
                        val videos = distribution["VIDEO"] ?: 0
                        val gifs = distribution["GIF"] ?: 0

                        val total = (images + videos + gifs).toFloat()

                        val imagesColor = MaterialTheme.colorScheme.primary
                        val videosColor = MaterialTheme.colorScheme.secondary
                        val gifsColor = MaterialTheme.colorScheme.tertiary

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.size(140.dp)) {
                                val sizeVal = size.minDimension
                                var startAngle = 0f

                                val segments = listOf(
                                    (images / total) * 360f to imagesColor,
                                    (videos / total) * 360f to videosColor,
                                    (gifs / total) * 360f to gifsColor
                                )

                                segments.forEach { (sweep, color) ->
                                    if (sweep > 0f) {
                                        drawArc(
                                            color = color,
                                            startAngle = startAngle,
                                            sweepAngle = sweep,
                                            useCenter = false,
                                            size = Size(sizeVal, sizeVal),
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 30f)
                                        )
                                        startAngle += sweep
                                    }
                                }
                            }
                        }

                        // Labels explanation
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            CategoryLegend("Images", images, imagesColor)
                            CategoryLegend("Videos", videos, videosColor)
                            CategoryLegend("GIFs", gifs, gifsColor)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            Column {
                Text(text = title, fontSize = 12.sp, color = Color.Gray)
                Text(text = count, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CategoryLegend(name: String, count: Int, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Text("$name: $count", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}
