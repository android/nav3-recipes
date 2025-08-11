package com.example.nav3recipes.modular.hilt

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.navigation3.runtime.NavKey
import androidx.savedstate.SavedState
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.serialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.serializer

/**
 * A saveable implementation of TopLevelBackStack that maintains separate navigation stacks
 * for different top-level keys (tabs/sections) while ensuring proper serialization across
 * process death and configuration changes.
 *
 * This implementation follows Navigation 3's serialization pattern using SavedState
 * and polymorphic serializers to ensure all NavKey types are properly serialized.
 *
 * @param T The type of top-level keys, must implement NavKey for serialization
 */
class TopLevelBackStack<T : NavKey>(
    startKey: T
) {
    companion object {
        private const val TAG = "TopLevelBackStack"
    }

    // Serializable state for saving/restoring the entire TopLevelBackStack
    @Serializable
    internal data class TopLevelBackStackState(
        val currentTopLevelKey: String, // Store as string for type safety across serialization
        val stackKeys: List<String>,   // Top-level keys in order
        val stackContents: Map<String, List<@Contextual SavedState>> // Each stack's content as SavedState
    )

    // Maintain a stack for each top level route - each stack can contain any NavKey
    private var topLevelStacks: LinkedHashMap<T, SnapshotStateList<NavKey>> = linkedMapOf()

    // Current top level key
    var topLevelKey by mutableStateOf(startKey)
        private set

    // Flattened back stack for NavDisplay - contains all NavKey types
    val backStack: SnapshotStateList<NavKey>

    init {
        // Initialize with the start key
        val initialStack: SnapshotStateList<NavKey> =
            mutableListOf<NavKey>(startKey).toMutableStateList()
        topLevelStacks[startKey] = initialStack
        backStack = initialStack
    }

    private fun updateBackStack(): SnapshotStateList<NavKey> {
        Log.d(TAG, "updateBackStack: Updating flattened back stack")
        return backStack.apply {
            clear()
            addAll(topLevelStacks.values.flatten())
            Log.d(TAG, "updateBackStack: New back stack size: ${this.size}")
        }
    }

    /**
     * Adds or switches to a top-level key, creating a new stack if needed
     */
    fun addTopLevel(key: T) {
        Log.d(TAG, "addTopLevel: Adding/switching to top level route: $key")

        // If the top level doesn't exist, create it
        if (topLevelStacks[key] == null) {
            Log.d(TAG, "addTopLevel: Creating new stack for $key")
            val newStack: SnapshotStateList<NavKey> =
                mutableListOf<NavKey>(key).toMutableStateList()
            topLevelStacks[key] = newStack
        } else {
            Log.d(TAG, "addTopLevel: Moving existing stack for $key to end")
            // Move existing stack to end (most recently used)
            val existingStack = topLevelStacks.remove(key)
            if (existingStack != null) {
                topLevelStacks[key] = existingStack
            }
        }

        topLevelKey = key
        Log.d(TAG, "addTopLevel: Current top level key: $topLevelKey")
        updateBackStack()
    }

    /**
     * Adds an entry to the current top-level stack
     */
    fun add(key: NavKey) {
        Log.d(TAG, "add: Adding $key to current top level stack ($topLevelKey)")
        topLevelStacks[topLevelKey]?.add(key)
        updateBackStack()
    }

    /**
     * Removes the last entry from the current stack, or removes entire top-level if it becomes empty
     */
    fun removeLast() {
        Log.d(TAG, "removeLast: Removing last item from current stack ($topLevelKey)")

        val currentStack = topLevelStacks[topLevelKey]
        if (currentStack != null && currentStack.size > 1) {
            // Remove last entry from current stack
            val removedKey = currentStack.removeLastOrNull()
            Log.d(TAG, "removeLast: Removed entry: $removedKey")
        } else {
            // Current stack only has one item (the top-level key itself), remove entire stack
            val removedStack = topLevelStacks.remove(topLevelKey)
            Log.d(TAG, "removeLast: Removed entire stack for: $topLevelKey")

            // Switch to the last remaining top-level key
            if (topLevelStacks.isNotEmpty()) {
                topLevelKey = topLevelStacks.keys.last()
                Log.d(TAG, "removeLast: New top level key: $topLevelKey")
            }
        }

        updateBackStack()
    }

    /**
     * Replaces the current top-level stack with a new one
     */
    fun replaceCurrentStack(entries: List<NavKey>) {
        Log.d(
            TAG,
            "replaceCurrentStack: Replacing stack for $topLevelKey with ${entries.size} entries"
        )
        topLevelStacks[topLevelKey] = entries.toMutableStateList()
        updateBackStack()
    }

    /**
     * Clears all stacks and sets a single entry
     */
    fun clearAndSet(key: T) {
        Log.d(TAG, "clearAndSet: Clearing all stacks and setting $key")
        topLevelStacks.clear()
        val newStack: SnapshotStateList<NavKey> = mutableListOf<NavKey>(key).toMutableStateList()
        topLevelStacks[key] = newStack
        topLevelKey = key
        updateBackStack()
    }

    /**
     * Gets the current stack for the active top-level key
     */
    fun getCurrentStack(): List<NavKey> {
        return topLevelStacks[topLevelKey]?.toList() ?: emptyList()
    }

    /**
     * Gets all top-level keys
     */
    fun getTopLevelKeys(): List<T> {
        return topLevelStacks.keys.toList()
    }

    /**
     * Serializes the entire TopLevelBackStack state
     */
    internal fun saveState(): TopLevelBackStackState {
        val serializer = UnsafePolymorphicSerializer<NavKey>()

        return TopLevelBackStackState(
            currentTopLevelKey = topLevelKey::class.java.name,
            stackKeys = topLevelStacks.keys.map { it::class.java.name },
            stackContents = topLevelStacks.mapKeys { it.key::class.java.name }
                .mapValues { (_, stack) ->
                    stack.map { navKey -> encodeToSavedState(serializer, navKey) }
                }
        )
    }

    /**
     * Restores the TopLevelBackStack state from serialized data
     */
    @Suppress("UNCHECKED_CAST")
    internal fun restoreState(state: TopLevelBackStackState) {
        val serializer = UnsafePolymorphicSerializer<NavKey>()

        topLevelStacks.clear()

        // Restore each stack
        state.stackContents.forEach { (keyClassName, savedStates) ->
            try {
                val keyClass = Class.forName(keyClassName).kotlin
                val restoredStack = savedStates.map { savedState ->
                    decodeFromSavedState(serializer, savedState)
                }.toMutableStateList()

                // Find the matching top-level key
                val topLevelKeyInstance = restoredStack.firstOrNull() as? T
                if (topLevelKeyInstance != null) {
                    topLevelStacks[topLevelKeyInstance] = restoredStack
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restore stack for key: $keyClassName", e)
            }
        }

        // Restore current top-level key
        try {
            val currentKeyClass = Class.forName(state.currentTopLevelKey).kotlin
            topLevelKey = topLevelStacks.keys.firstOrNull {
                it::class == currentKeyClass
            } ?: topLevelStacks.keys.firstOrNull() ?: return
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore current top-level key", e)
            topLevelKey = topLevelStacks.keys.firstOrNull() ?: return
        }

        updateBackStack()
    }
}

/**
 * Remembers a TopLevelBackStack that survives configuration changes and process death.
 * Uses the same serialization approach as Navigation 3's rememberNavBackStack.
 *
 * @param startKey The initial top-level key
 * @return A TopLevelBackStack that maintains state across recreation
 */
@Composable
fun <T : NavKey> rememberTopLevelBackStack(startKey: T): TopLevelBackStack<T> {
    return rememberSaveable(
        saver = topLevelBackStackSaver()
    ) {
        TopLevelBackStack(startKey)
    }
}

/**
 * Saver for TopLevelBackStack that handles proper serialization of all NavKey types
 * following Navigation 3's pattern with SavedState and polymorphic serializers
 */
@Suppress("UNCHECKED_CAST")
private fun <T : NavKey> topLevelBackStackSaver(): Saver<TopLevelBackStack<T>, Any> {
    return listSaver<TopLevelBackStack<T>, SavedState>(
        save = { topLevelBackStack ->
            try {
                val state = topLevelBackStack.saveState()
                listOf(
                    encodeToSavedState(
                        TopLevelBackStack.TopLevelBackStackState.serializer(),
                        state
                    )
                )
            } catch (e: Exception) {
                Log.e("TopLevelBackStack", "Failed to save state", e)
                emptyList()
            }
        },
        restore = { savedStates ->
            if (savedStates.isEmpty()) return@listSaver null

            try {
                val state = decodeFromSavedState(
                    TopLevelBackStack.TopLevelBackStackState.serializer(),
                    savedStates.first()
                )
                val topLevelBackStack = TopLevelBackStack(
                    state.stackContents.values.first().first().let {
                        decodeFromSavedState(
                            UnsafePolymorphicSerializer<NavKey>(),
                            it
                        )
                    } as T)
                topLevelBackStack.restoreState(state)
                topLevelBackStack
            } catch (e: Exception) {
                Log.e("TopLevelBackStack", "Failed to restore state", e)
                null
            }
        }
    )
}

/**
 * Unsafe polymorphic serializer for NavKey types - copied from Navigation 3 runtime
 */
@OptIn(InternalSerializationApi::class)
internal class UnsafePolymorphicSerializer<T : Any> : KSerializer<T> {

    override val descriptor =
        buildClassSerialDescriptor("PolymorphicData") {
            element(elementName = "type", serialDescriptor<String>())
            element(elementName = "payload", buildClassSerialDescriptor("Any"))
        }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(decoder: Decoder): T {
        return decoder.decodeStructure(descriptor) {
            val className = decodeStringElement(descriptor, decodeElementIndex(descriptor))
            val classRef = Class.forName(className).kotlin
            val serializer = classRef.serializer()

            decodeSerializableElement(descriptor, decodeElementIndex(descriptor), serializer) as T
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeStructure(descriptor) {
            val className = value::class.java.name
            encodeStringElement(descriptor, index = 0, className)
            val serializer = value::class.serializer() as KSerializer<T>
            encodeSerializableElement(descriptor, index = 1, serializer, value)
        }
    }
}