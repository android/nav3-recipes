# Nav UI with Scene Decorators Recipe

This recipe demonstrates how to add UI elements such as top app bars and navigation bars or rails that you’d like to add at the scene, rather than nav entry level. To do this, it uses the scene decorator API.

## How it works

### The `NavigationScene` class

The `NavigationScene` class is the core of this recipe. It takes in a `Scene`, the current window size class, a `SharedTransitionScope`, and composables for a nav bar and nav rail. If the window width size class is medium or greater, it renders the nav rail on the start edge of the screen with the content on the end edge. Otherwise, it renders the nav bar on the bottom edge of the screen with the content on top.

#### Rendering shared UI elements only once

During transitions between scenes, both scenes are composed and rendered at the same time. For elements that are shared between scenes, such as a nav bar or rail, it may not be desirable for them to be composed in both scenes.

For example, the nav bar and rail composables in this recipe have some internal state that can't be hoisted (such as the state for the animations after an item is selected). As such, it's desirable to call the given composable only from one scene at any given time.

To accomplish the desired behavior, this recipe combines Compose's [movable content](https://developer.android.com/reference/kotlin/androidx/compose/runtime/package-summary#movableContentOf(kotlin.Function0)) and [shared element](https://developer.android.com/develop/ui/compose/animation/shared-elements) APIs:

* By using `movableContentOf`, it is able to retain the state of the composable as it is moved between the different branches of the composition corresponding to each scene.
* By using the shared element APIs, it is able to keep the nav bar/rail in place while animating the content of the scenes that have been decorated. This is accomplished using the `renderInSharedTransitionScopeOverlay` modifier as well as a custom modifier, `cacheSize`, that maintains a placeholder of the correct size in the scene that doesn't call the movable content composable.

### The `NavigationSceneDecoratorStrategy` class

The `NavigationSceneDecoratorStrategy` class is responsible for wrapping the input scene in a `NavigationScene`. The `rememberNavigationSceneDecoratorStrategy` function simplifies the process of creating a `NavigationSceneDecoratorStrategy` by handling the creation of the `movableContentOf` composables. Generally, the `NavigationSceneDecoratorStrategy` should be one of, if not the final, items in the `sceneDecoratorStrategies` parameter so that it can contain all the other content of the app.
