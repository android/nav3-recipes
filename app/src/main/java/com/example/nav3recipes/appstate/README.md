# AppState Recipe

This recipe shows how to store and persist a Navigation 3 back stack using `StateStore` and `DataStore` so that navigation state survives configuration changes, process death, and app restarts.

## How it works

To manage and persist the back stack with `androidx.appstate`:

1. Define a `StateStoreKey` that implements `PersistToDataStore`.
2. Call `stateStore.syncToDataStore(dataStore)` to synchronize keys implementing `PersistToDataStore` with `DataStore`.
3. Read the back stack in Compose using `stateStore.getState(...)` and update it using `stateStore.updateState(...)`.

This recipe is divided into two packages demonstrating the two approaches to `NavKey` serialization:

- `closed` - Uses closed polymorphism by defining routes under a `@Serializable sealed interface AppNavKey : NavKey`. Because all subtypes are known at compile time, `StateStoreKey<NavBackStack<AppNavKey>>` is serialized automatically without custom serializer configuration.
- `open` - Uses open polymorphism where routes implement `NavKey` directly (`@Serializable data object RouteA : NavKey`). Because `NavKey` is an open interface, a `@Serializable` wrapper class (`AppBackStack`) is used to attach `NavKeySerializer` to the list of `NavKey` elements.
