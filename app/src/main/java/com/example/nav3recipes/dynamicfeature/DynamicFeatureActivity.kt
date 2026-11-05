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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.dynamicfeatures.rememberDynamicFeatureNavEntryDecorator
import androidx.navigation3.dynamicfeatures.rememberSplitInstallManager
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.serialization.Serializable

@Serializable
internal data object Home : NavKey

/** Initial nav key of install-time dynamic module. */
@Serializable
data object InstallTimeHome : NavKey

/** Initial nav key of on-demand dynamic module. */
@Serializable
data object OnDemandHome : NavKey

@AndroidEntryPoint
class DynamicFeatureActivity : ComponentActivity() {

    @Inject
    lateinit var navigator: DynamicFeatureNavigator

    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            val splitInstallManager = rememberSplitInstallManager()
            val dynamicFeatureDecorator = rememberDynamicFeatureNavEntryDecorator<NavKey>(
                splitInstallManager = splitInstallManager,
                defaultLoadingContent = { LoadingIndicator() }
            )

            NavDisplay(
                backStack = navigator.backStack,
                onBack = { navigator.goBack() },
                modifier = Modifier.fillMaxSize(),
                entryDecorators = listOf(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    dynamicFeatureDecorator,
                ),
                entryProvider = dynamicFeatureDecorator.entryProvider {
                    entry<Home> {
                        HomeScreen(
                            onNavigateToInstallTime = {
                                navigator.goTo(InstallTimeHome)
                            },
                            onNavigateToOnDemand = {
                                navigator.goTo(OnDemandHome)
                            },
                        )
                    }

                    dynamicFeatureModule(
                        initialKey = InstallTimeHome,
                        moduleName = "installtime",
                        entryProviderClassName = "com.example.dynamicfeature.installtime.InstallTimeModuleEntryProvider"
                    )

                    dynamicFeatureModule(
                        initialKey = OnDemandHome,
                        moduleName = "ondemand",
                        entryProviderClassName = "com.example.dynamicfeature.ondemand.OnDemandModuleEntryProvider",
                        loadingContent = { state ->
                            val confirmationLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartIntentSenderForResult()
                            ) { result ->
                                if (result.resultCode == RESULT_CANCELED) {
                                    navigator.backStack.remove(OnDemandHome)
                                }
                            }
                            when (state.status()) {
                                SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                                    LaunchedEffect(state.sessionId()) {
                                        splitInstallManager.startConfirmationDialogForResult(
                                            state,
                                            confirmationLauncher,
                                        )
                                    }
                                }
                                SplitInstallSessionStatus.CANCELED -> {
                                    LaunchedEffect(Unit) {
                                        navigator.backStack.remove(OnDemandHome)
                                    }
                                }
                                SplitInstallSessionStatus.FAILED -> {
                                    InstallationFailedDialog(
                                        errorCode = state.errorCode(),
                                        onDismiss = { navigator.backStack.remove(OnDemandHome) },
                                    )
                                }
                                else -> {
                                    InstallingModuleDialog(
                                        state = state,
                                        onCancel = { navigator.backStack.remove(OnDemandHome) },
                                    )
                                }
                            }
                        }
                    )
                }
            )
        }
    }
}

@Composable
fun HomeScreen(
    onNavigateToInstallTime: () -> Unit,
    onNavigateToOnDemand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ContentGreen(
        title = "Home screen",
        modifier = modifier,
    ) {
        Column {
            Button(onClick = dropUnlessResumed { onNavigateToInstallTime() }) {
                Text(text = "Go to Install Time Module Screen")
            }
            Button(onClick = dropUnlessResumed { onNavigateToOnDemand() }) {
                Text(text = "Go to On Demand Module Screen")
            }
        }
    }
}

@Composable
private fun InstallingModuleDialog(
    state: SplitInstallSessionState,
    onCancel: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Installing module...") },
        text = {
            val totalBytes = state.totalBytesToDownload()
            if (state.status() == SplitInstallSessionStatus.DOWNLOADING && totalBytes > 0) {
                LinearProgressIndicator(
                    progress = { state.bytesDownloaded().toFloat() / totalBytes },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun InstallationFailedDialog(
    errorCode: Int,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Installation failed") },
        text = { Text("Failed to install module (error code: $errorCode).") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
    )
}
