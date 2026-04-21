# Navigation 3 Recipes Style Guide

This project contains recipes for using the Navigation 3 library. To maintain consistency and discoverability, follow these guidelines.

## General Principles

- **Single Concept**: Each recipe should introduce a single concept. Avoid making existing recipes more complex; instead, create a new recipe for a new concept.
- **Modern Android**: Use Jetpack Compose, Material 3, and Kotlin Coroutines/Flow where appropriate.
- **Edge-to-Edge**: All activities should support edge-to-edge display using `setEdgeToEdgeConfig()`.

## Navigation 3 Patterns

- **Routes**: Use `@Serializable` classes or objects that implement `NavKey` for routes.
- **Back Stack**: Use `rememberNavBackStack()` for standard navigation or `rememberSaveable` with `mutableStateListOf` for custom back stack management (e.g., `Parcelable` routes).
- **Entry Provider**: Prefer the `entryProvider` DSL for defining navigation entries.
  - Exception: "Basic" recipes
- **Resumed Navigation**: Use `dropUnlessResumed` for navigation actions (e.g., button clicks that update navigation state) to prevent multiple rapid clicks or navigation from background.
  - Exception: Optional for the click handlers used by `NavigationBarItem`, `NavigationRailItem`, or equivalent composables for navigate between top-level destinations.
- **Scenes**: Implementations of the `Scene` interface should be data classes or implement `equals` and `hashcode` to ensure that the same `Scene` is used when appropriate.

## Recipe Discoverability and Maintenance

When adding a new recipe or updating an existing one, you **must** ensure the following are kept in sync:

1.  **Top-level README.md**: Add the recipe to the appropriate category in the top-level `README.md`. Include a brief description and a link to the recipe's package.
  - Exception: The migration recipe
2. **Recipe-level README.md**: Each recipe should have its own README.md file explaining the purpose of the recipe, with a focus on the specific APIs or patterns that it demonstrates.
  - If a recipe-level README.md includes any code blocks, ensure that they are kept in sync with the actual code in the repo.
3. **RecipePickerActivity.kt**: Add the recipe to the `recipes` list in `RecipePickerActivity.kt` so it can be launched from the app's main screen. Ensure it is placed under the correct `Heading`.
  - Exception: The migration recipe

## Code Style

- Follow the [official Kotlin style guide](https://kotlinlang.org/docs/coding-conventions.html).
- Use descriptive names for routes and composables.
- Ensure all new files have the appropriate license header.
- Validate that there are no unused imports.
