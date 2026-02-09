# Common UI Recipe

This recipe demonstrates how to implement a common navigation UI pattern with a bottom navigation bar and multiple back stacks, where each tab in the navigation bar has its own navigation history.

## How it works

This example has three top-level destinations: `Home`, `ChatList`, and `Camera`. The `ChatList` destination also has a sub-route, `ChatDetail`.

### `TopLevelBackStack`

The core of this recipe is the `TopLevelBackStack` class, which is responsible for managing the navigation state. It works as follows:

-   It maintains a separate back stack for each top-level destination (tab).
-   It keeps track of the currently selected top-level destination.
-   It provides a single, flattened back stack that can be used by the `NavDisplay` composable. This flattened back stack is a combination of the individual back stacks of all the tabs.

### UI Structure

The UI is built using a `Scaffold` composable, with a `NavigationBar` as the `bottomBar`.

-   The `NavigationBar` displays an item for each top-level destination. When an item is clicked, it calls `topLevelBackStack.addTopLevel` to switch to the corresponding tab, preserving the navigation history of each tab.
-   The `NavDisplay` composable is placed in the content area of the `Scaffold`. It is responsible for displaying the current screen based on the flattened back stack provided by `TopLevelBackStack`.

This approach allows for a common navigation pattern where users can switch between different sections of the app, and each section maintains its own navigation history.

### State Preservation

It's important to note how the navigation state is managed in this recipe. When a user navigates away from a top-level destination (e.g., by pressing the back button until they return to a previous tab), the entire navigation history for that destination is cleared. The state is not saved. When the user returns to that tab later, they will start from its initial screen.

**Note**: In this example, the `Home` route can move above the `ChatList` and `Camera` routes, meaning navigating back from `Home` doesn't necessarily leave the app. The app will exit when the user goes back from a single remaining top level route in the back stack.
