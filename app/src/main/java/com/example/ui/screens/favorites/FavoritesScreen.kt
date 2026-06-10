package com.example.ui.screens.favorites

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.WallpaperItem
import com.example.ui.components.WallpaperCard
import com.example.ui.screens.feed.WallpaperDetailView
import com.example.wallpaper.WallpaperSetter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    wallpaperSetter: WallpaperSetter,
    onDownloadRequested: (WallpaperItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val favorites by viewModel.favorites.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val sortBy by viewModel.sortBy.collectAsState()

    var activeDetailItem by remember { mutableStateOf<WallpaperItem?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                Text(
                    "Bookmarks",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Filter saved cards...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Categories chips: ALL, IMAGES, VIDEOS, GIFS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("ALL" to "All", "IMAGES" to "Images", "VIDEOS" to "Videos", "GIFS" to "GIFs").forEach { (key, label) ->
                        FilterChip(
                            selected = selectedCategory == key,
                            onClick = { viewModel.selectCategory(key) },
                            label = { Text(label) }
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (favorites.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Empty",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Your bookmark chest is vacant.\nDouble tap on any wallpaper in the feed to save it!",
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(favorites) { item ->
                        WallpaperCard(
                            item = item,
                            blurViewed = false,
                            onTap = {
                                activeDetailItem = item
                                showSheet = true
                            },
                            onLongPress = {
                                viewModel.removeFavorite(item.id)
                            },
                            onFavoriteToggle = {
                                viewModel.removeFavorite(item.id)
                            },
                            onDownload = {
                                onDownloadRequested(item)
                            }
                        )
                    }
                }
            }
        }
    }

    if (showSheet && activeDetailItem != null) {
        val detailItem = activeDetailItem!!
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                activeDetailItem = null
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            WallpaperDetailView(
                item = detailItem,
                wallpaperSetter = wallpaperSetter,
                onFavoriteToggle = {
                    viewModel.removeFavorite(detailItem.id)
                    showSheet = false
                },
                onDownload = { onDownloadRequested(detailItem) },
                onClose = {
                    showSheet = false
                    activeDetailItem = null
                }
            )
        }
    }
}
