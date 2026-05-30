package com.example

import android.app.Application
import com.example.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuroraApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        // Seed initial tracks on background thread from start
        CoroutineScope(Dispatchers.IO).launch {
            container.musicRepository.seedInitialDatabaseIfEmpty()
        }
    }
}
