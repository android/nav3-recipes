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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.play.core.ktx.bytesDownloaded
import com.google.android.play.core.ktx.sessionId
import com.google.android.play.core.ktx.status
import com.google.android.play.core.ktx.totalBytesToDownload
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import kotlin.math.roundToInt

@Composable
fun retainDynamicFeatureManager(): DynamicFeatureManager {
    val applicationContext = LocalContext.current.applicationContext

    return retain {
        DynamicFeatureManager(applicationContext)
    }
}

class DynamicFeatureManager(context: Context) {
    private val splitInstallManager = SplitInstallManagerFactory.create(context)
    private var listener: SplitInstallStateUpdatedListener? = null

    var sessionId by mutableStateOf<Int?>(null)
        private set

    var downloadState by mutableStateOf<DownloadState?>(null)
        private set

    fun installModule(moduleName: String, onModuleInstalled: () -> Unit) {
        if (splitInstallManager.installedModules.contains(moduleName)) {
            onModuleInstalled()
            return
        }

        listener = SplitInstallStateUpdatedListener { state ->
            if (state.sessionId == sessionId) {
                when (state.status) {
                    SplitInstallSessionStatus.DOWNLOADING -> {
                        downloadState = DownloadState(
                            bytesDownloaded = state.bytesDownloaded,
                            totalBytesToDownload = state.totalBytesToDownload,
                        )
                    }

                    SplitInstallSessionStatus.INSTALLED -> {
                        downloadState = null
                        sessionId = null
                        unregisterListener()
                        onModuleInstalled()
                    }

                    else -> {}
                }
            }
        }.also(splitInstallManager::registerListener)

        splitInstallManager.startInstall(
            SplitInstallRequest.newBuilder().addModule(moduleName).build()
        ).addOnSuccessListener {
            sessionId = it
        }
    }

    fun cancelInstallModule() {
        sessionId?.let {
            splitInstallManager.cancelInstall(it).addOnSuccessListener {
                downloadState = null
                sessionId = null
                unregisterListener()
            }
        }
    }

    private fun unregisterListener() {
        listener?.let(splitInstallManager::unregisterListener)
    }

    data class DownloadState(
        val bytesDownloaded: Long,
        val totalBytesToDownload: Long,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicFeatureDownloadProgressDialog(
    manager: DynamicFeatureManager,
    modifier: Modifier = Modifier,
) {
    val sessionId = manager.sessionId
    val state = manager.downloadState
    val progress = if (state != null) {
        state.bytesDownloaded.toFloat() / state.totalBytesToDownload
    } else {
        0f
    }
    val progressPercentage = "${(progress * 100).roundToInt()}%"

    if (sessionId != null) {
        BasicAlertDialog(
            onDismissRequest = {},
            modifier = modifier,
        ) {
            Surface(shape = MaterialTheme.shapes.large) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = "Downloading module...",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(progress = { progress })
                        Text(
                            text = progressPercentage,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    Button(onClick = manager::cancelInstallModule) {
                        Text(text = "Cancel")
                    }
                }
            }
        }
    }
}
