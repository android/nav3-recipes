# Shared ViewModel Recipe

This recipe demonstrates how to share a `ViewModel` between different screens (entries) in Navigation 3 using a custom `NavEntryDecorator`.

## How it works

This example defines three screens:
- `ParentScreen`: Displays a button that increments a counter, the counter state is held in a `CounterViewModel`.
- `ChildScreen`: A sub-screen that can update the `ParentScreen`'s counter state.
- `StandaloneScreen`: An independent screen with its own isolated state.

### `SharedViewModelStoreNavEntryDecorator`

The core of this recipe is the `SharedViewModelStoreNavEntryDecorator`. This decorator manages `ViewModelStore`s for navigation entries. It allows an entry to specify a "parent" entry whose `ViewModelStore` it should share.

In `SharedViewModelActivity.kt`, the `NavDisplay` is configured with this decorator:

```kotlin
entryDecorators = listOf(
    rememberSaveableStateHolderNavEntryDecorator(),
    rememberSharedViewModelStoreNavEntryDecorator(),
)
```

### Sharing the ViewModel

To enable sharing, the `ChildScreen` entry explicitly defines its parent using metadata:

```kotlin
entry<ChildScreen>(
    metadata = metadata {
        viewModelStoreKey(ParentScreen.toContentKey())
    },
) {
    // ...
}
```
The `toContentKey()` extension function is used to standardize how the parent `NavEntry`'s `contentKey` is specified, both when defining the parent and when referenced in metadata by the child.

When `ChildScreen` requests a `CounterViewModel`:
```kotlin
val parentViewModel = viewModel(modelClass = CounterViewModel::class)
```
The decorator ensures it receives the **same instance** that `ParentScreen` is using, because it's using the `ParentScreen`'s `ViewModelStore`.

In contrast, `StandaloneScreen` does not define a parent, so it gets its own fresh `ViewModelStore` and a new instance of `CounterViewModel`.
