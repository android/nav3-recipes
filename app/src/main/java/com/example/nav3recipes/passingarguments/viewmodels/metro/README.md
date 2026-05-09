# Passing Arguments to ViewModels (Metro)

This recipe demonstrates how to pass navigation arguments (keys) to a `ViewModel` using [Metro](https://zacsweers.github.io/metro/) for dependency injection.

## How it works

This example uses Metro's assisted injection feature:

1.  The `ViewModel`'s constructor uses `@AssistedInject` to receive the navigation key (which is annotated with `@Assisted`).
2.  An `@AssistedFactory` interface is defined to create the `ViewModel`. This interface extends `ManualViewModelAssistedFactory` and is annotated with `@ManualViewModelAssistedFactoryKey` and `@ContributesIntoMap` to be contributed to the `MetroViewModelFactory`.
3.  The `assistedMetroViewModel` composable function is used to obtain the `ViewModel` instance. A lambda is provided to pass the navigation key to the factory's `create` method, making it available to the `ViewModel`.

**Note**: The `rememberViewModelStoreNavEntryDecorator` is added to the `NavDisplay`'s `entryDecorators`. This ensures that `ViewModel`s are correctly scoped to their corresponding `NavEntry`, so that a new `ViewModel` instance is created for each unique navigation key.
