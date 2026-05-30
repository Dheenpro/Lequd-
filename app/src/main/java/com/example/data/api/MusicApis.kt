package com.example.data.api

import retrofit2.http.*
import retrofit2.Response

// --- Spotify API ---

interface SpotifyApiService {
    @GET("v1/me")
    suspend fun getProfile(
        @Header("Authorization") token: String
    ): Response<SpotifyMeResponse>

    @GET("v1/me/playlists")
    suspend fun getUserPlaylists(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 20
    ): Response<SpotifyPlaylistsResponse>

    @GET("v1/me/tracks")
    suspend fun getLikedSongs(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 50
    ): Response<SpotifyTracksResponse>

    @GET("v1/me/albums")
    suspend fun getSavedAlbums(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 20
    ): Response<SpotifyAlbumsResponse>

    @GET("v1/me/following")
    suspend fun getFollowedArtists(
        @Header("Authorization") token: String,
        @Query("type") type: String = "artist",
        @Query("limit") limit: Int = 20
    ): Response<SpotifyArtistsResponse>
}

data class SpotifyMeResponse(
    val id: String,
    val display_name: String?,
    val email: String?,
    val images: List<SpotifyImage>?,
    val product: String?
)

data class SpotifyImage(
    val url: String,
    val height: Int?,
    val width: Int?
)

data class SpotifyPlaylistsResponse(
    val items: List<SpotifyPlaylistObject>
)

data class SpotifyPlaylistObject(
    val id: String,
    val name: String,
    val description: String?,
    val images: List<SpotifyImage>?
)

data class SpotifyTracksResponse(
    val items: List<SpotifySavedTrackObject>
)

data class SpotifySavedTrackObject(
    val track: SpotifyTrackObject
)

data class SpotifyTrackObject(
    val id: String,
    val name: String,
    val duration_ms: Long,
    val artists: List<SpotifyArtistObject>,
    val album: SpotifyAlbumObject
)

data class SpotifyArtistObject(
    val id: String,
    val name: String
)

data class SpotifyAlbumObject(
    val id: String,
    val name: String,
    val images: List<SpotifyImage>
)

data class SpotifyAlbumsResponse(
    val items: List<SpotifySavedAlbumObject>
)

data class SpotifySavedAlbumObject(
    val album: SpotifyAlbumObject
)

data class SpotifyArtistsResponse(
    val artists: SpotifyArtistsListWrapper
)

data class SpotifyArtistsListWrapper(
    val items: List<SpotifyArtistFullObject>
)

data class SpotifyArtistFullObject(
    val id: String,
    val name: String,
    val images: List<SpotifyImage>?
)


// --- YouTube API ---

interface YouTubeApiService {
    @GET("youtube/v3/search")
    suspend fun searchVideos(
        @Query("key") apiKey: String,
        @Query("q") query: String,
        @Query("part") part: String = "snippet",
        @Query("type") type: String = "video",
        @Query("videoCategoryId") categoryId: String = "10", // Music
        @Query("maxResults") maxResults: Int = 25
    ): Response<YouTubeSearchResponse>

    @GET("youtube/v3/playlists")
    suspend fun getPlaylists(
        @Query("key") apiKey: String,
        @Query("part") part: String = "snippet",
        @Query("mine") mine: Boolean = true,
        @Header("Authorization") token: String? = null
    ): Response<YouTubePlaylistsResponse>

    @GET("youtube/v3/playlistItems")
    suspend fun getPlaylistItems(
        @Query("key") apiKey: String,
        @Query("playlistId") playlistId: String,
        @Query("part") part: String = "snippet",
        @Query("maxResults") maxResults: Int = 50
    ): Response<YouTubePlaylistItemsResponse>
}

data class YouTubeSearchResponse(
    val items: List<YouTubeSearchResult>
)

data class YouTubeSearchResult(
    val id: YouTubeVideoId,
    val snippet: YouTubeSnippet
)

data class YouTubeVideoId(
    val videoId: String
)

data class YouTubeSnippet(
    val title: String,
    val channelTitle: String,
    val thumbnails: YouTubeThumbnails
)

data class YouTubeThumbnails(
    val default: YouTubeThumbnailDetails?,
    val medium: YouTubeThumbnailDetails?,
    val high: YouTubeThumbnailDetails?
)

data class YouTubeThumbnailDetails(
    val url: String
)

data class YouTubePlaylistsResponse(
    val items: List<YouTubePlaylistResult>
)

data class YouTubePlaylistResult(
    val id: String,
    val snippet: YouTubeSnippet
)

data class YouTubePlaylistItemsResponse(
    val items: List<YouTubePlaylistItemResult>
)

data class YouTubePlaylistItemResult(
    val id: String,
    val snippet: YouTubePlaylistItemSnippet
)

data class YouTubePlaylistItemSnippet(
    val title: String,
    val resourceId: YouTubeVideoId,
    val thumbnails: YouTubeThumbnails
)
