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


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicFeatureDownloadProgressDialog(
    dynamicFeatureManager: DynamicFeatureManager,
    modifier: Modifier = Modifier,
) {
    val status = dynamicFeatureManager.status

    if (status is InstallStatus.Idle) return

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) {
        // The session status will be updated automatically via the
        // SplitInstallStateUpdatedListener in DynamicFeatureManager,
        // so no explicit handling is required here.
    }

    val progress = if (status is InstallStatus.Downloading) status.progress else 0f
    val progressPercentage = "${(progress * 100).roundToInt()}%"

    AlertDialog(
        onDismissRequest = {},
        modifier = modifier,
        title = {
            Text(text = getStatusTitle(status))
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (status is InstallStatus.Downloading) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.semantics {
                            contentDescription = "Downloading: $progressPercentage"
                        }
                    )
                    Text(
                        text = progressPercentage,
                        style = MaterialTheme.typography.labelSmall,
                    )
                } else if (status !is InstallStatus.Failed && status !is InstallStatus.RequiresUserConfirmation) {
                    CircularProgressIndicator()
                }

                if (status is InstallStatus.Failed) {
                    Text(
                        text = "Error code: ${status.errorCode}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            if (status is InstallStatus.RequiresUserConfirmation) {
                Button(onClick = {
                    dynamicFeatureManager.startConfirmationDialogForResult(
                        status.state,
                        launcher
                    )
                }) {
                    Text(text = "Confirm Download")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = dynamicFeatureManager::cancelInstallModule) {
                Text(text = "Cancel")
            }
        }
    )
}

private fun getStatusTitle(status: InstallStatus): String {
    return when (status) {
        is InstallStatus.Pending -> "Requesting..."
        is InstallStatus.Downloading -> "Downloading..."
        is InstallStatus.Installing -> "Installing..."
        is InstallStatus.RequiresUserConfirmation -> "Requires Confirmation"
        is InstallStatus.Failed -> "Installation Failed"
        else -> ""
    }
}
