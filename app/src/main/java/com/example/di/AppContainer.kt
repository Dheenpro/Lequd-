package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.database.MusicDatabase
import com.example.data.repository.MusicRepository
import com.example.data.api.*
import com.example.player.AudioPlayerManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class AppContainer(private val context: Context) {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    // Real Spotify Retrofit integration
    private val spotifyRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.spotify.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val spotifyService: SpotifyApiService = spotifyRetrofit.create(SpotifyApiService::class.java)

    // Real Google/YouTube API Retrofit integration
    private val googleRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://www.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val youtubeService: YouTubeApiService = googleRetrofit.create(YouTubeApiService::class.java)

    // Room Database
    val database: MusicDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            MusicDatabase::class.java,
            "aurora_music_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    // Repository
    val musicRepository: MusicRepository by lazy {
        MusicRepository(
            db = database,
            spotifyService = spotifyService,
            youtubeService = youtubeService
        )
    }

    // Playback Engine
    val audioPlayerManager: AudioPlayerManager by lazy {
        AudioPlayerManager(context)
    }
}
