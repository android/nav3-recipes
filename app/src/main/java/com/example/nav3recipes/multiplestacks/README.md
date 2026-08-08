# Multiple back stacks recipe #

This recipe demonstrates how to create multiple back stacks. 

The app has three top level routes: `RouteA`, `RouteB` and `RouteC`. These routes have sub routes `RouteA1`, `RouteB1` and `RouteC1` respectively. `RouteA1` contains a 100-item scrollable list, while `RouteB1` and `RouteC1` contain counters used to verify state retention through configuration changes and process death.

The app's navigation state is held in the `NavigationState` class. The state itself is created using `rememberNavigationState`. 

Navigation events are handled by the `Navigator`. It updates the navigation state.

The navigation state is converted into `NavEntry`s with `NavigationState.toDecoratedEntries`. These entries are then displayed by `NavDisplay`. 

Key behaviors: 

- This app follows the "exit through home" pattern where the user always exits through the starting back stack. This means that `RouteA`'s entries are _always_ in the list of entries. 
- Navigating to a top level route that is not the starting route _replaces_ the other entries. For example, navigating A->B->C would result in entries for A+C, B's entries are removed. 
- When a top-level route is reselected, for example if the user is on `RouteA` and taps `RouteA` on the navigation bar again, the `NavigationBar` signals the reselected key to `Navigator`, which emits it to a `Flow<NavKey>`. `RouteA1` observes this flow and, when it receives `RouteA`, resets its list scroll position to index 0.

Important implementation details: 

- Each top level route has its own `SaveableStateHolderNavEntryDecorator` and keyed `ViewModelStoreProvider` with a `ViewModelStoreNavEntryDecorator`. These objects manage the state for the entries in each back stack, so saveable state and `ViewModel`s survive top-level stack swaps.
