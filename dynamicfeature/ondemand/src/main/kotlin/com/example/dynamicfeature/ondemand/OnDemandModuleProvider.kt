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

package com.example.dynamicfeature.ondemand

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.dynamicfeatures.DynamicFeatureModuleEntryProvider
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.content.ContentPink
import com.example.nav3recipes.content.ContentPurple
import com.example.nav3recipes.dynamicfeature.DynamicFeatureEntryPoint
import com.example.nav3recipes.dynamicfeature.DynamicFeatureNavigator
import com.example.nav3recipes.dynamicfeature.OnDemandHome
import dagger.hilt.android.EntryPointAccessors
import kotlinx.serialization.Serializable

@Serializable
private sealed interface OnDemandSubKey : NavKey

@Serializable
private data class OnDemandDetail(val id: String) : OnDemandSubKey

@Serializable
private data object OnDemandSettings : OnDemandSubKey

class OnDemandModuleEntryProvider : DynamicFeatureModuleEntryProvider<NavKey> {
    override fun EntryProviderScope<NavKey>.buildEntries() {
        entry<OnDemandHome> {
            val navigator = rememberNavigator()
            OnDemandModuleScreen(
                onNavigateToDetail = { id -> navigator.goTo(OnDemandDetail(id)) },
                onNavigateToSettings = { navigator.goTo(OnDemandSettings) },
            )
        }
        entry<OnDemandDetail> { key ->
            val navigator = rememberNavigator()
            OnDemandDetailScreen(
                id = key.id,
                onNavigateToSettings = { navigator.goTo(OnDemandSettings) },
            )
        }
        entry<OnDemandSettings> {
            val navigator = rememberNavigator()
            OnDemandSettingsScreen(
                onBackToModuleHome = {
                    navigator.backStack.removeAll { it is OnDemandSubKey }
                },
            )
        }
    }
}

@Composable
private fun rememberNavigator(): DynamicFeatureNavigator {
    val activity = checkNotNull(LocalActivity.current)
    return remember(activity) {
        EntryPointAccessors.fromActivity<DynamicFeatureEntryPoint>(activity).navigator()
    }
}

@Composable
private fun OnDemandModuleScreen(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    ContentBlue(title = "On Demand Module screen") {
        Column {
            Button(onClick = dropUnlessResumed { onNavigateToDetail("A") }) {
                Text("Go to On Demand Detail A")
            }
            Button(onClick = dropUnlessResumed { onNavigateToDetail("B") }) {
                Text("Go to On Demand Detail B")
            }
            Button(onClick = dropUnlessResumed { onNavigateToSettings() }) {
                Text("Go to On Demand Settings")
            }
        }
    }
}

@Composable
private fun OnDemandDetailScreen(
    id: String,
    onNavigateToSettings: () -> Unit,
) {
    ContentPurple(title = "On Demand Detail $id") {
        Button(onClick = dropUnlessResumed { onNavigateToSettings() }) {
            Text("Go to On Demand Settings")
        }
    }
}

@Composable
private fun OnDemandSettingsScreen(
    onBackToModuleHome: () -> Unit,
) {
    ContentPink(title = "On Demand Settings") {
        Button(onClick = dropUnlessResumed { onBackToModuleHome() }) {
            Text("Back to On Demand Module Home")
        }
    }
}
