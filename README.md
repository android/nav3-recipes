# Navigation 3 - Code recipes
[Jetpack Navigation 3](https://goo.gle/nav3) is a library for app navigation. This repository contains recipes for how to
use its APIs to implement common navigation use cases. Each recipe introduces a single concept. Instead
of making existing recipes more complex, there should be a new recipe for that particular concept.

Every Navigation 3 release will be an opportunity for patterns you see in recipes to "graduate" and become
(optional) helpers in the library itself. Then we'll update the recipe to use that prebuilt helper, thus
ensuring that the recipes continue to be a good way to approach these kinds of problems.

Recipes on the `main` branch use the **latest** (which may be an alpha or snapshot) version of Nav3. For recipes that use **stable** versions, check the [releases page](https://github.com/android/nav3-recipes/releases).

## Recipes
These are the recipes and what they demonstrate.

### Basic API usage
- **[Basic](app/src/main/java/com/example/nav3recipes/basic)**: Shows most basic API usage.
- **[Saveable back stack](app/src/main/java/com/example/nav3recipes/basicsaveable)**: As above, with a persistent back stack.
- **[Entry provider DSL](app/src/main/java/com/example/nav3recipes/basicdsl)**: As above, using the entryProvider DSL.

### Deep links
Read the [guide to deeplinking](docs/deeplink-guide.md). Upvote [this issue](https://issuetracker.google.com/470282247) if you would like an API for deeplinks.
- **[Basic](app/src/main/java/com/example/nav3recipes/deeplink/basic)**: Shows how to parse a deep link URL from an Android Intent into a navigation key.
- **[Advanced](app/src/main/java/com/example/nav3recipes/deeplink/advanced)**: Shows how to handle deep links with a synthetic back stack and correct "Up" navigation behavior.

### Scenes
#### Use built-in Scenes
- **[Dialog](app/src/main/java/com/example/nav3recipes/dialog)**: Shows how to create a Dialog.

#### Create custom Scenes
- **[BottomSheet](app/src/main/java/com/example/nav3recipes/bottomsheet)**: Shows how to create a BottomSheet destination.
- **[List-Detail Scene](app/src/main/java/com/example/nav3recipes/scenes/listdetail)**: Shows how to create a custom, list-detail layout using a `Scene` and `SceneStrategy` (see video of UI behavior below).
- **[Two pane Scene](app/src/main/java/com/example/nav3recipes/scenes/twopane)**: Shows how to create a custom, 2-pane layout.

#### Use Material Scenes
Examples showing how to use the layouts provided by the [Compose Material3 Adaptive Navigation3 library](https://developer.android.com/jetpack/androidx/releases/compose-material3-adaptive#compose_material3_adaptive_navigation3_version_10_2)
- **[List-Detail](app/src/main/java/com/example/nav3recipes/material/listdetail)**: Shows how to use a Material adaptive list-detail layout.
- **[Supporting Pane](app/src/main/java/com/example/nav3recipes/material/supportingpane)**: Shows how to use a Material adaptive supporting pane layout.

Note: If you find a bug or have a feature request for Material3 Adaptive Scenes [please file it here](https://issuetracker.google.com/issues/new?component=1467081). Don't file an issue on this repository.

### Animations
- **[Animations](app/src/main/java/com/example/nav3recipes/animations)**: Shows how to override the default animations for all destinations and a single destination.

### Common back stack behavior
- **[Common navigation UI](app/src/main/java/com/example/nav3recipes/commonui)**: A common navigation toolbar where each item in the toolbar navigates to a top level destination.
- **[Multiple back stacks](app/src/main/java/com/example/nav3recipes/multiplestacks)**: Shows how to create multiple top level routes, each with its own back stack. Top level routes are displayed in a navigation bar allowing users to switch between them. State is retained for each top level route, and the navigation state persists config changes and process death.

### Conditional navigation
- **[Conditional navigation](app/src/main/java/com/example/nav3recipes/conditional)**: Switch to a different navigation flow when a condition is met. For example, for authentication or first-time user onboarding.

### Architecture
- **[Hilt - Modularized navigation code](app/src/main/java/com/example/nav3recipes/modular/hilt)**: Demonstrates how to decouple navigation code into separate modules (uses Dagger/Hilt for DI).
- **[Koin - Modularized navigation code](app/src/main/java/com/example/nav3recipes/modular/koin)**: Demonstrates how to decouple navigation code into separate modules (uses Koin for DI).

### Working with ViewModels
#### Passing navigation arguments
- **[Basic ViewModel](app/src/main/java/com/example/nav3recipes/passingarguments/viewmodels/basic)**: Navigation arguments are passed to a ViewModel constructed using `viewModel()`
- **[Hilt injected ViewModel](app/src/main/java/com/example/nav3recipes/passingarguments/viewmodels/hilt)**: Navigation arguments are passed to a ViewModel constructed using `hiltViewModel()`
- **[Koin injected ViewModel](app/src/main/java/com/example/nav3recipes/passingarguments/viewmodels/koin)**: Navigation arguments are passed to a ViewModel constructed using `koinViewModel()`

#### Sharing a ViewModel between screens
- **[Shared ViewModel](app/src/main/java/com/example/nav3recipes/sharedviewmodel)**: Demonstrates how to share a `ViewModel` between different screens (entries) in Navigation 3 using a custom `NavEntryDecorator`.

### Returning Results
- **[Returning Results as Events](app/src/main/java/com/example/nav3recipes/results/event)**: Returning results as events to content in another NavEntry.
- **[Returning Results as State](app/src/main/java/com/example/nav3recipes/results/state)**: Returning results as state stored in a CompositionLocal.

### Future recipes
The most upvoted [recipe requests]([url](https://github.com/android/nav3-recipes/issues?q=is%3Aissue%20state%3Aopen%20label%3Arecipe-request)) will be considered for implementation. Don't see your recipe? [File a request for one here](https://github.com/android/nav3-recipes/issues/new?template=1-recipe-request.md)

## Custom layout example
The following is a screen recording showing the navigation behavior of a [custom, list-detail Scene](app/src/main/java/com/example/nav3recipes/scenes/listdetail).

![Custom layout example](/docs/images/ListDetailScene.gif)

## Instructions
Clone this repository and open the root folder in [Android Studio](https://developer.android.com/studio). Each recipe is contained in its own package with its own `Activity`.

## Found an issue?
If the issue is _directly related to this project_, as in, it's reproducible without modifying this project's source code, then please [file an issue on github](https://github.com/android/nav3-recipes/issues/new?template=2-bug-report.md). If you've found an issue with the Jetpack Navigation 3 library, please [file an issue on the issue tracker](https://issuetracker.google.com/issues/new?component=1750212&template=2102223).

## Contributing
We'd love to accept your contributions. Please follow [these instructions](CONTRIBUTING.md).

## Compose Multiplatform Recipes
CMP recipes can be found [here](https://github.com/terrakok/nav3-recipes).

## License
```
Copyright 2025 The Android Open Source Project

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
