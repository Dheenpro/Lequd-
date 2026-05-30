package com.example.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.data.model.Playlist
import com.example.data.model.Track
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassTextField
import com.example.ui.viewmodel.MusicViewModel

@Composable
fun LibraryScreen(
    viewModel: MusicViewModel,
    modifier: Modifier = Modifier
) {
    val playlists by viewModel.playlists.collectAsState()
    val allTracks by viewModel.allTracks.collectAsState()
    val activePlaylistId by viewModel.selectedPlaylistId.collectAsState()
    val activePlaylistTracks by viewModel.selectedPlaylistTracks.collectAsState()

    var showMetadataEditor by remember { mutableStateOf<Track?>(null) }
    var newTitle by remember { mutableStateOf("") }
    var newArtist by remember { mutableStateOf("") }
    var newAlbum by remember { mutableStateOf("") }

    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var newPlaylistDesc by remember { mutableStateOf("") }

    val localFolders = listOf(
        Pair("📁 /Music", "Internal Storage • 12 tracks"),
        Pair("💾 /AuraTracks", "SD Card Storage • 4 tracks"),
        Pair("📁 /Downloads/Music", "Cache Files • 8 tracks")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dynamic navigation inside library (Playlist view vs index view)
        if (activePlaylistId != null) {
            val pName = playlists.find { it.id == activePlaylistId }?.name ?: "Selected Tracks"
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "← BACK TO LIB",
                        color = Color(0xFF00E5FF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { viewModel.selectPlaylist(null) }
                    )
                    Text(
                        text = pName.uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }

            if (activePlaylistTracks.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No tracks found under criteria", color = Color(0x66FFFFFF))
                    }
                }
            } else {
                items(activePlaylistTracks) { track ->
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
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.0f)) {
                                AsyncImage(
                                    model = track.albumArtUri,
                                    contentDescription = "Cover",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp)),
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
                                        text = "${track.artist} • ${track.source}",
                                        color = Color(0x80FFFFFF),
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "✏️",
                                    fontSize = 18.sp,
                                    modifier = Modifier.clickable {
                                        showMetadataEditor = track
                                        newTitle = track.title
                                        newArtist = track.artist
                                        newAlbum = track.album
                                    }
                                )
                                Text(
                                    text = if (track.isFavorite) "❤️" else "🤍",
                                    fontSize = 18.sp,
                                    modifier = Modifier.clickable { viewModel.toggleFavorite(track.id) }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // General Library index
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AURORA MUSIC SYNC",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color(0x80FFFFFF)
                    )
                    GlassButton(
                        onClick = { viewModel.scanLocalFiles() },
                        cornerRadius = 8.dp,
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Scan Directory 🔍", fontSize = 11.sp, color = Color.White)
                    }
                }
            }

            // Playlists Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SMART PLAYLISTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = Color(0x80FFFFFF)
                    )
                    Text(
                        text = "Create Playlist ➕",
                        fontSize = 12.sp,
                        color = Color(0xFF00E5FF),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { showNewPlaylistDialog = true }
                    )
                }
            }

            items(playlists) { playlist ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectPlaylist(playlist.id) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (playlist.isSmart) "⚙️" else "🎵",
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = playlist.name,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = playlist.description,
                                color = Color(0x80FFFFFF),
                                fontSize = 12.sp
                            )
                        }
                        Text(text = "→", color = Color(0x66FFFFFF))
                    }
                }
            }

            // Local Directory Folders
            item {
                Text(
                    text = "FOLDER DETECTED SCANS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color(0x80FFFFFF)
                )
            }

            items(localFolders) { folder ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = folder.first,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = folder.second,
                                color = Color(0x66FFFFFF),
                                fontSize = 12.sp
                            )
                        }
                        Text(text = "Scan", color = Color(0xFF00E5FF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // --- New Playlist Creator Dialog ---
    if (showNewPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showNewPlaylistDialog = false },
            containerColor = Color(0xFF141722),
            title = { Text("New smart playlist configuration", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    GlassTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        placeholder = "Playlist name..."
                    )
                    GlassTextField(
                        value = newPlaylistDesc,
                        onValueChange = { newPlaylistDesc = it },
                        placeholder = "Details/Description..."
                    )
                }
            },
            confirmButton = {
                GlassButton(
                    onClick = {
                        if (newPlaylistName.isNotEmpty()) {
                            viewModel.createPlaylist(newPlaylistName, newPlaylistDesc)
                            newPlaylistName = ""
                            newPlaylistDesc = ""
                        }
                        showNewPlaylistDialog = false
                    },
                    cornerRadius = 8.dp
                ) {
                    Text("Assemble", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewPlaylistDialog = false }) {
                    Text("Close", color = Color(0x80FFFFFF))
                }
            }
        )
    }

    // --- Metadata ID3 Editor Custom Dialog ---
    if (showMetadataEditor != null) {
        val targetTrack = showMetadataEditor!!
        AlertDialog(
            onDismissRequest = { showMetadataEditor = null },
            containerColor = Color(0xFF141722),
            title = { Text("Metadata Tag Editor (ID3v2)", color = Color.White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Tag editor for ID3 metadata headers", color = Color(0x80FFFFFF), fontSize = 12.sp)
                    GlassTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        placeholder = "Title"
                    )
                    GlassTextField(
                        value = newArtist,
                        onValueChange = { newArtist = it },
                        placeholder = "Artist"
                    )
                    GlassTextField(
                        value = newAlbum,
                        onValueChange = { newAlbum = it },
                        placeholder = "Album"
                    )
                }
            },
            confirmButton = {
                GlassButton(
                    onClick = {
                        viewModel.updateTrackMetadata(targetTrack.id, newTitle, newArtist, newAlbum)
                        showMetadataEditor = null
                    },
                    cornerRadius = 8.dp
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMetadataEditor = null }) {
                    Text("Dismiss", color = Color(0xAAFFFFFF))
                }
            }
        )
    }
}
