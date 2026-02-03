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

package com.example.nav3recipes.migration.content

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.dropUnlessResumed
import com.example.nav3recipes.content.ContentGreen
import com.example.nav3recipes.content.ContentMauve
import com.example.nav3recipes.content.ContentPink
import com.example.nav3recipes.content.ContentPurple
import com.example.nav3recipes.content.ContentRed


@Composable
fun ScreenA(onSubRouteClick: () -> Unit, onDialogClick: () -> Unit) {
    ContentRed("Route A title") {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = dropUnlessResumed(block = onSubRouteClick)) {
                Text("Go to A1")
            }
            Button(onClick = dropUnlessResumed(block = onDialogClick)) {
                Text("Open dialog D")
            }
        }
    }
}

@Composable
fun ScreenA1() {
    ContentPink("Route A1 title")
}

@Composable
fun ScreenB(
    onDetailClick: (String) -> Unit,
    onDialogClick: () -> Unit
) {
    ContentGreen("Route B title") {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = dropUnlessResumed { onDetailClick("ABC") }) {
                Text("Go to B1")
            }
            Button(onClick = dropUnlessResumed(block = onDialogClick)) {
                Text("Open dialog D")
            }
        }
    }
}

@Composable
fun ScreenB1(id: String) {
    ContentPurple("Route B1 title. ID: $id")
}

@Composable
fun ScreenC(onDialogClick: () -> Unit) {
    ContentMauve("Route C title") {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = dropUnlessResumed(block = onDialogClick)) {
                Text("Open dialog D")
            }
        }
    }
}
