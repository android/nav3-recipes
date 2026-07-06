# Returning a Result (Serializable State-Based)

This recipe demonstrates how to return a result from one screen to a previous screen using a state-based approach that survives configuration changes and process death by leveraging Kotlin Serialization and `rememberSerializable`.

## How it works

This example builds on top of `ResultEventBus` and introduces a custom extension function `conflateAsSerializableState`.

1.  **ResultEventBusNavEntryDecorator**: A `NavEntryDecorator` that provides a `ResultEventBus` via `LocalResultEventBus`.
2.  **`ResultEventBus`**: A `ResultEventBus` is created and made available to the composables via `LocalResultEventBus`. This EventBus sends and receives the results.
3.  **`conflateAsSerializableState`**: A custom extension function on `ResultEventBus` that uses `rememberSerializable` to create a state container, and `ResultEffect` to listen for new results and persist them.
4.  **Sending the result**: The screen that produces the result calls `resultBus.sendResult(person)` to send the data back.
5.  **Observing the result**: The screen that needs the result calls `LocalResultEventBus.current.conflateAsSerializableState<Person?>(null)` to get a `State` object. The UI observes this state and recomposes whenever the result changes.

This approach is suitable when the result needs to survive configuration changes and process death, whereas the standard `conflateAsState` does not.

### Supporting Nullable Types

The standard `rememberSerializable` function has a generic upper-bound constraint of `T : Any`, which prevents direct preservation of nullable types. 

To support nullable types (such as `Person?`), `conflateAsSerializableState` circumvents this restriction by internally wrapping the value in a generic `@Serializable` class, `NullableWrapper`:

```kotlin
@Serializable
private data class NullableWrapper<T>(val value: T)
```

### Serialization of Custom Types

Because this approach uses `rememberSerializable` from the `androidx.compose.runtime.saveable` package, any custom class used as a result (like `Person`) must be marked with Kotlin Serialization's `@Serializable` annotation:

```kotlin
@Serializable
data class Person(val name: String, val favoriteColor: String)
```

The `conflateAsSerializableState` extension function automatically retrieves the appropriate `KSerializer` via the `serializer<T>()` helper when using the reified version:

```kotlin
@Composable
inline fun <reified T> ResultEventBus.conflateAsSerializableState(
    defaultValue: T,
    vararg inputs: Any?,
    configuration: SavedStateConfiguration = SavedStateConfiguration.DEFAULT,
): State<T>
```
