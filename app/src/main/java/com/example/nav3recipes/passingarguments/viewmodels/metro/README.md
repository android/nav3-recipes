# Passing Arguments to ViewModels (Hilt)

This recipe demonstrates how to pass navigation arguments (keys) to a `ViewModel` using Hilt for dependency injection.

## How it works

This example uses Dagger/Hilt's assisted injection feature:

1.  The `ViewModel` is annotated with `@HiltViewModel` and its constructor uses `@AssistedInject` to receive the navigation key (which is annotated with `@Assisted`).
2.  An `@AssistedFactory` interface is defined to create the `ViewModel`.
3.  The `hiltViewModel` composable function is used to obtain the `ViewModel` instance. A `creationCallback` is provided to pass the navigation key to the factory, making it available to the `ViewModel`.

**Note**: The `rememberViewModelStoreNavEntryDecorator` is added to the `NavDisplay`'s `entryDecorators`. This ensures that `ViewModel`s are correctly scoped to their corresponding `NavEntry`, so that a new `ViewModel` instance is created for each unique navigation key.