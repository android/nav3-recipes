# Gemini Context: Navigation 3 Recipes

This repository contains code recipes for [Jetpack Navigation 3](https://goo.gle/nav3), a library for Android app navigation.

## Project Goal
To provide clear, concise, and modern examples of how to implement common navigation use cases using Navigation 3 APIs. Each recipe should focus on a single concept.

## Navigation 3 Patterns
Follow the patterns defined in [styleguide.md](styleguide.md) when creating and updating files.

## Adding a New Recipe
When adding a new recipe:
1. Create a new package under `app/src/main/java/com/example/nav3recipes/`.
2. Add a new `Activity` and its content.
3. Update `RecipePickerActivity.kt` to include the new recipe in the list.
4. Update the top-level `README.md` with a description and link.

## Project Structure
- `app/`: Main application module containing most recipes.
- `common/`: Shared code and UI components used across recipes.
- `advanceddeeplinkapp/`: A separate module for advanced deep link demonstrations.
- `.gemini/styleguide.md`: Detailed style guide for the project.
