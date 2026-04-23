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
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.ui.setEdgeToEdgeConfig
import kotlinx.serialization.Serializable

class DynamicFeatureActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        setEdgeToEdgeConfig()
        super.onCreate(savedInstanceState)
        setContent {
            val backStack = rememberNavBackStack(RegularModule)
            val dynamicFeatureManager = retainDynamicFeatureManager()

            DynamicFeatureDownloadProgressDialog(dynamicFeatureManager)

            NavDisplay(
                backStack = backStack,
                modifier = Modifier.fillMaxSize(),
                entryProvider = entryProvider {
                    entry<RegularModule> {
                        RegularModuleScreen(
                            onNavigateToInstallTime = {
                                dynamicFeatureManager.installModule(InstallTimeModule.MODULE_NAME) {
                                    backStack.add(InstallTimeModule)
                                }
                            },
                            onNavigateToOnDemand = {
                                dynamicFeatureManager.installModule(OnDemandModule.MODULE_NAME) {
                                    backStack.add(OnDemandModule)
                                }
                            },
                        )
                    }

                    dynamicFeatureEntry<InstallTimeModule>(InstallTimeModule.CLASS_NAME)

                    dynamicFeatureEntry<OnDemandModule>(OnDemandModule.CLASS_NAME)
                }
            )
        }
    }
}

@Composable
fun RegularModuleScreen(
    onNavigateToInstallTime: () -> Unit,
    onNavigateToOnDemand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ContentGreen(
        title = "Regular Module screen",
        modifier = modifier,
    ) {
        Column {
            Button(onClick = onNavigateToInstallTime) {
                Text(text = "Go to Install Time Module Screen")
            }
            Button(onClick = onNavigateToOnDemand) {
                Text(text = "Go to On Demand Module Screen")
            }
        }
    }
}

@Serializable
private data object RegularModule : NavKey

@Serializable
data object InstallTimeModule : NavKey {
    const val CLASS_NAME =
        "com.example.dynamicfeature.installtime.DynamicFeatureInstallTimeContentProvider"
    const val MODULE_NAME = "installtime"
}

@Serializable
data object OnDemandModule : NavKey {
    const val CLASS_NAME =
        "com.example.dynamicfeature.ondemand.DynamicFeatureOnDemandContentProvider"
    const val MODULE_NAME = "ondemand"
}
