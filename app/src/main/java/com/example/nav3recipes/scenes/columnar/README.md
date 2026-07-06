# Columnar Scene Recipe

This recipe demonstrates how to create a custom scene strategy using Jetpack Navigation 3 to display multiple items side-by-side on wide screens.

## Features
- **Adaptive Layout:** Displays a single column on compact screens and multiple columns on medium/expanded screens.
- **Custom Scene:** The `ColumnarScene` handles the layout of multiple `NavEntry`s using a `LazyRow`.
- **Predictive Back:** Integrates with `PredictiveBackHandler` to provide a smooth backward transition when swiping within the scene.
- **Entry Metadata:** Uses `NavMetadataKey`s (`DirectoryKey` and `DetailKey`) to categorize entries and construct the scene.
- **Custom Strategy:** The `ColumnarSceneStrategy` determines when and how to build the `ColumnarScene` based on the window size and the backstack entries.

## How it works

1.  **Define Keys:** `ColumnarScene.Companion.directoryPane()` and `detailPane()` define metadata keys to attach to routes in the `entryProvider`.
2.  **Attach Metadata:** In `ColumnarActivity`, `entry<DirectoryRoute>(metadata = ColumnarScene.directoryPane())` attaches the key to directory entries.
3.  **Strategy Evaluation:** `ColumnarSceneStrategy` inspects the backstack. If the screen is wide enough and the top entries contain the `DirectoryKey`, it gathers them (and an optional `DetailKey` entry) and wraps them in a `ColumnarScene`.
4.  **Scene Rendering:** `ColumnarScene` overrides the `content` property. It iterates through the gathered entries and displays them side-by-side using a `LazyRow`.
5.  **Navigation within Scene:** The `Navigator` replaces entries in the backstack when navigating between directories in the same level, allowing the `ColumnarScene` to update dynamically.

## Key Files
- `ColumnarScene.kt`: The core implementation of the custom scene and its strategy.
- `ColumnarActivity.kt`: The main entry point that sets up the `NavDisplay` and the scene strategy.
- `Content.kt`: The UI composables for the directory and item detail screens.
- `Navigator.kt`: A helper class to manage backstack manipulations for this specific navigation pattern.
