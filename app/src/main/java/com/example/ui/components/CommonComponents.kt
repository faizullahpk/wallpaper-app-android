package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.domain.model.MediaType
import com.example.domain.model.WallpaperItem

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WallpaperCard(
    item: WallpaperItem,
    blurViewed: Boolean,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isLikeVisible by remember { mutableStateOf(false) }

    val amoledOverlay = if (item.isViewed) Color.Black.copy(alpha = 0.5f) else Color.Transparent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        isLikeVisible = true
                        onFavoriteToggle()
                    },
                    onTap = { onTap() },
                    onLongPress = { onLongPress() }
                )
            }
    ) {
        // Thumbnail Image
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(item.thumbnailUrl)
                .crossfade(true)
                .build(),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(item.aspectRatio.toFloat().coerceIn(0.5f, 1.8f))
                .let {
                    if (item.isViewed && blurViewed) it.blur(6.dp) else it
                }
        )

        // Dark dim overlay if viewed
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(amoledOverlay)
        )

        // Vignette Gradient back cover for text legibility
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.7f)
                        ),
                        startY = 100f
                    )
                )
        )

        // Top Badges Row (NSFW and Resolution tags)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (item.isNsfw) {
                    Badge(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ) {
                        Text("NSFW", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (item.mediaType == MediaType.VIDEO) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Video marker",
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
                if (item.mediaType == MediaType.GIF) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ) {
                        Text("GIF", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // HD/4K tags
            Badge(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Text(item.resolutionTag, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Bottom Metadata Frame
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .align(Alignment.BottomStart)
        ) {
            Text(
                text = item.title,
                color = Color.White,
                fontSize = 12.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "r/${item.subreddit}",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Votes",
                        tint = Color.Yellow,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = item.score.toString(),
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
        }

        // Viewed Icon Marker
        if (item.isViewed) {
            Icon(
                imageVector = Icons.Default.RemoveRedEye,
                contentDescription = "Viewed indicator",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center)
            )
        }

        // Double tap animation visible
        if (isLikeVisible) {
            var pulseScale by remember { mutableStateOf(0.4f) }
            LaunchedEffect(Unit) {
                animate(
                    initialValue = 0.4f,
                    targetValue = 1.2f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ) { value, _ -> pulseScale = value }
                isLikeVisible = false
            }
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Liked pulse",
                tint = Color.Red,
                modifier = Modifier
                    .size(54.dp)
                    .align(Alignment.Center)
                    .graphicsLayer(
                        scaleX = pulseScale,
                        scaleY = pulseScale
                    )
            )
        }
    }
}

@Composable
fun ShimmerPlaceholder(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition()
    val alphaAnim = infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Gray.copy(alpha = alphaAnim.value))
    )
}
