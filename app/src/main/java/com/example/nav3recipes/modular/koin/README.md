# Modular Navigation Recipe (Koin)

This recipe demonstrates how to structure a multi-module application using Navigation 3 and Koin for dependency injection. The goal is to create a decoupled architecture where navigation is defined and implemented in separate feature modules. It relies on the [`koin-compose-navigation3`](https://insert-koin.io/docs/reference/koin-compose/navigation3) artifact.

## How it works

The application is divided into several Android modules:

-   **`app` module**: This is the main application module. It `includes()` the feature modules and initializes a common `Navigator`.

-   **`common` module**: This module contains the core navigation logic used by both the application module and the feature modules. Namely, it defines a `Navigator` class that manages the back stack.

-   **Feature modules (e.g., `conversation`, `profile`)**: Each feature is split into two sub-modules:
    -   **`api` module**: Defines the public API for the feature, including its navigation routes. This allows other modules to navigate to this feature without needing to know about its implementation details.
    -   **`impl` module**: Provides the implementation of the feature, including its composables and Koin `Module`. The Koin module uses the [`navigation`](https://insert-koin.io/docs/reference/koin-compose/navigation3/#declaring-navigation-entries) DSL to define the entry provider installers for the feature module.

This modular approach allows for a clean separation of concerns, making the codebase more scalable and maintainable. Each feature is responsible for its own navigation logic, and the `app` module only combines these pieces together.
