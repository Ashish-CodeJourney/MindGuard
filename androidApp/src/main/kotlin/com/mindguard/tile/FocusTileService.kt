package com.mindguard.tile

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.mindguard.shared.usecases.isPausedAt
import com.mindguard.storage.SettingsDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class FocusTileService : TileService(), KoinComponent {

    private val settingsDataStore: SettingsDataStore by inject()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var listenJob: Job? = null

    override fun onStartListening() {
        listenJob?.cancel()
        listenJob = serviceScope.launch {
            settingsDataStore.pauseUntilFlow.collect { pauseUntil ->
                withContext(Dispatchers.Main) { updateTileState(pauseUntil) }
            }
        }
    }

    override fun onStopListening() {
        listenJob?.cancel()
        listenJob = null
    }

    override fun onClick() {
        serviceScope.launch {
            val pauseUntil = settingsDataStore.pauseUntilFlow.first()
            if (isPausedAt(pauseUntil, System.currentTimeMillis())) {
                settingsDataStore.resumeFromPause()
            } else {
                settingsDataStore.pauseProtection(PAUSE_DURATION_MS)
            }
        }
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun updateTileState(pauseUntilMs: Long) {
        val tile = qsTile ?: return
        val isPaused = isPausedAt(pauseUntilMs, System.currentTimeMillis())
        tile.state = if (isPaused) Tile.STATE_INACTIVE else Tile.STATE_ACTIVE
        tile.label = if (isPaused) "Focus Paused" else "Focus Mode"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (isPaused) "Tap to resume" else "Tap to pause 30m"
        }
        tile.updateTile()
    }

    companion object {
        const val PAUSE_DURATION_MS = 30 * 60 * 1000L
    }
}
