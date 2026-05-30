package com.example.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import com.example.ui.components.glassmorphic
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Track
import com.example.ui.components.GlassCard
import com.example.ui.viewmodel.MusicViewModel

@Composable
fun HomeScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val recentTracks = listOf(
        Track(id = "seed_aurora", title = "Aurora Borealis", artist = "Solaris Resonance", album = "Cosmic Winds", durationMs = 248000, filePathOrUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3", albumArtUri = "https://images.unsplash.com/photo-1541185933-ef5d8ed016c2?q=80&w=300", source = "LOCAL"),
        Track(id = "seed_liquid", title = "Liquid Glass", artist = "Monochrome Flux", album = "Reflections", durationMs = 182000, filePathOrUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3", albumArtUri = "https://images.unsplash.com/photo-1518156677180-95a2893f3e9f?q=80&w=300", source = "LOCAL")
    )

    val popularAlbums = listOf(
        Pair("Cosmic Winds", "https://images.unsplash.com/photo-1541185933-ef5d8ed016c2?q=80&w=300"),
        Pair("Mirror Reflections", "https://images.unsplash.com/photo-1518156677180-95a2893f3e9f?q=80&w=300"),
        Pair("Ambient Glacier", "https://images.unsplash.com/photo-1557672172-298e090bd0f1?q=80&w=300")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "AURORA MUSIC",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        color = Color(0x99FFFFFF)
                    )
                    Text(
                        text = "Liquid Sound Space",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
                Text(text = "🛰️", fontSize = 28.sp)
            }
        }

        // Continuing section
        item {
            Text(
                text = "CONTINUE LISTENING",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = Color(0x80FFFFFF)
            )
            Spacer(modifier = Modifier.height(12.dp))
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.playTrack(recentTracks.first()) }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AsyncImage(
                        model = recentTracks.first().albumArtUri,
                        contentDescription = "Artwork",
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1.0f)) {
                        Text(
                            text = recentTracks.first().title,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = recentTracks.first().artist,
                            color = Color(0xB3FFFFFF),
                            fontSize = 14.sp,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "2:14 remaining • Standard FLAC",
                            color = Color(0x66FFFFFF),
                            fontSize = 12.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(21.dp))
                            .background(Color(0x30FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Recently Played horizontal row
        item {
            Text(
                text = "RECENTLY PLAYED",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = Color(0x80FFFFFF)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(recentTracks) { track ->
                    Column(
                        modifier = Modifier
                            .width(140.dp)
                            .clickable { viewModel.playTrack(track) }
                    ) {
                        AsyncImage(
                            model = track.albumArtUri,
                            contentDescription = track.title,
                            modifier = Modifier
                                .size(140.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .glassmorphic(cornerRadius = 16.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = track.title,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            color = Color(0x80FFFFFF),
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // Popular Albums / Artists
        item {
            Text(
                text = "TRENDING RECOMMENDED",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = Color(0x80FFFFFF)
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(popularAlbums) { item ->
                    Column(
                        modifier = Modifier.width(120.dp)
                    ) {
                        AsyncImage(
                            model = item.second,
                            contentDescription = item.first,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(60.dp)), // Circular favorite art
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.first,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp)) // Bottom bar spacer safety
        }
    }
}
