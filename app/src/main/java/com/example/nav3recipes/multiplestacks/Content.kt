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

package com.example.nav3recipes.multiplestacks

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.dropUnlessResumed
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.content.ContentMauve
import com.example.nav3recipes.content.ContentOrange
import com.example.nav3recipes.content.ContentPink
import com.example.nav3recipes.content.ContentPurple
import com.example.nav3recipes.content.ContentRed

fun EntryProviderScope<NavKey>.featureASection(
    onSubRouteClick: () -> Unit,
) {
    entry<RouteA> {
        ContentRed("Route A") {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = dropUnlessResumed(block = onSubRouteClick)) {
                    Text("Go to A1")
                }
            }
        }
    }
    entry<RouteA1> {
        ContentPink("Route A1") {
            var count by rememberSaveable {
                mutableIntStateOf(0)
            }

            Button(onClick = dropUnlessResumed { count++ }) {
                Text("Value: $count")
            }
        }
    }
}

fun EntryProviderScope<NavKey>.featureBSection(
    onSubRouteClick: () -> Unit,
) {
    entry<RouteB> {
        ContentGreen("Route B") {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = dropUnlessResumed(block = onSubRouteClick)) {
                    Text("Go to B1")
                }
            }
        }
    }
    entry<RouteB1> {
        ContentPurple("Route B1") {
            var count by rememberSaveable {
                mutableIntStateOf(0)
            }
            Button(onClick = dropUnlessResumed { count++ }) {
                Text("Value: $count")
            }
        }
    }
}

fun EntryProviderScope<NavKey>.featureCSection(
    onSubRouteClick: () -> Unit,
) {
    entry<RouteC> {
        ContentMauve("Route C") {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Button(onClick = dropUnlessResumed(block = onSubRouteClick)) {
                    Text("Go to C1")
                }
            }
        }
    }
    entry<RouteC1> {
        ContentOrange("Route C1") {
            var count by rememberSaveable {
                mutableIntStateOf(0)
            }

            Button(onClick = dropUnlessResumed { count++ }) {
                Text("Value: $count")
            }
        }
    }
}