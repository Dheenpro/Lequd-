package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.repository.MusicRepository
import com.example.player.AudioPlayerManager
import com.example.ui.components.glassBackgroundDynamic
import com.example.ui.components.glassmorphic
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import com.example.ui.home.HomeScreen
import com.example.ui.discover.DiscoverScreen
import com.example.ui.library.LibraryScreen
import com.example.ui.downloads.DownloadsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.player.NowPlayingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MusicViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AuroraApplication
        val container = app.container

        setContent {
            MyApplicationTheme {
                val factory = remember {
                    MusicViewModelFactory(container.musicRepository, container.audioPlayerManager)
                }
                val viewModel: MusicViewModel = viewModel(factory = factory)

                val context = LocalContext.current
                LaunchedEffect(Unit) {
                    viewModel.uiEvent.collectLatest { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }

                MainScaffold(viewModel = viewModel)
            }
        }
    }
}

// Simple standard ViewModel factory
class MusicViewModelFactory(
    private val repository: MusicRepository,
    private val playerManager: AudioPlayerManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MusicViewModel(repository, playerManager) as T
    }
}

@Composable
fun MainScaffold(viewModel: MusicViewModel) {
    val activeTab by viewModel.activeTabByState.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val blurIntensity by viewModel.blurIntensity.collectAsState()

    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val position by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()

    var isNowPlayingExpanded by remember { mutableStateOf(false) }

    // Dynamic Color Blend tint based on current art details
    val activeCoverHue = when (currentTrack?.id) {
        "seed_aurora" -> Color(0xFF00C6FF)
        "seed_liquid" -> Color(0xFF0072FF)
        "seed_ether" -> Color(0xFF8E2DE2)
        else -> Color(0xFF1E2433)
    }

    val dynamicBlurBgModifier = if (themeMode == "GLASS") {
        Modifier.glassBackgroundDynamic(artworkColor = activeCoverHue, intensity = blurIntensity)
    } else if (themeMode == "DARK") {
        Modifier.background(Color(0xFF090A0E))
    } else {
        Modifier.background(Color(0xFFF0F3F9))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(dynamicBlurBgModifier)
    ) {
        // Safe drawing window insets covering top status gaps
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            // Screen contents
            Box(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    "HOME" -> HomeScreen(viewModel = viewModel)
                    "DISCOVER" -> DiscoverScreen(viewModel = viewModel)
                    "LIBRARY" -> LibraryScreen(viewModel = viewModel)
                    "DOWNLOADS" -> DownloadsScreen(viewModel = viewModel)
                    "SETTINGS" -> SettingsScreen(viewModel = viewModel)
                }
            }

            // High elegance Mini Player hovering deck above navigation bar
            if (currentTrack != null) {
                val track = currentTrack!!
                val miniProgress = if (duration > 0) position.toFloat() / duration else 0f

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .glassmorphic(cornerRadius = 24.dp)
                        .clickable { isNowPlayingExpanded = true }
                ) {
                    // Micro timeline progress line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(Color(0x13FFFFFF))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(miniProgress)
                                .background(Color(0xFF00E5FF))
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.0f)) {
                            AsyncImage(
                                model = track.albumArtUri,
                                contentDescription = "Mini cover artwork",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = track.title,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = track.artist,
                                    color = Color(0x9EFFFFFF),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(17.dp))
                                    .background(Color(0x1AFFFFFF))
                                    .clickable { viewModel.togglePlayPause() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(if (isPlaying) "⏸️" else "▶️", fontSize = 14.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(17.dp))
                                    .background(Color(0x1AFFFFFF))
                                    .clickable { viewModel.nextTrack() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⏭️", fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            // Persistent bottom navigation deck bar supporting navigationBars safe areas padding
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xCC0A0A0A)) // 80% opaque dark charcoal base
                    .drawBehind {
                        drawLine(
                            color = Color(0x0FFFFFFF), // border-white/5
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val menuItems = listOf(
                    Pair("HOME", "🪐"),
                    Pair("DISCOVER", "🔍"),
                    Pair("LIBRARY", "🎵"),
                    Pair("DOWNLOADS", "💾"),
                    Pair("SETTINGS", "⚙️")
                )

                menuItems.forEach { item ->
                    val isSelected = activeTab == item.first
                    val textCol = if (isSelected) Color(0xFF00E5FF) else Color(0x66FFFFFF)

                    Column(
                        modifier = Modifier
                            .clickable { viewModel.switchTab(item.first) }
                            .padding(horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = item.second, fontSize = 22.sp, color = textCol)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.first,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = textCol
                        )
                    }
                }
            }
        }

        // Expanded full-page sliding deck cabins overlay
        AnimatedVisibility(
            visible = isNowPlayingExpanded,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            NowPlayingScreen(
                viewModel = viewModel,
                onClose = { isNowPlayingExpanded = false }
            )
        }
    }
}
