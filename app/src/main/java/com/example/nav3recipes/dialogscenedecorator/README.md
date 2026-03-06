# Dialog Scene Decorator Recipe

This recipe demonstrates one approach to displaying a scene within a dialog. For example, this could be used to display a list-detail settings UI as an overlay when the app is in an expanded window, but as full screen pages otherwise.

## How it works

To display a scene within a dialog, you need to do the following:

1. **Create a `SceneDecorationStrategy`**: This recipe defines the `DialogSceneDecoratorStrategy` as well as the `DialogDecoratorScene` it uses to present an input `Scene` within a dialog. When defining a dialog scene decoration strategy, consider the following:
   1.**When can a scene be displayed within a dialog?** In this recipe, scenes are only displayed in a dialog if the window width is at least expanded.
   2**What metadata should determine if a scene can be displayed within a dialog?** In this recipe, a scene is displayed in a dialog if the first entry within that scene has the `DialogSceneMetadataKey` metadata. Depending on your use case, another approach such as requiring all entries within the scene to have the same metadata might be more appropriate.
   3**What is the dismissal behavior of the dialog?** In this recipe, clicking outside the dialog removes all the entries in the scene while navigating back only removes one at a time.
2. **Use your `SceneDecorationStrategy`**: To use your scene decorator strategy, pass it to the `NavDisplay` (or `rememberSceneState`) as part of the `sceneDecoratorStrategies` parameter.
   1. **Caution:** Because `NavDisplay` doesn't decorate `OverlayScene` instances, you may need to pay attention to the position of your dialog scene decoration strategy within the `sceneDecoratorStrategies` list.
