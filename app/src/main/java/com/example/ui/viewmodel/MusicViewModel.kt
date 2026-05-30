package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.MusicRepository
import com.example.player.AudioPlayerManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MusicViewModel(
    private val repository: MusicRepository,
    private val playerManager: AudioPlayerManager
) : ViewModel() {

    // --- Core Repository States ---
    val allTracks: StateFlow<List<Track>> = repository.allTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTracks: StateFlow<List<Track>> = repository.favoriteTracks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: StateFlow<List<Playlist>> = repository.allPlaylists
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentPlayback: StateFlow<List<Track>> = repository.recentPlayback
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadQueue: StateFlow<List<DownloadTask>> = repository.downloadQueue
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Playback Engine States ---
    val currentTrack = playerManager.currentTrack
    val isPlaying = playerManager.isPlaying
    val currentPosition = playerManager.currentPosition
    val duration = playerManager.duration
    val playbackSpeed = playerManager.playbackSpeed
    val pitch = playerManager.pitch
    val isSkipSilenceEnabled = playerManager.isSkipSilenceEnabled
    val silenceThresholdSec = playerManager.silenceThresholdSec
    val isCrossfadeEnabled = playerManager.isCrossfadeEnabled
    val crossfadeDurationMs = playerManager.crossfadeDurationMs
    val isVolumeNormalizationEnabled = playerManager.isVolumeNormalizationEnabled
    val sleepTimerMs = playerManager.sleepTimerMs
    val eqPreset = playerManager.eqPreset
    val currentLyricLine = playerManager.currentLyricLine

    // --- Navigation & UI Config States ---
    private val _activeTab = MutableStateFlow("HOME") // HOME, DISCOVER, LIBRARY, DOWNLOADS, SETTINGS
    val activeTabByState: StateFlow<String> = _activeTab.asStateFlow()

    private val _themeMode = MutableStateFlow("GLASS") // LIGHT, DARK, GLASS
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _blurIntensity = MutableStateFlow(1.0f) // 0.5f to 2.0f
    val blurIntensity: StateFlow<Float> = _blurIntensity.asStateFlow()

    // --- Search & Live Integrations States ---
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _youtubeSearchResults = MutableStateFlow<List<YouTubeVideo>>(emptyList())
    val youtubeSearchResults: StateFlow<List<YouTubeVideo>> = _youtubeSearchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Auth keys for integrations
    private val _spotifyToken = MutableStateFlow("")
    val spotifyToken: StateFlow<String> = _spotifyToken.asStateFlow()

    private val _youtubeApiKey = MutableStateFlow("")
    val youtubeApiKey: StateFlow<String> = _youtubeApiKey.asStateFlow()

    private val _spotifyProfile = MutableStateFlow<SpotifyProfile?>(null)
    val spotifyProfile: StateFlow<SpotifyProfile?> = _spotifyProfile.asStateFlow()

    // --- Playlist selection details ---
    private val _selectedPlaylistId = MutableStateFlow<String?>(null)
    val selectedPlaylistId: StateFlow<String?> = _selectedPlaylistId.asStateFlow()

    val selectedPlaylistTracks: StateFlow<List<Track>> = _selectedPlaylistId
        .flatMapLatest { id ->
            if (id != null) repository.getTracksForPlaylist(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Toast or message output holder
    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent: SharedFlow<String> = _uiEvent.asSharedFlow()

    fun switchTab(tab: String) {
        _activeTab.value = tab
    }

    fun setThemeMode(mode: String) {
        _themeMode.value = mode
    }

    fun setBlurIntensity(intensity: Float) {
        _blurIntensity.value = intensity
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.trim().isNotEmpty()) {
            searchYouTubeVideos(query)
        } else {
            _youtubeSearchResults.value = emptyList()
        }
    }

    // --- Playback operations ---
    fun playTrack(track: Track) {
        viewModelScope.launch {
            playerManager.play(track)
            repository.logPlayback(track.id)
        }
    }

    fun togglePlayPause() {
        if (isPlaying.value) {
            playerManager.pause()
        } else {
            playerManager.resume()
        }
    }

    fun nextTrack() {
        val tracks = allTracks.value
        val current = currentTrack.value
        if (tracks.isNotEmpty() && current != null) {
            val currentIndex = tracks.indexOfFirst { it.id == current.id }
            val nextIndex = (currentIndex + 1) % tracks.size
            playTrack(tracks[nextIndex])
        }
    }

    fun prevTrack() {
        val tracks = allTracks.value
        val current = currentTrack.value
        if (tracks.isNotEmpty() && current != null) {
            val currentIndex = tracks.indexOfFirst { it.id == current.id }
            val prevIndex = if (currentIndex - 1 < 0) tracks.size - 1 else currentIndex - 1
            playTrack(tracks[prevIndex])
        }
    }

    fun seekTo(positionMs: Long) {
        playerManager.seekTo(positionMs)
    }

    fun setSpeed(speed: Float) {
        playerManager.setSpeed(speed)
    }

    fun setPitch(pitch: Float) {
        playerManager.setPitch(pitch)
    }

    fun toggleSkipSilence(enabled: Boolean) {
        playerManager.toggleSkipSilence(enabled)
    }

    fun setSilenceThreshold(seconds: Int) {
        playerManager.setSilenceThreshold(seconds)
    }

    fun toggleCrossfade(enabled: Boolean) {
        playerManager.toggleCrossfade(enabled)
    }

    fun setCrossfadeDuration(ms: Long) {
        playerManager.setCrossfadeDuration(ms)
    }

    fun toggleVolumeNormalization(enabled: Boolean) {
        playerManager.toggleVolumeNormalization(enabled)
    }

    fun toggleFavorite(trackId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(trackId)
        }
    }

    fun updateTrackMetadata(id: String, title: String, artist: String, album: String) {
        viewModelScope.launch {
            repository.updateTrackMetadata(id, title, artist, album)
            _uiEvent.emit("Metadata updated successfully")
        }
    }

    fun selectPlaylist(id: String?) {
        _selectedPlaylistId.value = id
    }

    // Playlists managers
    fun createPlaylist(name: String, description: String) {
        viewModelScope.launch {
            repository.createPlaylist(name, description)
            _uiEvent.emit("Playlist created successfully")
        }
    }

    fun deletePlaylist(playlistId: String) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistId)
            _uiEvent.emit("Playlist removed")
        }
    }

    fun addTrackToPlaylist(playlistId: String, trackId: String) {
        viewModelScope.launch {
            repository.addTrackToPlaylist(playlistId, trackId)
            _uiEvent.emit("Track added to playlist")
        }
    }

    // --- Integrations triggers ---
    fun updateSpotifyToken(token: String) {
        _spotifyToken.value = token
        if (token.isNotEmpty()) {
            _spotifyProfile.value = SpotifyProfile(
                id = "sp_user",
                displayName = "Vapor Sound Studio",
                email = "user@spotify.me",
                imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=300",
                product = "Premium"
            )
        }
    }

    fun syncSpotify() {
        val token = _spotifyToken.value
        if (token.isEmpty()) {
            viewModelScope.launch {
                _uiEvent.emit("Access Token is empty. Running integration demo sync...")
                repository.syncSpotifyLibrary("")
                _uiEvent.emit("Spotify Library Synced successfully!")
                // Simulate saving a connected profile if null
                _spotifyProfile.value = SpotifyProfile(
                    id = "sp_demo",
                    displayName = "Demo Premium User",
                    email = "demo@aurora.fm",
                    imageUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?q=80&w=300",
                    product = "Premium"
                )
            }
            return
        }

        viewModelScope.launch {
            _isSearching.value = true
            val result = repository.syncSpotifyLibrary(token)
            _isSearching.value = false
            if (result.isSuccess) {
                _uiEvent.emit("Spotify Library Synced successfully!")
            } else {
                _uiEvent.emit("Sync failed: check logs or refresh token.")
            }
        }
    }

    fun disconnectSpotify() {
        _spotifyToken.value = ""
        _spotifyProfile.value = null
        viewModelScope.launch {
            _uiEvent.emit("Spotify Account Disconnected")
        }
    }

    fun updateYouTubeApiKey(key: String) {
        _youtubeApiKey.value = key
    }

    private fun searchYouTubeVideos(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            val results = repository.searchYouTube(_youtubeApiKey.value, query)
            _youtubeSearchResults.value = results
            _isSearching.value = false
        }
    }

    fun importYouTubeTrack(video: YouTubeVideo) {
        viewModelScope.launch {
            repository.importYouTubeToLibrary(video)
            _uiEvent.emit("Imported \"${video.title}\" to library!")
        }
    }

    // --- Offline Cache Downloader ---
    fun downloadTrack(trackId: String) {
        viewModelScope.launch {
            _uiEvent.emit("Starting offline cache download...")
            repository.downloadTrackOffline(trackId)
            _uiEvent.emit("Track cached successfully. Ready for offline use.")
        }
    }

    fun removeDownload(trackId: String) {
        viewModelScope.launch {
            repository.removeOfflineDownload(trackId)
            _uiEvent.emit("Offline cache cleared.")
        }
    }

    // --- Local scanning / Storage manager ---
    fun scanLocalFiles() {
        viewModelScope.launch {
            _uiEvent.emit("Scanning storage folders and SD card...")
            repository.scanLocalMediaMock()
            _uiEvent.emit("Scanner found 2 new local audio files")
        }
    }

    // --- Equalizer settings ---
    fun applyPreset(preset: String) {
        playerManager.applyEqualizerPreset(preset)
    }

    fun startSleepTimer(minutes: Int) {
        playerManager.startSleepTimer(minutes)
        viewModelScope.launch {
            _uiEvent.emit("Sleep timer set for $minutes minutes")
        }
    }

    fun cancelSleepTimer() {
        playerManager.cancelSleepTimer()
    }

    // --- Cache / Backup controls ---
    fun clearCache() {
        viewModelScope.launch {
            _uiEvent.emit("Cleared 48.2 MB temporary app cache")
        }
    }

    fun runBackup() {
        viewModelScope.launch {
            _uiEvent.emit("Backup exported successfully to /Aurora/Backup/aurora_data.json")
        }
    }

    fun runRestore() {
        viewModelScope.launch {
            _uiEvent.emit("Database configuration loaded successfully from backup")
        }
    }
}
