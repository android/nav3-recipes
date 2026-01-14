# Multiple back stacks recipe #

This recipe demonstrates how to create multiple back stacks. 

The app has three top level routes: `RouteA`, `RouteB` and `RouteC`. These routes have sub routes `RouteA1`, `RouteB1` and `RouteC1` respectively. The content for the sub routes is a counter that can be used to verify state retention through configuration changes and process death.

The app's navigation state is held in the `NavigationState` class. The state itself is created using `rememberNavigationState`. 

Navigation events are handled by the `Navigator`. It updates the navigation state.

The navigation state is converted into `NavEntry`s with `NavigationState.toDecoratedEntries`. These entries are then displayed by `NavDisplay`. 

Key behaviors: 

- This app follows the "exit through home" pattern where the user always exits through the starting back stack. This means that `RouteA`'s entries are _always_ in the list of entries. 
- Navigating to a top level route that is not the starting route _replaces_ the other entries. For example, navigating A->B->C would result in entries for A+C, B's entries are removed. 

Important implementation details: 

- Each top level route has its own `SaveableStateHolderNavEntryDecorator`. This is the object responsible for managing the state for the entries in its back stack. 
