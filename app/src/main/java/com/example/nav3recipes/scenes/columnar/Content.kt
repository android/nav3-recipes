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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.dropUnlessResumed

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DirectoryScreen(
    route: DirectoryRoute,
    lastActiveId: String?,
    onNavigateToSubDir: (DirectoryRoute) -> Unit,
    onNavigateToItem: (ItemRoute) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        TopAppBar(
            title = { Text(route.name) },
            subtitle = { Text(route.id) }
        )

        val slots = rememberSaveable(route.id) {
            val numFolders = (3..5).random()
            val numItems = (5..10).random()
            (List(numFolders) { true } + List(numItems) { false }).shuffled().toBooleanArray()
        }

        val combinedItems = remember(slots) {
            var dirCount = 0
            var itemCount = 0
            slots.map { isFolder ->
                if (isFolder) {
                    val i = dirCount++
                    DirectoryRoute("${route.id}/dir_$i", "Folder $i")
                } else {
                    val i = itemCount++
                    ItemRoute("${route.id}/item_$i", "Item $i")
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
                .asPaddingValues()
        ) {
            items(combinedItems, key = { "route_${it.id}" }) { item ->
                val isSelected = lastActiveId != null
                        && (lastActiveId == item.id || lastActiveId.startsWith("${item.id}/"))

                ListItem(
                    headlineContent = { Text(item.name) },
                    leadingContent = { Text(if (item is DirectoryRoute) "📁" else "📄") },
                    colors = ListItemDefaults.colors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = isSelected,
                            onClick = dropUnlessResumed {
                                when (item) {
                                    is DirectoryRoute -> onNavigateToSubDir(item)
                                    is ItemRoute -> onNavigateToItem(item)
                                }
                            }
                        )
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ItemDetailScreen(
    route: ItemRoute,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
    ) {
        TopAppBar(
            title = { Text(route.name) },
            subtitle = { Text(route.id) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )
        Box(modifier = Modifier.padding(16.dp)) {
            Text(route.content)
        }
    }
}
