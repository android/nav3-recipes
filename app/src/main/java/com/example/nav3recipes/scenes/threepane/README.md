# Three-Pane Scene Recipe

This example shows how to create a three pane layout using the Scenes API.

A `ThreePaneSceneStrategy` will return a `ThreePaneScene` if:

-   the window width is over 1200dp
-   the last three nav entries on the back stack have indicated that they support being displayed in a `ThreePaneScene` in their metadata.

It will instead return a `TwoPaneScene` if

-   the window width is over 600dp but smaller than 1200dp

See `ThreePaneScene.kt` for more implementation details.
