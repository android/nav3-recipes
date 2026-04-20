# Modular Navigation Recipe (Hilt)

This recipe demonstrates how to structure a multi-module application using Navigation 3 and Dagger/Hilt for dependency injection. The goal is to create a decoupled architecture where navigation is defined and implemented in separate feature modules.

## How it works

The application is divided into several modules:

-   **`app` module**: This is the main application module. It initializes a common `Navigator` and injects a set of `EntryProviderInstaller`s from the feature modules. It then uses these installers to build the final `entryProvider` for the `NavDisplay`.

-   **`common` module**: This module contains the core navigation logic, including:
    -   A `Navigator` class that manages the back stack.
    -   An `EntryProviderInstaller` type, which is a function that feature modules use to contribute their navigation entries to the application's `entryProvider`.

-   **Feature modules (e.g., `conversation`, `profile`)**: Each feature is split into two sub-modules:
    -   **`api` module**: Defines the public API for the feature, including its navigation routes. This allows other modules to navigate to this feature without needing to know about its implementation details.
    -   **`impl` module**: Provides the implementation of the feature, including its composables and an `EntryProviderInstaller` that maps the feature's routes to its composables. This installer is then provided to the `app` module using Dagger/Hilt.

This modular approach allows for a clean separation of concerns, making the codebase more scalable and maintainable. Each feature is responsible for its own navigation logic, and the `app` module only combines these pieces together.
