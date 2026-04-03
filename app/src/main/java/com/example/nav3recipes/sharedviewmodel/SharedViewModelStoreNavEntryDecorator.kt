/*
 * Copyright 2025 The Android Open Source Project
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

package com.example.nav3recipes.sharedviewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavMetadataKey
import androidx.navigation3.runtime.get
import androidx.navigation3.runtime.metadata
import androidx.savedstate.compose.LocalSavedStateRegistryOwner
import androidx.lifecycle.viewmodel.ViewModelStoreProvider
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreProvider
import androidx.navigation3.runtime.SaveableStateHolderNavEntryDecorator
import kotlin.collections.mutableListOf

/**
 * Returns a [SharedViewModelStoreNavEntryDecorator] that is remembered across recompositions.
 *
 * @param [viewModelStoreOwner] The [ViewModelStoreOwner] that provides the [ViewModelStore] to
 *   NavEntries
 */
@Composable
fun <T : Any> rememberSharedViewModelStoreNavEntryDecorator(
    viewModelStoreOwner: ViewModelStoreOwner =
        checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        },
): SharedViewModelStoreNavEntryDecorator<T> {
    val viewModelStoreProvider = rememberViewModelStoreProvider(viewModelStoreOwner)
    return remember(viewModelStoreOwner) {
        SharedViewModelStoreNavEntryDecorator(
            viewModelStoreProvider,
        )
    }
}

/**
 * Provides the content of a [NavEntry] with a new [ViewModelStoreOwner] and provides that
 * [ViewModelStoreOwner] as a [LocalViewModelStoreOwner] so that it is available within the content.
 *
 * If the [NavEntry] specifies that it has a parent in its metadata, the parent's
 * [ViewModelStoreOwner] will also be supplied along with the new one. This allows the
 * entry to access both its own [ViewModel] and its parent's [ViewModel]s.
 *
 * This requires the usage of [SaveableStateHolderNavEntryDecorator] to ensure that the [NavEntry]
 * scoped [ViewModel]s can properly provide access to [androidx.lifecycle.SavedStateHandle]s.
 *
 * @see [SharedViewModelStoreNavEntryDecorator.parent]
 *
 * @param [viewModelStoreProvider] The [ViewModelStoreProvider] scoped to
 * the parent [ViewModelStoreOwner]
 */
class SharedViewModelStoreNavEntryDecorator<T : Any>(
    viewModelStoreProvider: ViewModelStoreProvider
) : NavEntryDecorator<T>(
    onPop = { key -> viewModelStoreProvider.clearKey(key) },
    decorate = { entry ->
        val localContentKey = entry.contentKey
        val localOwner =
            rememberViewModelStoreOwner(
                viewModelStoreProvider,
                localContentKey,
                savedStateRegistryOwner = LocalSavedStateRegistryOwner.current,
            )

        val localValues: MutableList<ProvidedValue<*>> = mutableListOf(LocalViewModelStoreOwner provides localOwner)

        // If the entry indicates it has a parent, also provide its parent's ViewModelStore
        val parentContentKey = entry.metadata[ParentKey]
        if (parentContentKey != null) {
            val parentOwner = rememberViewModelStoreOwner(
                    viewModelStoreProvider,
                    parentContentKey,
                    savedStateRegistryOwner = LocalSavedStateRegistryOwner.current,
                )

            localValues.add(LocalSharedViewModelStoreOwner provides parentOwner)
        }
        CompositionLocalProvider(
            values = localValues.toTypedArray()
        ) { entry.Content() }
    },
) {
    companion object {
        /**
         * Use this function to specify a `NavEntry`'s parent. The parent's
         * `ViewModelStoreOwner` will be supplied via `LocalSharedViewModelStoreOwner`
         */
        fun parent(key: Any) = metadata {
            put(ParentKey, key)
        }

        object ParentKey : NavMetadataKey<Any>
    }
}

val LocalSharedViewModelStoreOwner =
    staticCompositionLocalOf<ViewModelStoreOwner> { error("No LocalSharedViewModelStoreOwner provided!") }
