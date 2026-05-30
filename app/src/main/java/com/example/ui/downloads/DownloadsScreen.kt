package com.example.ui.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassButton
import com.example.ui.viewmodel.MusicViewModel

@Composable
fun DownloadsScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val downloadQueue by viewModel.downloadQueue.collectAsState()
    val allTracks by viewModel.allTracks.collectAsState()
    val cachedTracks = allTracks.filter { it.cachedOffline }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "STORAGE UTILIZATION MONITOR",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = Color(0x80FFFFFF)
            )
            Spacer(modifier = Modifier.height(12.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "AURA FLAC CACHE ENGINE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0x73FFFFFF)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("256.2 MB", fontSize = 28.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text("Used by music cache", fontSize = 12.sp, color = Color(0x80FFFFFF))
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("124 GB", fontSize = 16.sp, fontWeight = FontWeight.Normal, color = Color(0xB2FFFFFF))
                        Text("Available Storage", fontSize = 12.sp, color = Color(0x66FFFFFF))
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Custom segmented visual storage representation bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0x1AFFFFFF))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.15f)
                            .background(Color(0xFF00E5FF))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(0.85f)
                            .background(Color.Transparent)
                    )
                }
            }
        }

        // Active Downloading Tasks
        if (downloadQueue.isNotEmpty()) {
            item {
                Text(
                    text = "ACTIVE DOWNLOAD QUEUE SPEED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color(0x80FFFFFF)
                )
            }
            items(downloadQueue) { task ->
                val trackName = allTracks.find { it.id == task.trackId }?.title ?: "Unknown Track"
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1.0f)) {
                            Text(
                                text = trackName,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Status: ${task.status} (${task.progress}%)",
                                    color = Color(0x80FFFFFF),
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "${task.downloadedBytes / 1024 / 1024}MB / ${task.totalBytes / 1024 / 1024}MB",
                                    color = Color(0x4DFFFFFF),
                                    fontSize = 11.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            // Simple Progress Indicators
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Color(0x13FFFFFF))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(task.progress / 100.0f)
                                        .background(Color(0xFF00E5FF))
                                )
                            }
                        }
                    }
                }
            }
        }

        // Offline Cached Library
        item {
            Text(
                text = "OFFLINE CACHED FLAC FILES",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = Color(0x80FFFFFF)
            )
        }

        if (cachedTracks.isEmpty()) {
            item {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No tracks downloaded offline yet.", color = Color(0x4DFFFFFF), fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "💡 Go to Discover or Library, click on any track's edit options or perform offline synchronizations.",
                                color = Color(0x33FFFFFF),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        } else {
            items(cachedTracks) { track ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.playTrack(track) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = track.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Cached FLAC • ${track.artist}",
                                color = Color(0x80FFFFFF),
                                fontSize = 12.sp
                            )
                        }
                        GlassButton(
                            onClick = { viewModel.removeDownload(track.id) },
                            cornerRadius = 8.dp,
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text("Purge Cache 🧹", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
