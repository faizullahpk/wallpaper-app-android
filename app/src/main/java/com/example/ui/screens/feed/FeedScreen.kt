package com.example.ui.screens.feed

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.ui.PlayerView
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.AsyncImage
import com.example.domain.model.MediaType
import com.example.domain.model.WallpaperItem
import com.example.player.WallpaperVideoPlayer
import com.example.ui.components.ShimmerPlaceholder
import com.example.ui.components.WallpaperCard
import com.example.wallpaper.WallpaperSetter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    wallpaperSetter: WallpaperSetter,
    onDownloadRequested: (WallpaperItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lazyItems = viewModel.wallpaperFeed.collectAsLazyPagingItems()
    val sortOrder by viewModel.sortOrder.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val blurViewed by viewModel.blurViewed.collectAsState()

    var activeDetailItem by remember { mutableStateOf<WallpaperItem?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // WallReddit Brand Header matching HTML design precisely
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "WallReddit",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary, // `#D0BCFF`
                            letterSpacing = (-0.5).sp
                        )
                        Text(
                            text = "PREMIUM WALLPAPER ENGINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF71717A), // zinc-500
                            letterSpacing = 1.2.sp
                        )
                    }

                    // Circular quick search / indicator layout matching HTML design
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                            .clip(CircleShape)
                            .animateContentSize()
                    ) {
                        IconButton(
                            onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    viewModel.updateSearchQuery("")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (searchQuery.isNotEmpty()) Icons.Default.Clear else Icons.Default.Search,
                                contentDescription = "Search status",
                                tint = if (searchQuery.isNotEmpty()) MaterialTheme.colorScheme.primary else Color(0xFFA1A1AA)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = { Text("Search Wallpapers...", color = Color(0xFF71717A)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFFA1A1AA)) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFFA1A1AA))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Search
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onSearch = {
                            // Soft-keyboard search action dismisses search keyboard smoothly
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Horizontal Carousel of Active Subreddits matching HTML Design
                val activeSubs by viewModel.activeSubreddits.collectAsState()
                if (activeSubs.isNotEmpty()) {
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            val isAllSelected = searchQuery.isEmpty()
                            Surface(
                                onClick = { viewModel.updateSearchQuery("") },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isAllSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isAllSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "All Channels",
                                        color = if (isAllSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFFA1A1AA),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        items(activeSubs.size) { index ->
                            val sub = activeSubs[index]
                            val isCurrentSubSelected = searchQuery.removePrefix("r/").trim().equals(sub, ignoreCase = true)
                            Surface(
                                onClick = { viewModel.updateSearchQuery(sub) },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isCurrentSubSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isCurrentSubSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier.height(38.dp)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "r/$sub",
                                        color = if (isCurrentSubSelected) MaterialTheme.colorScheme.onPrimaryContainer else Color(0xFFA1A1AA),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Sort Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("hot" to "Trending", "new" to "Fresh", "top" to "Popular").forEach { (key, label) ->
                        FilterChip(
                            selected = sortOrder == key,
                            onClick = { viewModel.changeSort(key) },
                            label = { Text(label) },
                            leadingIcon = if (sortOrder == key) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
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
            // Masonry Feed
            if (lazyItems.loadState.refresh is LoadState.Loading) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(12) {
                        ShimmerPlaceholder(modifier = Modifier.padding(4.dp))
                    }
                }
            } else if (lazyItems.loadState.refresh is LoadState.Error) {
                val error = (lazyItems.loadState.refresh as LoadState.Error).error
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = "Error icon",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Connection Error",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error.localizedMessage ?: "Failed to fetch wallpapers. Reddit API might be restricted or network is offline.",
                        color = Color.Gray,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { lazyItems.retry() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry Connection", color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            } else {
                val list = lazyItems.itemSnapshotList.items.filter { item ->
                    val query = searchQuery.removePrefix("r/").trim()
                    query.isEmpty() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.subreddit.contains(query, ignoreCase = true) ||
                    item.author.contains(query, ignoreCase = true)
                }

                if (list.isEmpty() && lazyItems.loadState.refresh is LoadState.NotLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.SentimentDissatisfied,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No Wallpapers loaded.\nMake sure you have added enabled subreddits or check internet settings.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp)
                    ) {
                        items(list.size) { index ->
                            val item = list[index]
                            WallpaperCard(
                                item = item,
                                blurViewed = blurViewed,
                                onTap = {
                                    viewModel.markAsViewed(item)
                                    activeDetailItem = item
                                    showSheet = true
                                },
                                onLongPress = {
                                    onDownloadRequested(item)
                                },
                                onFavoriteToggle = {
                                    viewModel.toggleFavorite(item)
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
    }

    // Modal Sheet Detail Viewer
    if (showSheet && activeDetailItem != null) {
        val detailItem = activeDetailItem!!
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                activeDetailItem = null
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            WallpaperDetailView(
                item = detailItem,
                wallpaperSetter = wallpaperSetter,
                onFavoriteToggle = { viewModel.toggleFavorite(detailItem) },
                onDownload = { onDownloadRequested(detailItem) },
                onClose = {
                    showSheet = false
                    activeDetailItem = null
                }
            )
        }
    }
}

@Composable
fun WallpaperDetailView(
    item: WallpaperItem,
    wallpaperSetter: WallpaperSetter,
    onFavoriteToggle: () -> Unit,
    onDownload: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isMuted by remember { mutableStateOf(true) }
    var currentScaleMode by remember { mutableStateOf(WallpaperSetter.ScaleMode.FILL) }

    // Gesture States for Pinch-Zooming
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        offset += offsetChange
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = item.title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            textAlign = TextAlign.Center
        )
        Text(
            text = "r/${item.subreddit} • by u/${item.author}",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Live Preview Box supporting videos/images & pinch-zooming
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black)
                .transformable(state = state)
        ) {
            if (item.mediaType == MediaType.VIDEO) {
                val videoPlayer = remember { WallpaperVideoPlayer(context) }
                DisposableEffect(item.mediaUrl) {
                    videoPlayer.playMedia(item.mediaUrl, isMuted)
                    onDispose { videoPlayer.release() }
                }

                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = videoPlayer.getPlayer()
                            useController = false
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                IconButton(
                    onClick = {
                        isMuted = !isMuted
                        videoPlayer.setMute(isMuted)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                        contentDescription = "Mute Toggle",
                        tint = Color.White
                    )
                }
            } else {
                AsyncImage(
                    model = item.mediaUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scale Modes Selectors (Fit vs Fill vs Crop)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            listOf(
                WallpaperSetter.ScaleMode.FILL to "Fill Screen",
                WallpaperSetter.ScaleMode.FIT to "Aspect Fit",
                WallpaperSetter.ScaleMode.CROP to "Native Crop"
            ).forEach { (mode, label) ->
                ElevatedFilterChip(
                    selected = currentScaleMode == mode,
                    onClick = { currentScaleMode = mode },
                    label = { Text(label) },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onFavoriteToggle) {
                Icon(
                    imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (item.isFavorite) Color.Red else Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Set Wallpaper buttons
            Button(
                onClick = {
                    coroutineScope.launch {
                        Toast.makeText(context, "Downloading & Setting Wallpaper...", Toast.LENGTH_SHORT).show()
                        val result = wallpaperSetter.setWallpaperFromUrl(
                            url = item.mediaUrl,
                            screenType = WallpaperSetter.ScreenType.BOTH,
                            scaleMode = currentScaleMode
                        )
                        if (result) {
                            Toast.makeText(context, "Wallpaper set successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Failed to apply wallpaper.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .height(48.dp)
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Wallpaper, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply Canvas")
            }

            IconButton(onClick = onDownload) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Save To Gallery",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}
