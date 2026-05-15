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
import androidx.lifecycle.compose.dropUnlessResumed
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
            val backStack = rememberNavBackStack(Home)
            val dynamicFeatureManager = retainDynamicFeatureManager()

            DynamicFeatureDownloadProgressDialog(dynamicFeatureManager)

            NavDisplay(
                backStack = backStack,
                onBack = { backStack.removeLastOrNull() },
                modifier = Modifier.fillMaxSize(),
                entryProvider = entryProvider {
                    appEntry<Home> {
                        HomeScreen(
                            onNavigateToInstallTime = {
                                dynamicFeatureManager.installModule(
                                    moduleName = InstallTimeModule.moduleName,
                                    onModuleInstalled = {
                                        backStack.add(InstallTimeModule.Home)
                                    }
                                )
                            },
                            onNavigateToOnDemand = {
                                dynamicFeatureManager.installModule(
                                    moduleName = OnDemandModule.moduleName,
                                    onModuleInstalled = {
                                        backStack.add(OnDemandModule.Home)
                                    }
                                )
                            },
                        )
                    }

                    dynamicFeatureManager.installedModules
                        .mapNotNull { ALL_DYNAMIC_MODULES_MAP[it] }
                        .forEach { buildDynamicEntries(it) }
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

@Serializable
private data object Home : AppNavKey

private val ALL_DYNAMIC_MODULES_MAP = listOf(
    InstallTimeModule,
    OnDemandModule
).associateBy { it.moduleName }

object InstallTimeModule : DynamicModule(
    entryBuilderClassName = "com.example.dynamicfeature.installtime.InstallTimeEntryBuilder",
    moduleName = "installtime",
) {
    @Serializable
    data object Home : AppNavKey {
        override fun toContentKey(): Any {
            return "InstallTimeHome"
        }
    }
}

object OnDemandModule : DynamicModule(
    entryBuilderClassName = "com.example.dynamicfeature.ondemand.OnDemandEntryBuilder",
    moduleName = "ondemand"
) {
    @Serializable
    data object Home : AppNavKey {
        override fun toContentKey(): Any {
            return "OnDemandHome"
        }
    }
}