/*
 * Copyright 2026 The Android Open Source Project
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

package com.example.nav3recipes.scenes.columnar

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 */
class Navigator<T : NavKey>(private val backStack: NavBackStack<T>) {

    fun navigate(from: T, to: T) {
        val index = backStack.indexOfFirst { it == from }
        if (index != -1) {
            while (backStack.size > index + 1) {
                backStack.removeLastOrNull()
            }
        }
        backStack.add(to)
    }

    fun goBack() {
        backStack.removeLastOrNull()
    }
}
