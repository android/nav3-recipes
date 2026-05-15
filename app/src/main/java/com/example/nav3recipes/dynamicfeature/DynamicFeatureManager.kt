/*
 * Copyright 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.nav3recipes.dynamicfeature

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.play.core.ktx.bytesDownloaded
import com.google.android.play.core.ktx.errorCode
import com.google.android.play.core.ktx.sessionId
import com.google.android.play.core.ktx.status
import com.google.android.play.core.ktx.totalBytesToDownload
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus

@Composable
fun retainDynamicFeatureManager(): DynamicFeatureManager {
    val applicationContext = LocalContext.current.applicationContext

    val manager = retain {
        DynamicFeatureManager(applicationContext)
    }

    DisposableEffect(manager) {
        onDispose {
            manager.dispose()
        }
    }

    return manager
}

sealed interface InstallStatus {
    data object Idle : InstallStatus
    data class Pending(val sessionId: Int) : InstallStatus
    data class Downloading(val sessionId: Int, val progress: Float) : InstallStatus
    data class Installing(val sessionId: Int) : InstallStatus
    data class RequiresUserConfirmation(val state: SplitInstallSessionState) : InstallStatus
    data class Failed(val sessionId: Int, val errorCode: Int) : InstallStatus
}

@Stable
class DynamicFeatureManager(context: Context) {
    private val splitInstallManager = SplitInstallManagerFactory.create(context)

    // Maintain a single listener for the lifetime of the manager
    private val listener = SplitInstallStateUpdatedListener { state ->
        val sessionId = state.sessionId
        // Capture session ID if we're expecting an install but haven't got the ID yet (race condition)
        if (activeSessionId == null && activeModuleName != null) {
            if (state.moduleNames().contains(activeModuleName)) {
                activeSessionId = sessionId
            }
        }

        if (sessionId == activeSessionId) {
            updateStatus(state)
        }
    }

    private var activeSessionId: Int? = null
    private var activeModuleName: String? = null
    private var onModuleInstalledCallback: (() -> Unit)? = null

    var status by mutableStateOf<InstallStatus>(InstallStatus.Idle)
        private set

    var installedModules: Set<String> by mutableStateOf(splitInstallManager.installedModules.toSet())
        private set

    init {
        splitInstallManager.registerListener(listener)
    }

    fun dispose() {
        splitInstallManager.unregisterListener(listener)
    }

    fun installModule(moduleName: String, onModuleInstalled: () -> Unit) {
        if (splitInstallManager.installedModules.contains(moduleName)) {
            onModuleInstalled()
            return
        }

        // Avoid starting multiple installs if one is already in progress
        if (status !is InstallStatus.Idle && status !is InstallStatus.Failed) return

        activeModuleName = moduleName
        onModuleInstalledCallback = onModuleInstalled
        // Use a placeholder ID to indicate we are waiting for a session
        status = InstallStatus.Pending(-1)

        splitInstallManager.startInstall(
            SplitInstallRequest.newBuilder().addModule(moduleName).build()
        ).addOnSuccessListener { sessionId ->
            if (activeSessionId == null) {
                activeSessionId = sessionId
            }
            // Only update status if it hasn't progressed beyond our placeholder
            if (status is InstallStatus.Pending && (status as InstallStatus.Pending).sessionId == -1) {
                status = InstallStatus.Pending(sessionId)
            }
        }.addOnFailureListener {
            status = InstallStatus.Failed(-1, -1)
            onModuleInstalledCallback = null
        }
    }

    private fun clearSessionState() {
        activeSessionId = null
        activeModuleName = null
        onModuleInstalledCallback = null
    }

    private fun updateStatus(state: SplitInstallSessionState) {
        val sessionId = state.sessionId
        when (state.status) {
            SplitInstallSessionStatus.PENDING -> {
                status = InstallStatus.Pending(sessionId)
            }

            SplitInstallSessionStatus.DOWNLOADING -> {
                val progress = if (state.totalBytesToDownload > 0) {
                    state.bytesDownloaded.toFloat() / state.totalBytesToDownload
                } else 0f
                status = InstallStatus.Downloading(sessionId, progress)
            }

            SplitInstallSessionStatus.DOWNLOADED -> {
                status = InstallStatus.Installing(sessionId)
            }

            SplitInstallSessionStatus.INSTALLING -> {
                status = InstallStatus.Installing(sessionId)
            }

            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                status = InstallStatus.RequiresUserConfirmation(state)
            }

            SplitInstallSessionStatus.INSTALLED -> {
                status = InstallStatus.Idle
                installedModules = splitInstallManager.installedModules.toSet()
                onModuleInstalledCallback?.invoke()
                clearSessionState()
            }

            SplitInstallSessionStatus.FAILED -> {
                status = InstallStatus.Failed(sessionId, state.errorCode)
                clearSessionState()
            }

            SplitInstallSessionStatus.CANCELING -> {
                status = InstallStatus.Pending(sessionId)
            }

            SplitInstallSessionStatus.CANCELED -> {
                status = InstallStatus.Idle
                clearSessionState()
            }

            SplitInstallSessionStatus.UNKNOWN -> {
                status = InstallStatus.Failed(sessionId, -1)
                clearSessionState()
            }
        }
    }

    fun startConfirmationDialogForResult(
        state: SplitInstallSessionState,
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        splitInstallManager.startConfirmationDialogForResult(state, launcher)
    }

    fun cancelInstallModule() {
        activeSessionId?.let { id ->
            splitInstallManager.cancelInstall(id)
        }
        status = InstallStatus.Idle
        clearSessionState()
    }
}
