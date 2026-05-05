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

import androidx.compose.runtime.Composable
import com.example.nav3recipes.content.ContentBlue
import com.example.nav3recipes.dynamicfeature.DynamicFeatureContentProvider
import com.example.nav3recipes.dynamicfeature.OnDemandModule

@Suppress("unused")
class DynamicFeatureOnDemandContentProvider : DynamicFeatureContentProvider<OnDemandModule> {
    @Composable
    override fun Content(key: OnDemandModule) {
        OnDemandModuleScreen()
    }
}

@Composable
private fun OnDemandModuleScreen() {
    ContentBlue(
        title = "On Demand Module screen"
    )
}
