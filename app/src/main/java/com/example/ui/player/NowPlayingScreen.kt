package com.example.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Track
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassSwitch
import com.example.ui.viewmodel.MusicViewModel

@Composable
fun NowPlayingScreen(
    viewModel: MusicViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTrack by viewModel.currentTrack.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val position by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()

    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val pitch by viewModel.pitch.collectAsState()
    val isSkipSilenceEnabled by viewModel.isSkipSilenceEnabled.collectAsState()
    val silenceThresholdSec by viewModel.silenceThresholdSec.collectAsState()
    val sleepTimerMs by viewModel.sleepTimerMs.collectAsState()
    val eqPreset by viewModel.eqPreset.collectAsState()
    val currentLyricLine by viewModel.currentLyricLine.collectAsState()

    var activeSectTab by remember { mutableStateOf("CONTROLS") } // CONTROLS, EQUALIZER, LYRICS, TIMERS
    var showSleepTimerSelector by remember { mutableStateOf(false) }

    val progressValue = if (duration > 0) position.toFloat() / duration else 0.0f

    // Format millisecond integers cleanly to standard mm:ss representation
    fun formatMs(ms: Long): String {
        val totalSecs = ms / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return String.format("%d:%02d", mins, secs)
    }

    if (currentTrack == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090A0E)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🪐", fontSize = 54.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Select any audio track to start the sound cabin", color = Color(0x66FFFFFF), fontSize = 14.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onClose) { Text("Back Home") }
            }
        }
        return
    }

    val track = currentTrack!!

    // Premium Liquid Color Dynamic Ambient Blend Base
    val artworkBlendColor = when (track.id) {
        "seed_aurora" -> Color(0xFF00C6FF)
        "seed_liquid" -> Color(0xFF0072FF)
        "seed_ether" -> Color(0xFF8E2DE2)
        else -> Color(0xFF6A11CB)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF07080C))
    ) {
        // Draw real-time artwork dynamic ambient bleed background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            artworkBlendColor.copy(alpha = 0.22f),
                            Color.Transparent
                        ),
                        radius = 1200f
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dismiss Head handle
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "← DISMISS SOUND CABIN",
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onClose() }
                    )
                    Text(
                        text = "STUDIO MONITOR (Hi-Fi)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color(0x66FFFFFF)
                    )
                    Text(
                        text = if (sleepTimerMs != null) "⏳ ${formatMs(sleepTimerMs!!)}" else "⏱️",
                        fontSize = 14.sp,
                        color = if (sleepTimerMs != null) Color(0xFF00E5FF) else Color.White,
                        modifier = Modifier.clickable { showSleepTimerSelector = true }
                    )
                }
            }

            // High aesthetic spinning Art Panel
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = track.albumArtUri,
                        contentDescription = "Spinning cover artwork",
                        modifier = Modifier
                            .size(240.dp)
                            .clip(RoundedCornerShape(120.dp)) // Pure circle vinyl style
                            .border(6.dp, Color(0x13FFFFFF), RoundedCornerShape(120.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Track metadata details
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = track.title,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${track.artist}  •  ${track.album}",
                        color = Color(0x9EFFFFFF),
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Sync slide progressing track timeline bar
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = progressValue,
                        onValueChange = {
                            val newPos = (it * duration).toLong()
                            viewModel.seekTo(newPos)
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color(0xFF00E5FF)
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = formatMs(position), color = Color(0x66FFFFFF), fontSize = 12.sp)
                        Text(text = formatMs(duration), color = Color(0x66FFFFFF), fontSize = 12.sp)
                    }
                }
            }

            // Central Audio Cabin controllers
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Prevs
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0x13FFFFFF))
                            .clickable { viewModel.prevTrack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⏮️", fontSize = 16.sp)
                    }

                    // Playback
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color.White)
                            .clickable { viewModel.togglePlayPause() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPlaying) "⏸️" else "▶️",
                            fontSize = 24.sp,
                            modifier = Modifier.padding(start = if (isPlaying) 0.dp else 2.dp)
                        )
                    }

                    // Nexts
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0x13FFFFFF))
                            .clickable { viewModel.nextTrack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⏭️", fontSize = 16.sp)
                    }
                }
            }

            // Section switch controls
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0x1AFFFFFF))
                        .padding(2.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val tabs = listOf("CONTROLS", "EQUALIZER", "LYRICS")
                    tabs.forEach { tab ->
                        val isSel = activeSectTab == tab
                        val bg = if (isSel) Color(0x3BFFFFFF) else Color.Transparent
                        val textCol = if (isSel) Color.White else Color(0x80FFFFFF)
                        Box(
                            modifier = Modifier
                                .weight(1.0f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(bg)
                                .clickable { activeSectTab = tab }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = tab, color = textCol, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // CONDITIONAL SUBSECTIONS INTERFACE DISPLAYERS
            item {
                when (activeSectTab) {
                    "LYRICS" -> {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "DEEP ARTWORK LYRICS STREAMER",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0x80FFFFFF)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            if (currentLyricLine != null) {
                                Text(
                                    text = currentLyricLine!!,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF00E5FF),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            } else {
                                Text(
                                    text = "Instrumental interval or lyrics parsing",
                                    fontSize = 15.sp,
                                    color = Color(0x4DFFFFFF),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Full static ID3 Lyrics:",
                                fontSize = 11.sp,
                                color = Color(0x4DFFFFFF)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = track.lyrics ?: "Lyrics metadata block is empty. Connect Spotify libraries to fetch lyrics online.",
                                fontSize = 13.sp,
                                color = Color(0x99FFFFFF),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    "EQUALIZER" -> {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "INTEGRATED SYSTEM HARDWARE EQ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0x80FFFFFF)
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            val presets = listOf("Flat", "Classical", "Dance", "Metal", "Pop", "Rock")
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(presets) { preset ->
                                    val isSelected = eqPreset == preset
                                    val bg = if (isSelected) Color(0x2BFFFFFF) else Color(0x0EFFFFFF)
                                    val bCol = if (isSelected) Color(0xFF00E5FF) else Color.Transparent

                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(bg)
                                            .border(1.dp, bCol, RoundedCornerShape(8.dp))
                                            .clickable { viewModel.applyPreset(preset) }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = preset,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else Color(0x80FFFFFF)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    else -> { // CONTROLS (default custom pitch parameters and skip thresholds!)
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Speed, Pitch, Silence
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    // Speed Config
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Playback Velocity Ratio", color = Color.White, fontSize = 13.sp)
                                        Text("${playbackSpeed}x", color = Color(0xFF00E5FF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Slider(
                                        value = playbackSpeed,
                                        onValueChange = { viewModel.setSpeed(it) },
                                        valueRange = 0.5f..2.0f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color.White,
                                            activeTrackColor = Color(0xFF00E5FF)
                                        )
                                    )

                                    Divider(color = Color(0x13FFFFFF))

                                    // Pitch Parameters
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Frequency Pitch Scaling", color = Color.White, fontSize = 13.sp)
                                        Text("${pitch}x", color = Color(0xFF00E5FF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Slider(
                                        value = pitch,
                                        onValueChange = { viewModel.setPitch(it) },
                                        valueRange = 0.5f..1.5f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = Color.White,
                                            activeTrackColor = Color(0xFF00E5FF)
                                        )
                                    )
                                }
                            }

                            // Smart Silence Config Thresholds
                            GlassCard(modifier = Modifier.fillMaxWidth()) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Silence Decibel Skip Gateway", color = Color.White, fontSize = 14.sp)
                                            Text("Adjustable silence detection threshold", color = Color(0x80FFFFFF), fontSize = 11.sp)
                                        }
                                        GlassSwitch(
                                            checked = isSkipSilenceEnabled,
                                            onCheckedChange = { viewModel.toggleSkipSilence(it) }
                                        )
                                    }

                                    AnimatedVisibility(visible = isSkipSilenceEnabled) {
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text("Silence duration gateway", color = Color(0x66FFFFFF), fontSize = 12.sp)
                                                // Default skip silence threshold: 12 seconds or custom.
                                                Text("${silenceThresholdSec}s", color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Slider(
                                                value = silenceThresholdSec.toFloat(),
                                                onValueChange = { viewModel.setSilenceThreshold(it.toInt()) },
                                                valueRange = 1f..30f,
                                                colors = SliderDefaults.colors(
                                                    thumbColor = Color.White,
                                                    activeTrackColor = Color(0xFF00E5FF)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    // --- Sleep timers sheet selector ---
    if (showSleepTimerSelector) {
        val timerOptions = listOf(5, 10, 15, 30, 60)
        AlertDialog(
            onDismissRequest = { showSleepTimerSelector = false },
            containerColor = Color(0xFF141722),
            title = { Text("Assemble Smart Sleep Timer Fade", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select session limits. Sound fades out slowly.", color = Color(0x80FFFFFF), fontSize = 12.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        timerOptions.forEach { mins ->
                            Box(
                                modifier = Modifier
                                    .weight(1.0f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x0EFFFFFF))
                                    .clickable {
                                        viewModel.startSleepTimer(mins)
                                        showSleepTimerSelector = false
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("${mins}m", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (sleepTimerMs != null) {
                    GlassButton(
                        onClick = {
                            viewModel.cancelSleepTimer()
                            showSleepTimerSelector = false
                        },
                        cornerRadius = 8.dp
                    ) {
                        Text("Disable Timer", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showSleepTimerSelector = false }) {
                    Text("Dismiss", color = Color(0x80FFFFFF))
                }
            }
        )
    }
}
