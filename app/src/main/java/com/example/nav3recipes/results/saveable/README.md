# Returning a Result (Saveable State-Based)

This recipe demonstrates how to return a result from one screen to a previous screen using a state-based approach that survives configuration changes and process death.

## How it works

This example builds on top of `ResultEventBus` and introduces a custom extension function `conflateAsSaveableState`.

1.  **ResultEventBusNavEntryDecorator**: A `NavEntryDecorator` that provides a `ResultEventBus` via `LocalResultEventBus`.
2.  **`ResultEventBus`**: A `ResultEventBus` is created and made available to the composables via `LocalResultEventBus`. This EventBus sends and receives the results.
3.  **`conflateAsSaveableState`**: A custom extension function on `ResultEventBus` that uses `rememberSaveable` to create a state container, and `ResultEffect` to listen for new results and persist them.
4.  **Sending the result**: The screen that produces the result calls `resultBus.sendResult(person)` to send the data back.
5.  **Observing the result**: The screen that needs the result calls `LocalResultEventBus.current.conflateAsSaveableState<Person?>(null)` to get a `State` object. The UI observes this state and recomposes whenever the result changes.

This approach is suitable when the result needs to survive configuration changes and process death, whereas the standard `conflateAsState` does not.

### Using Complex Data Types

`rememberSaveable` works automatically with primitives, `Parcelable`, and `Serializable` types. If your result is a custom class that isn't one of these, you'll need to use a custom `Saver`.

Because `conflateAsSaveableState` wraps the value in a `MutableState`, you would need to add a `saver` parameter to `conflateAsSaveableState` and use a helper to adapt your `Saver` to work with `MutableState`:

```kotlin
fun <T, Saveable : Any> mutableStateSaver(saver: Saver<T, Saveable>) = Saver<MutableState<T>, Saveable>(
    save = { state -> with(saver) { save(state.value) } },
    restore = { value -> mutableStateOf(saver.restore(value) as T) }
)
```

