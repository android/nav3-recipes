# Interop Recipe

This recipe demonstrates how to use `AndroidFragment` and `AndroidView` within a Navigation3 application.

## Features

- **AndroidFragment**: Shows how to embed a Fragment inside a Composable destination.
- **AndroidView**: Shows how to embed a classic Android View inside a Composable destination.

## Key Components

- `InteropActivity`: The main activity hosting the navigation.
- `MyCustomFragment`: A simple Fragment used in the example.
- `AndroidFragment<T>`: A Composable that hosts a Fragment.
- `AndroidView`: A Composable that hosts an Android View.

## Usage

1.  Run the `InteropActivity`.
2.  The initial screen shows a Fragment.
3.  Click "Go to View" to navigate to a screen displaying a `TextView`.
