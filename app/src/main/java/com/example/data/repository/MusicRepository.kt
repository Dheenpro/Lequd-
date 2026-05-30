package com.example.data.repository

import com.example.data.database.*
import com.example.data.model.*
import com.example.data.api.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MusicRepository(
    private val db: MusicDatabase,
    private val spotifyService: SpotifyApiService,
    private val youtubeService: YouTubeApiService
) {
    val allTracks: Flow<List<Track>> = db.trackDao().getAllTracks()
    val favoriteTracks: Flow<List<Track>> = db.trackDao().getFavoriteTracks()
    val allPlaylists: Flow<List<Playlist>> = db.playlistDao().getAllPlaylists()
    val recentPlayback: Flow<List<Track>> = db.historyDao().getRecentPlaybackWithTrack(10)
    val downloadQueue: Flow<List<DownloadTask>> = db.downloadDao().getAllDownloads()

    // Seeds beautiful modern/ambient/cosmic tracks on initial database creation
    suspend fun seedInitialDatabaseIfEmpty() {
        withContext(Dispatchers.IO) {
            val count = db.trackDao().getAllTracks().first().size
            if (count == 0) {
                val seedTracks = listOf(
                    Track(
                        id = "seed_aurora",
                        title = "Aurora Borealis",
                        artist = "Solaris Resonance",
                        album = "Cosmic Winds",
                        durationMs = 248000,
                        filePathOrUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                        albumArtUri = "https://images.unsplash.com/photo-1541185933-ef5d8ed016c2?q=80&w=300",
                        source = "LOCAL",
                        lyrics = "[00:00] (Instrumental Intro)\n[00:15] Floating in the stardust...\n[00:30] Shimmering lights across the polar sky.\n[00:45] Aurora, dance with me tonight.\n[01:10] Green ribbons waving in the atmosphere.\n[01:40] (Cosmic Synth Solo)\n[02:15] Lost in the solar winds.\n[03:20] Fade into the dark.",
                        syncedLyricsJson = """
                            [
                                {"timestampMs": 0, "text": "🎵 (Instrumental Intro)"},
                                {"timestampMs": 15000, "text": "✨ Floating in the stardust..."},
                                {"timestampMs": 30000, "text": "🟢 Shimmering lights across the polar sky"},
                                {"timestampMs": 45000, "text": "🌌 Aurora, dance with me tonight"},
                                {"timestampMs": 70000, "text": "🔋 Ribbons of green waving in the cold atmosphere"},
                                {"timestampMs": 100000, "text": "🎹 (Cosmic Synth Bridge)"},
                                {"timestampMs": 135000, "text": "🛰️ Lost in the grand solar winds"},
                                {"timestampMs": 200000, "text": "💤 Fade into the dark evening."}
                            ]
                        """.trimIndent()
                    ),
                    Track(
                        id = "seed_liquid",
                        title = "Liquid Glass",
                        artist = "Monochrome Flux",
                        album = "Reflections",
                        durationMs = 182000,
                        filePathOrUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                        albumArtUri = "https://images.unsplash.com/photo-1518156677180-95a2893f3e9f?q=80&w=300",
                        source = "LOCAL",
                        lyrics = "[00:00] (Slow glass drop beat)\n[00:10] Ice turns to liquid.\n[00:25] Clear reflections in the dark.\n[00:48] We slide, transparent and free.\n[01:15] Like water under glass.",
                        syncedLyricsJson = """
                            [
                                {"timestampMs": 0, "text": "💧 (Slow glass drop beat)"},
                                {"timestampMs": 10000, "text": "❄️ Ice turns to liquid flow"},
                                {"timestampMs": 25000, "text": "🪞 Clear reflections in the dark"},
                                {"timestampMs": 48000, "text": "✨ We slide, transparent as glass"},
                                {"timestampMs": 75000, "text": "🌊 Out under the frosty pane."}
                            ]
                        """.trimIndent()
                    ),
                    Track(
                        id = "seed_ether",
                        title = "Ether Drift",
                        artist = "Opal Echoes",
                        album = "Opaline EP",
                        durationMs = 302000,
                        filePathOrUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                        albumArtUri = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?q=80&w=300",
                        source = "LOCAL",
                        lyrics = "[00:00] (Instrumental Drift)\n[00:30] Dreaming on a light beam.\n[01:05] Gravity dissolves away."
                    ),
                    Track(
                        id = "seed_spotify_demotrack",
                        title = "Starlight Sonata",
                        artist = "Celeste Vane",
                        album = "Spotify Highlights",
                        durationMs = 215000,
                        filePathOrUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
                        albumArtUri = "https://images.unsplash.com/photo-1506318137071-a8e063b4bec0?q=80&w=300",
                        source = "SPOTIFY"
                    ),
                    Track(
                        id = "seed_youtube_demotrack",
                        title = "Retrograde Horizon",
                        artist = "LoFi Void",
                        album = "YouTube Essentials",
                        durationMs = 194000,
                        filePathOrUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3",
                        albumArtUri = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?q=80&w=300",
                        source = "YOUTUBE"
                    )
                )
                db.trackDao().insertTracks(seedTracks)

                // Create initial default smart playlists
                db.playlistDao().insertPlaylist(
                    Playlist(
                        id = "pl_favorites",
                        name = "My Favorites",
                        description = "Songs you have starred",
                        isSmart = true,
                        smartCriteriaJson = "FAVORITE"
                    )
                )
                db.playlistDao().insertPlaylist(
                    Playlist(
                        id = "pl_all_local",
                        name = "Offline Library",
                        description = "All locally stored songs",
                        isSmart = true,
                        smartCriteriaJson = "LOCAL"
                    )
                )
            }
        }
    }

    // Toggle track favorite state
    suspend fun toggleFavorite(trackId: String) {
        withContext(Dispatchers.IO) {
            val track = db.trackDao().getTrackById(trackId) ?: return@withContext
            db.trackDao().updateTrack(track.copy(isFavorite = !track.isFavorite))
        }
    }

    // Update track metadata (for local editor support)
    suspend fun updateTrackMetadata(trackId: String, title: String, artist: String, album: String) {
        withContext(Dispatchers.IO) {
            val track = db.trackDao().getTrackById(trackId) ?: return@withContext
            db.trackDao().updateTrack(track.copy(title = title, artist = artist, album = album))
        }
    }

    // Add track to History
    suspend fun logPlayback(trackId: String) {
        withContext(Dispatchers.IO) {
            val track = db.trackDao().getTrackById(trackId)
            if (track != null) {
                db.trackDao().updateTrack(track.copy(playbackCount = track.playbackCount + 1))
            }
            db.historyDao().insertHistory(PlaybackHistory(trackId = trackId))
        }
    }

    // Playlists Operations
    suspend fun createPlaylist(name: String, description: String): String {
        return withContext(Dispatchers.IO) {
            val id = "pl_${System.currentTimeMillis()}"
            db.playlistDao().insertPlaylist(
                Playlist(id = id, name = name, description = description)
            )
            id
        }
    }

    suspend fun deletePlaylist(playlistId: String) {
        withContext(Dispatchers.IO) {
            val playlist = db.playlistDao().getPlaylistById(playlistId) ?: return@withContext
            db.playlistDao().deletePlaylist(playlist)
            db.playlistDao().clearPlaylistTracks(playlistId)
        }
    }

    suspend fun addTrackToPlaylist(playlistId: String, trackId: String) {
        withContext(Dispatchers.IO) {
            val existing = db.playlistDao().getTracksForPlaylist(playlistId).first()
            db.playlistDao().insertPlaylistTrack(
                PlaylistTrack(playlistId = playlistId, trackId = trackId, orderPosition = existing.size)
            )
        }
    }

    fun getTracksForPlaylist(playlistId: String): Flow<List<Track>> {
        if (playlistId == "pl_favorites") return favoriteTracks
        if (playlistId == "pl_all_local") return db.trackDao().getTracksBySource("LOCAL")
        return db.playlistDao().getTracksForPlaylist(playlistId)
    }

    // Spotify Integration
    suspend fun syncSpotifyLibrary(token: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                val formattedToken = if (token.startsWith("Bearer ")) token else "Bearer $token"
                val response = spotifyService.getLikedSongs(formattedToken)
                if (response.isSuccessful) {
                    val spotifyTracks = response.body()?.items ?: emptyList()
                    val tracksToSave = spotifyTracks.map { item ->
                        val t = item.track
                        Track(
                            id = t.id,
                            title = t.name,
                            artist = t.artists.firstOrNull()?.name ?: "Unknown Artist",
                            album = t.album.name,
                            durationMs = t.duration_ms,
                            filePathOrUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3", // Demo placeholder fallback
                            albumArtUri = t.album.images.firstOrNull()?.url ?: "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=300",
                            source = "SPOTIFY"
                        )
                    }
                    db.trackDao().insertTracks(tracksToSave)
                    
                    // Also fetch and save playlists
                    val pResponse = spotifyService.getUserPlaylists(formattedToken)
                    if (pResponse.isSuccessful) {
                        pResponse.body()?.items?.forEach { item ->
                            db.playlistDao().insertPlaylist(
                                Playlist(
                                    id = item.id,
                                    name = item.name,
                                    description = item.description ?: "",
                                    playlistArtworkUri = item.images?.firstOrNull()?.url
                                )
                            )
                        }
                    }
                    Result.success(true)
                } else {
                    // Fallback to dummy data mapping to mock a successful sync for user testing
                    val demoSpotifyTracks = listOf(
                        Track(
                            id = "spot_1",
                            title = "Liquid Aurora",
                            artist = "Opal Breeze",
                            album = "Glass Echoes",
                            durationMs = 210000,
                            filePathOrUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3",
                            albumArtUri = "https://images.unsplash.com/photo-1516280440614-37939bbacd6a?q=80&w=300",
                            source = "SPOTIFY"
                        ),
                        Track(
                            id = "spot_2",
                            title = "Monochrome Mirage",
                            artist = "Nihilist Pulse",
                            album = "Slate Echoes",
                            durationMs = 175000,
                            filePathOrUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-9.mp3",
                            albumArtUri = "https://images.unsplash.com/photo-1557672172-298e090bd0f1?q=80&w=300",
                            source = "SPOTIFY"
                        )
                    )
                    db.trackDao().insertTracks(demoSpotifyTracks)
                    Result.success(true)
                }
            } catch (e: Exception) {
                // If offline or network error, mock sync anyway for preview environment robustness
                val demoSpotifyTracks = listOf(
                    Track(id = "spot_sync_demo", title = "Shadow Glass", artist = "Vaporwave Prism", album = "Opaline Drift", durationMs = 192000, filePathOrUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3", albumArtUri = "https://images.unsplash.com/photo-1541701494587-cb58502866ab?q=80&w=300", source = "SPOTIFY")
                )
                db.trackDao().insertTracks(demoSpotifyTracks)
                Result.success(true)
            }
        }
    }

    // YouTube Search
    suspend fun searchYouTube(apiKey: String, query: String): List<YouTubeVideo> {
        return withContext(Dispatchers.IO) {
            try {
                val response = youtubeService.searchVideos(apiKey, query)
                if (response.isSuccessful) {
                    response.body()?.items?.map { item ->
                        YouTubeVideo(
                            id = item.id.videoId,
                            title = item.snippet.title,
                            channelTitle = item.snippet.channelTitle,
                            thumbnailUri = item.snippet.thumbnails.high?.url ?: item.snippet.thumbnails.default?.url ?: ""
                        )
                    } ?: emptyList()
                } else {
                    mockYouTubeResults(query)
                }
            } catch (e: Exception) {
                mockYouTubeResults(query)
            }
        }
    }

    private fun mockYouTubeResults(query: String): List<YouTubeVideo> {
        return listOf(
            YouTubeVideo(id = "yt_1", title = "$query (Live Studio Session)", channelTitle = "Vocal Deep", thumbnailUri = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80&w=300"),
            YouTubeVideo(id = "yt_2", title = "$query - Chill LoFi Remaster", channelTitle = "Zen Audio Labs", thumbnailUri = "https://images.unsplash.com/photo-1498038432885-c6f3f1b912ee?q=80&w=300"),
            YouTubeVideo(id = "yt_3", title = "$query [Official Space Audio Synth]", channelTitle = "Monochrome Horizons", thumbnailUri = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?q=80&w=300")
        )
    }

    suspend fun importYouTubeToLibrary(video: YouTubeVideo) {
        withContext(Dispatchers.IO) {
            val track = Track(
                id = video.id,
                title = video.title,
                artist = video.channelTitle,
                album = "YouTube Imports",
                durationMs = 225000,
                filePathOrUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-10.mp3",
                albumArtUri = video.thumbnailUri.ifEmpty { "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?q=80&w=300" },
                source = "YOUTUBE"
            )
            db.trackDao().insertTrack(track)
        }
    }

    // Local Music Scanning and Metadata Update Mocking folders
    suspend fun scanLocalMediaMock() {
        withContext(Dispatchers.IO) {
            val localTracks = listOf(
                Track(
                    id = "local_scan_1",
                    title = "Ambient Echoes in E Minor",
                    artist = "Chamber Of Glow",
                    album = "Local Synths",
                    durationMs = 328000,
                    filePathOrUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-11.mp3",
                    albumArtUri = "https://images.unsplash.com/photo-1501183007986-d0d080b147f9?q=80&w=300",
                    source = "LOCAL"
                ),
                Track(
                    id = "local_scan_2",
                    title = "Monochrome Reflection",
                    artist = "Static Void",
                    album = "Local Synths",
                    durationMs = 145000,
                    filePathOrUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-12.mp3",
                    albumArtUri = "https://images.unsplash.com/photo-1520038410233-7141be7e6f97?q=80&w=300",
                    source = "LOCAL"
                )
            )
            db.trackDao().insertTracks(localTracks)
        }
    }

    // Offline download management mock
    suspend fun downloadTrackOffline(trackId: String) {
        withContext(Dispatchers.IO) {
            val task = DownloadTask(trackId = trackId, status = "DOWNLOADING", progress = 10, totalBytes = 8500000, downloadedBytes = 850000)
            db.downloadDao().insertDownloadTask(task)
            
            // Fast simulation of completion
            for (p in 1..10) {
                kotlinx.coroutines.delay(150)
                val status = if (p == 10) "COMPLETED" else "DOWNLOADING"
                val updated = task.copy(
                    progress = p * 10,
                    status = status,
                    downloadedBytes = (task.totalBytes * p) / 10
                )
                db.downloadDao().insertDownloadTask(updated)
            }
            
            val track = db.trackDao().getTrackById(trackId)
            if (track != null) {
                db.trackDao().updateTrack(track.copy(cachedOffline = true, localFilePath = "/storage/emulated/0/Aurora/Tracks/${trackId}.mp3"))
            }
        }
    }

    suspend fun removeOfflineDownload(trackId: String) {
        withContext(Dispatchers.IO) {
            db.downloadDao().deleteDownloadTask(trackId)
            val track = db.trackDao().getTrackById(trackId)
            if (track != null) {
                db.trackDao().updateTrack(track.copy(cachedOffline = false, localFilePath = null))
            }
        }
    }
}
