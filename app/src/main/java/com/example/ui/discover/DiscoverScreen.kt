package com.example.ui.discover

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassTextField
import com.example.ui.viewmodel.MusicViewModel

@Composable
fun DiscoverScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val youtubeResults by viewModel.youtubeSearchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    val spotifyProfile by viewModel.spotifyProfile.collectAsState()
    val spotifyToken by viewModel.spotifyToken.collectAsState()

    var showSpotifyConfig by remember { mutableStateOf(false) }
    var rawTokenInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Search Head
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "DISCOVER METADATA & API",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = Color(0x80FFFFFF)
            )
            Spacer(modifier = Modifier.height(8.dp))
            GlassTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = "Search YouTube music & metadata...",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search icon",
                        tint = Color.White
                    )
                }
            )
        }

        // Search Results
        if (isSearching) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }
        } else if (youtubeResults.isNotEmpty()) {
            item {
                Text(
                    text = "YOUTUBE AUDIO SEARCH RESULTS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color(0x80FFFFFF)
                )
            }
            items(youtubeResults) { video ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = video.thumbnailUri,
                            contentDescription = "Youtube cover",
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x11FFFFFF)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1.0f)) {
                            Text(
                                text = video.title,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Text(
                                text = video.channelTitle,
                                color = Color(0x80FFFFFF),
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        GlassButton(
                            onClick = { viewModel.importYouTubeTrack(video) },
                            cornerRadius = 8.dp,
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Import ⬇️", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Integrations Management Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "STREAMING ACCOUNTS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = Color(0x80FFFFFF)
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Spotify Connector
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔊", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Spotify Music Link",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (spotifyProfile != null) "Connected as ${spotifyProfile!!.displayName}" else "Sync liked songs & playlists",
                                color = Color(0x80FFFFFF),
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (spotifyProfile != null) {
                        GlassButton(
                            onClick = { viewModel.disconnectSpotify() },
                            cornerRadius = 8.dp,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Unlink", fontSize = 12.sp, color = Color.White)
                        }
                    } else {
                        GlassButton(
                            onClick = { showSpotifyConfig = !showSpotifyConfig },
                            cornerRadius = 8.dp,
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text("Link Link", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }

                AnimatedVisibility(visible = showSpotifyConfig && spotifyProfile == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Config Spotify Web OAuth API key:",
                            color = Color(0x99FFFFFF),
                            fontSize = 12.sp
                        )
                        GlassTextField(
                            value = rawTokenInput,
                            onValueChange = { rawTokenInput = it },
                            placeholder = "Paste Spotify OAuth Access Token..."
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            GlassButton(
                                onClick = {
                                    viewModel.updateSpotifyToken(rawTokenInput)
                                    viewModel.syncSpotify()
                                    showSpotifyConfig = false
                                },
                                cornerRadius = 8.dp,
                                modifier = Modifier.weight(1.0f)
                            ) {
                                Text("Link Account", fontSize = 12.sp, color = Color.White)
                            }
                            GlassButton(
                                onClick = {
                                    // Use demo connection
                                    viewModel.updateSpotifyToken("demo_token")
                                    viewModel.syncSpotify()
                                    showSpotifyConfig = false
                                },
                                cornerRadius = 8.dp,
                                modifier = Modifier.weight(1.0f)
                            ) {
                                Text("Demo Mock Sync", fontSize = 12.sp, color = Color.White)
                            }
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
