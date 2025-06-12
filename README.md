# Navigation 3 - Code recipes
[Jetpack Navigation 3](https://goo.gle/nav3) is a library for app navigation. This repository contains recipes for how to 
use its APIs to implement common navigation use cases.

## Recipes
These are the recipes and what they demonstrate. 

**Basic API examples**
- **[Basic](app/src/main/java/com/example/nav3recipes/basic)**: Shows most basic API usage.
- **[Saveable back stack](app/src/main/java/com/example/nav3recipes/basicsaveable)**: As above, with a persistent back stack.
- **[Entry provider DSL](app/src/main/java/com/example/nav3recipes/basicdsl)**: As above, using the entryProvider DSL.

**Layouts and animations**
- **[Material adaptive](app/src/main/java/com/example/nav3recipes/scenes/materiallistdetail)**: Shows how to use a Material list-detail layout. 
- **[Custom Scene](app/src/main/java/com/example/nav3recipes/scenes/twopane)**: Shows how to create a custom layout using a `Scene` and `SceneStrategy` (see video of UI behavior below).
- **[Animations](app/src/main/java/com/example/nav3recipes/animations)**: Override the default animations for all destinations and a single destination.

**Common use cases**
- **[Common navigation UI](app/src/main/java/com/example/nav3recipes/commonui)**: A common navigation toolbar where each item in the toolbar navigates to a top level destination.
- **[Conditional navigation](app/src/main/java/com/example/nav3recipes/conditional)**: Switch to a different navigation flow when a condition is met. For example, for authentication or first-time user onboarding.

**Architecture**
- **[Modularized navigation code](app/src/main/java/com/example/nav3recipes/modular)**: Demonstrates how to decouple navigation code into separate modules. Feature modules inject their content using Hilt. 

**Passing navigation arguments to ViewModels**
- **[Basic ViewModel](app/src/main/java/com/example/nav3recipes/passingarguments/basicviewmodels)**: Navigation arguments are passed to a ViewModel constructed using `viewModel()`
- **[Hilt injected ViewModel](app/src/main/java/com/example/nav3recipes/passingarguments/injectedviewmodels)**: Navigation arguments are passed to a ViewModel constructed using `hiltViewModel()`

**Planned**
- **Deeplinks**: Create and handle deeplinks to specific destinations
- **Android XR**: Custom navigation and layout behavior for Android XR
- **Returning a result from a destination**: Return a result to a previous destination

## Custom layout example
The following is a screen recording showing the navigation behavior of a [custom, two-pane Scene](app/src/main/java/com/example/nav3recipes/scenes/twopane).

![Custom layout example](/docs/images/TwoPaneScene.gif)

## Instructions
Clone this repository and open the root folder in [Android Studio](https://developer.android.com/studio). Each recipe is contained in its own package with its own `Activity`.

## Found an issue?
If the issue is _directly related to this project_, as in, it's reproducible without modifying this project's source code, then please [file an issue on github](https://github.com/android/nav3-recipes/issues/new). If you've found an issue with the Jetpack Navigation 3 library, please [file an issue on the issue tracker](https://issuetracker.google.com/issues/new?component=1750212&template=2102223).

## Contributing
We'd love to accept your contributions. Please follow [these instructions](CONTRIBUTING.md).

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
