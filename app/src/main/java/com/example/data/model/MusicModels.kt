package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.JsonClass

@Entity(tableName = "tracks")
@JsonClass(generateAdapter = true)
data class Track(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val filePathOrUrl: String,
    val albumArtUri: String,
    val source: String, // "LOCAL", "SPOTIFY", "YOUTUBE"
    val isFavorite: Boolean = false,
    val playbackCount: Int = 0,
    val cachedOffline: Boolean = false,
    val localFilePath: String? = null,
    val artistId: String = "",
    val albumId: String = "",
    val lyrics: String? = null,
    val syncedLyricsJson: String? = null // JSON holding List<SyncedLyricLine>
)

@Entity(tableName = "playlists")
@JsonClass(generateAdapter = true)
data class Playlist(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val isSmart: Boolean = false,
    val smartCriteriaJson: String? = null, // Smart criteria configurations
    val isCustomArtwork: Boolean = false,
    val playlistArtworkUri: String? = null
)

@Entity(tableName = "playlist_tracks", primaryKeys = ["playlistId", "trackId"])
data class PlaylistTrack(
    val playlistId: String,
    val trackId: String,
    val orderPosition: Int
)

@Entity(tableName = "playback_history")
data class PlaybackHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackId: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "download_queue")
data class DownloadTask(
    @PrimaryKey val trackId: String,
    val progress: Int = 0, // 0 - 100
    val status: String, // "PENDING", "DOWNLOADING", "COMPLETED", "FAILED"
    val totalBytes: Long = 0,
    val downloadedBytes: Long = 0
)

data class SyncedLyricLine(
    val timestampMs: Long,
    val text: String
)

data class Artist(
    val id: String,
    val name: String,
    val imageUrl: String,
    val spotifyFollowed: Boolean = false
)

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val imageUrl: String,
    val spotifySaved: Boolean = false
)

data class SpotifyProfile(
    val id: String,
    val displayName: String,
    val email: String,
    val imageUrl: String,
    val product: String // "premium", "free"
)

data class YouTubeVideo(
    val id: String,
    val title: String,
    val channelTitle: String,
    val thumbnailUri: String,
    val durationString: String = "3:45"
)
