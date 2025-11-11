/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.nav3recipes.scenes.xr

import androidx.navigation3.runtime.NavEntry
import com.example.nav3recipes.scenes.listdetail.ListDetailScene

object XrNavigationKeys{
    internal const val FIRST_PANE_KEY = "XrScene-FirstPane"
    internal const val SECOND_PANE_KEY = "XrScene-SecondPane"

    /**
     * Helper function to add metadata to a [NavEntry] indicating it can be displayed
     * in the list pane of a [ListDetailScene].
     */
    fun firstPane() = mapOf(FIRST_PANE_KEY to true)

    /**
     * Helper function to add metadata to a [NavEntry] indicating it can be displayed
     * in the detail pane of a the [ListDetailScene].
     */
    fun secondPane() = mapOf(SECOND_PANE_KEY to true)
}