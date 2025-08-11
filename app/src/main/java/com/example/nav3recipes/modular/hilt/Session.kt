package com.example.nav3recipes.modular.hilt

import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Session @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    companion object {
        private const val TAG = "Session"
        private const val SESSION_KEY = "session_active"
        private const val SESSION_TIMESTAMP_KEY = "session_start_time"
        private const val SESSION_TIMEOUT_MS: Long = 2 * 60 * 1000 // 2 minutes
    }

    suspend fun isSessionActive(): Boolean = withContext(Dispatchers.IO) {
        val isActive = sharedPreferences.getBoolean(SESSION_KEY, false)
        val sessionStart = sharedPreferences.getLong(SESSION_TIMESTAMP_KEY, 0L)
        val now = System.currentTimeMillis()

        val isValid = isActive && (now - sessionStart < SESSION_TIMEOUT_MS)

        if (isActive && !isValid) {
            Log.d(TAG, "Session expired, clearing saved state")
            endSession()
        }

        if (isValid) {
            Log.d(TAG, "Session is active, updating timestamp")
            updateTimestamp()
        }

        isValid
    }

    suspend fun startSession(): Unit = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        Log.d(TAG, "Starting session at $now")
        sharedPreferences.edit()
            .putBoolean(SESSION_KEY, true)
            .putLong(SESSION_TIMESTAMP_KEY, now)
            .apply()
    }

    suspend fun endSession(): Unit = withContext(Dispatchers.IO) {
        Log.d(TAG, "Ending session")
        sharedPreferences.edit()
            .putBoolean(SESSION_KEY, false)
            .remove(SESSION_TIMESTAMP_KEY)
            .apply()
    }

    private suspend fun updateTimestamp(): Unit = withContext(Dispatchers.IO) {
        if (sharedPreferences.getBoolean(SESSION_KEY, false)) {
            sharedPreferences.edit()
                .putLong(SESSION_TIMESTAMP_KEY, System.currentTimeMillis())
                .apply()
        }
    }

    suspend fun checkAndUpdateSession(): Boolean = withContext(Dispatchers.IO) {
        val isActive = isSessionActive()
        if (isActive) {
            updateTimestamp()
        }
        isActive
    }
}
