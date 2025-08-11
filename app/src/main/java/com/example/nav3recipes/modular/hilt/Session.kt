package com.example.nav3recipes.modular.hilt

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class Account(val uid: String) {
    companion object {
        val ANONYMOUS = Account("anonymous")
    }

    val isAuthenticated: Boolean
        get() = this != ANONYMOUS
}

interface Session {
    val account: Flow<Account>
    suspend fun login(uid: String = UUID.randomUUID().toString())
    suspend fun logout()
}

@Singleton
internal class SessionImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : Session {

    companion object {
        private const val TAG = "SessionImpl"
        private const val SESSION_KEY = "session_active"
        private const val SESSION_UID_KEY = "session_uid"
        private const val SESSION_TIMESTAMP_KEY = "session_start_time"
        private const val SESSION_TIMEOUT_MS: Long = 2 * 60 * 1000 // 2 minutes
        private const val SESSION_CHECK_INTERVAL_MS: Long = 1000 // 1 second
    }

    override val account: Flow<Account> = flow {
        Log.d(TAG, "Account flow started - checking initial session state")

        // Cold flow - check session validity on subscription
        val initialAccount = withContext(Dispatchers.IO) {
            checkSessionValidity()
        }

        emit(initialAccount)

        // If authenticated, start timer-based checking
        if (initialAccount.isAuthenticated) {
            Log.d(TAG, "Starting timer-based session checking")

            try {
                while (true) {
                    delay(SESSION_CHECK_INTERVAL_MS)

                    val currentAccount = withContext(Dispatchers.IO) {
                        checkSessionValidity()
                    }

                    emit(currentAccount)

                    // Stop timer if session expired
                    if (!currentAccount.isAuthenticated) {
                        Log.d(TAG, "Session expired, stopping timer")
                        break
                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Session checking cancelled: ${e.message}")
            }
        } else {
            Log.d(TAG, "No active session, no timer needed")
        }
    }.distinctUntilChanged()

    private fun checkSessionValidity(): Account {
        val isActive = sharedPreferences.getBoolean(SESSION_KEY, false)
        val sessionUid = sharedPreferences.getString(SESSION_UID_KEY, null)
        val sessionStart = sharedPreferences.getLong(SESSION_TIMESTAMP_KEY, 0L)
        val now = System.currentTimeMillis()

        val isValid = isActive &&
                sessionUid != null &&
                (now - sessionStart < SESSION_TIMEOUT_MS)

        if (isActive && !isValid) {
            Log.d(TAG, "Session expired, clearing saved state")
            clearSession()
            return Account.ANONYMOUS
        }

        return if (isValid) Account(sessionUid!!) else Account.ANONYMOUS
    }

    private fun clearSession() {
        sharedPreferences.edit()
            .putBoolean(SESSION_KEY, false)
            .remove(SESSION_UID_KEY)
            .remove(SESSION_TIMESTAMP_KEY)
            .apply()
    }

    override suspend fun login(uid: String) = withContext(Dispatchers.IO) {
        Log.d(TAG, "User logged in: $uid")
        val now = System.currentTimeMillis()

        sharedPreferences.edit()
            .putBoolean(SESSION_KEY, true)
            .putString(SESSION_UID_KEY, uid)
            .putLong(SESSION_TIMESTAMP_KEY, now)
            .apply()
    }

    override suspend fun logout() = withContext(Dispatchers.IO) {
        Log.d(TAG, "User logged out")
        clearSession()
    }
}

@Module
@InstallIn(SingletonComponent::class)
object SessionModule {
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("nav_session_prefs", Context.MODE_PRIVATE)
    }

    @Provides
    @Singleton
    fun provideSession(sharedPreferences: SharedPreferences): Session {
        return SessionImpl(sharedPreferences)
    }
}
