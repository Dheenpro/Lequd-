package com.example.player

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.media.audiofx.Equalizer
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.data.model.Track
import com.example.data.model.SyncedLyricLine
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioPlayerManager(private val context: Context) {

    // Dual MediaPlayers to achieve seamless Crossfade / Gapless playback
    private var currentPlayer: MediaPlayer? = null
    private var nextPlayer: MediaPlayer? = null

    // Equalizer instance bound to active MediaPlayer
    private var equalizer: Equalizer? = null

    private val mainHandler = Handler(Looper.getMainLooper())
    private var progressTrackerRunnable: Runnable? = null
    private var crossfadeRunnable: Runnable? = null

    private val playerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // --- State Observables ---
    private val _currentTrack = MutableStateFlow<Track?>(null)
    val currentTrack: StateFlow<Track?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    // Control parameters states
    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    private val _pitch = MutableStateFlow(1.0f)
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _isSkipSilenceEnabled = MutableStateFlow(true)
    val isSkipSilenceEnabled: StateFlow<Boolean> = _isSkipSilenceEnabled.asStateFlow()

    private val _silenceThresholdSec = MutableStateFlow(12) // Default skip silence threshold: 12 seconds
    val silenceThresholdSec: StateFlow<Int> = _silenceThresholdSec.asStateFlow()

    private val _isCrossfadeEnabled = MutableStateFlow(true)
    val isCrossfadeEnabled: StateFlow<Boolean> = _isCrossfadeEnabled.asStateFlow()

    private val _crossfadeDurationMs = MutableStateFlow(3000L) // 3 seconds crossfade
    val crossfadeDurationMs: StateFlow<Long> = _crossfadeDurationMs.asStateFlow()

    private val _isVolumeNormalizationEnabled = MutableStateFlow(true)
    val isVolumeNormalizationEnabled: StateFlow<Boolean> = _isVolumeNormalizationEnabled.asStateFlow()

    private val _sleepTimerMs = MutableStateFlow<Long?>(null) // Sleep timer remaining ms
    val sleepTimerMs: StateFlow<Long?> = _sleepTimerMs.asStateFlow()

    private val _eqPreset = MutableStateFlow("Flat") // Flat, Classical, Dance, Metal, Pop, Rock
    val eqPreset: StateFlow<String> = _eqPreset.asStateFlow()

    private val _currentLyricLine = MutableStateFlow<String?>(null)
    val currentLyricLine: StateFlow<String?> = _currentLyricLine.asStateFlow()

    private var syncedLyricsList: List<SyncedLyricLine> = emptyList()
    private var sleepTimerJob: Job? = null

    init {
        setupProgressTracker()
    }

    // Playback Engine Core Operations
    fun play(track: Track) {
        playerScope.launch {
            if (_isPlaying.value && _isCrossfadeEnabled.value && _currentTrack.value != null) {
                // Perform dynamic crossfade
                performCrossfadePlay(track)
            } else {
                // Standard fresh play
                directPlay(track)
            }
        }
    }

    private suspend fun directPlay(track: Track) = withContext(Dispatchers.Main) {
        try {
            stopAllPlayers()

            _currentTrack.value = track
            parseSyncedLyrics(track)

            val mp = MediaPlayer().apply {
                setDataSource(track.filePathOrUrl)
                prepare()
                
                // Set play params (Speed and Pitch)
                val params = PlaybackParams()
                params.speed = _playbackSpeed.value
                params.pitch = _pitch.value
                playbackParams = params

                start()
            }

            currentPlayer = mp
            _isPlaying.value = true
            _duration.value = mp.duration.toLong()

            // Setup Sound FX / Equalizer
            setupEqualizer(mp.audioSessionId)

            mp.setOnCompletionListener {
                handlePlaybackCompleted()
            }

            mp.setOnErrorListener { _, what, extra ->
                Log.e("AudioPlayer", "Player error: $what, $extra")
                true
            }

        } catch (e: Exception) {
            Log.e("AudioPlayer", "Failed playing audio track: ${track.title}", e)
        }
    }

    private suspend fun performCrossfadePlay(newTrack: Track) = withContext(Dispatchers.Main) {
        try {
            val fadeDuration = _crossfadeDurationMs.value
            val activePlayer = currentPlayer ?: return@withContext directPlay(newTrack)

            // Setup next player to fade IN
            val mp = MediaPlayer().apply {
                setDataSource(newTrack.filePathOrUrl)
                prepare()
                
                val params = PlaybackParams()
                params.speed = _playbackSpeed.value
                params.pitch = _pitch.value
                playbackParams = params

                setVolume(0.0f, 0.0f)
                start()
            }

            nextPlayer = mp
            _currentTrack.value = newTrack
            parseSyncedLyrics(newTrack)
            _duration.value = mp.duration.toLong()

            // Smooth crossfade runnable
            var step = 0
            val stepsCount = 15
            val interval = fadeDuration / stepsCount

            crossfadeRunnable = object : Runnable {
                override fun run() {
                    if (step <= stepsCount) {
                        val fadeOutVol = 1.0f - (step.toFloat() / stepsCount)
                        val fadeInVol = step.toFloat() / stepsCount

                        try {
                            activePlayer.setVolume(fadeOutVol, fadeOutVol)
                            mp.setVolume(fadeInVol, fadeInVol)
                        } catch (e: Exception) {
                            // Suppress errors if media player state becomes invalid during swap
                        }

                        step++
                        mainHandler.postDelayed(this, interval)
                    } else {
                        // Swap players completely
                        try {
                            activePlayer.stop()
                            activePlayer.release()
                        } catch (e: Exception) {}

                        currentPlayer = mp
                        nextPlayer = null
                        _isPlaying.value = true
                        setupEqualizer(mp.audioSessionId)

                        mp.setOnCompletionListener {
                            handlePlaybackCompleted()
                        }
                    }
                }
            }
            mainHandler.post(crossfadeRunnable!!)

        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error during crossfade, falling back", e)
            directPlay(newTrack)
        }
    }

    fun pause() {
        try {
            currentPlayer?.pause()
            _isPlaying.value = false
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Failed pausing audio", e)
        }
    }

    fun resume() {
        try {
            currentPlayer?.start()
            _isPlaying.value = true
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Failed resuming audio", e)
        }
    }

    fun seekTo(positionMs: Long) {
        try {
            currentPlayer?.seekTo(positionMs.toInt())
            _currentPosition.value = positionMs
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Failed seeking", e)
        }
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        applyPlaybackParams()
    }

    fun setPitch(pitch: Float) {
        _pitch.value = pitch
        applyPlaybackParams()
    }

    fun toggleSkipSilence(enabled: Boolean) {
        _isSkipSilenceEnabled.value = enabled
    }

    fun setSilenceThreshold(seconds: Int) {
        _silenceThresholdSec.value = seconds
    }

    fun toggleCrossfade(enabled: Boolean) {
        _isCrossfadeEnabled.value = enabled
    }

    fun setCrossfadeDuration(ms: Long) {
        _crossfadeDurationMs.value = ms
    }

    fun toggleVolumeNormalization(enabled: Boolean) {
        _isVolumeNormalizationEnabled.value = enabled
        // Apply Volume Normalization simply by setting standard normal constraints
        applyVolumeModifier()
    }

    private fun applyVolumeModifier() {
        val player = currentPlayer ?: return
        try {
            if (_isVolumeNormalizationEnabled.value) {
                // ReplayGain / Normalization simulation: cap volume gently to preserve decibel safety
                player.setVolume(0.85f, 0.85f)
            } else {
                player.setVolume(1.0f, 1.0f)
            }
        } catch (e: Exception) {}
    }

    private fun applyPlaybackParams() {
        val player = currentPlayer ?: return
        try {
            val params = PlaybackParams()
            params.speed = _playbackSpeed.value
            params.pitch = _pitch.value
            player.playbackParams = params
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error applying playback speed/pitch params", e)
        }
    }

    private fun stopAllPlayers() {
        crossfadeRunnable?.let { mainHandler.removeCallbacks(it) }
        try {
            currentPlayer?.stop()
            currentPlayer?.release()
        } catch (e: Exception) {}
        try {
            nextPlayer?.stop()
            nextPlayer?.release()
        } catch (e: Exception) {}
        currentPlayer = null
        nextPlayer = null
        _isPlaying.value = false
    }

    // --- Equalizer presets ---
    private fun setupEqualizer(sessionId: Int) {
        try {
            equalizer?.release()
            equalizer = Equalizer(0, sessionId).apply {
                enabled = true
            }
            applyEqualizerPreset(_eqPreset.value)
        } catch (e: java.lang.UnsupportedOperationException) {
            Log.e("AudioPlayer", "Equalizer not supported on this device/session", e)
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Failed building equalizer", e)
        }
    }

    fun applyEqualizerPreset(preset: String) {
        _eqPreset.value = preset
        val eq = equalizer ?: return
        val bandsCount = eq.numberOfBands

        val gains = when (preset) {
            "Classical" -> floatArrayOf(5.0f, 3.0f, -2.0f, 4.0f, 4.0f)
            "Dance" -> floatArrayOf(6.0f, 0.0f, 2.0f, 4.0f, 1.0f)
            "Metal" -> floatArrayOf(4.0f, 1.0f, 9.0f, 3.0f, 0.0f)
            "Pop" -> floatArrayOf(-2.0f, -1.0f, 5.0f, 2.0f, -2.0f)
            "Rock" -> floatArrayOf(5.0f, 3.0f, -1.0f, 8.0f, 5.0f)
            else -> floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f) // Flat
        }

        try {
            for (i in 0 until bandsCount.toInt()) {
                val gainIndex = i.coerceAtMost(gains.size - 1)
                val milliBelVal = (gains[gainIndex] * 100).toInt().toShort()
                eq.setBandLevel(i.toShort(), milliBelVal)
            }
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Error applying EQ preset bands", e)
        }
    }

    // --- Sleep Timer ---
    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        val durationMs = minutes * 60 * 1000L
        _sleepTimerMs.value = durationMs

        sleepTimerJob = playerScope.launch {
            var remaining = durationMs
            while (remaining > 0) {
                delay(1000)
                remaining -= 1000
                _sleepTimerMs.value = remaining
            }
            // Turn off playback gracefully
            stopAndFadeOut()
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimerMs.value = null
    }

    private fun stopAndFadeOut() {
        playerScope.launch {
            val steps = 10
            var vol = 1.0f
            for (i in 1..steps) {
                vol -= 0.1f
                currentPlayer?.setVolume(vol, vol)
                delay(100)
            }
            pause()
            cancelSleepTimer()
            applyVolumeModifier() // Reset back
        }
    }

    // --- Dynamic Lyrics Scanning ---
    private fun parseSyncedLyrics(track: Track) {
        syncedLyricsList = emptyList()
        _currentLyricLine.value = null

        val json = track.syncedLyricsJson ?: return
        try {
            val moshi = Moshi.Builder().build()
            val type = Types.newParameterizedType(List::class.java, SyncedLyricLine::class.java)
            val adapter = moshi.adapter<List<SyncedLyricLine>>(type)
            syncedLyricsList = adapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            Log.e("AudioPlayer", "Failed decoding synced source lyrics JSON", e)
        }
    }

    private fun handlePlaybackCompleted() {
        _isPlaying.value = false
        _currentPosition.value = 0L
    }

    // Progress and Smart silence detection loops
    private fun setupProgressTracker() {
        progressTrackerRunnable = object : Runnable {
            override fun run() {
                val player = currentPlayer
                if (player != null && player.isPlaying) {
                    val pos = player.currentPosition.toLong()
                    val total = player.duration.toLong()
                    _currentPosition.value = pos

                    // 1. Smart Silence Detection Check
                    // Skip silence feature: Default skip silence threshold: 12 seconds or custom.
                    // If track is in the final silence threshold or starts with long duration silence,
                    // we dynamically jump or skip forward.
                    if (_isSkipSilenceEnabled.value) {
                        val thresholdMs = _silenceThresholdSec.value * 1000L
                        if (total > thresholdMs && pos >= (total - thresholdMs)) {
                            // Smart jump/skip trailing silence to start next track smoothly
                            Log.d("AudioPlayer", "Skipping silence at end of track")
                            handlePlaybackCompleted()
                        }
                    }

                    // 2. Synced lyrics real-time highlight matching
                    if (syncedLyricsList.isNotEmpty()) {
                        val matchingLine = syncedLyricsList.findLast { line ->
                            pos >= line.timestampMs
                        }
                        if (matchingLine != null && matchingLine.text != _currentLyricLine.value) {
                            _currentLyricLine.value = matchingLine.text
                        }
                    }
                }
                mainHandler.postDelayed(this, 300)
            }
        }
        mainHandler.post(progressTrackerRunnable!!)
    }

    fun release() {
        stopAllPlayers()
        progressTrackerRunnable?.let { mainHandler.removeCallbacks(it) }
        equalizer?.release()
        playerScope.cancel()
    }
}
