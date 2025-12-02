# Generic Overlay

This recipe demonstrates how to show dialogs, alert dialogs, or bottom sheets as
overlays using the `OverlaySceneStrategy`.

## How it works

A new `OverlaySceneStrategy` is added to the `NavDisplay`. Any entry that adds
`OverlaySceneStrategy.overlay()` to its metadata will be displayed as an overlay
instead of replacing the previous scene.

This allows the entry itself to show any overlay UI such as:

- `Dialog`
- `AlertDialog`
- `ModalBottomSheet`

The strategy does not manage dialog parameters or properties. Developers define
the overlay directly inside the entry's composable, giving full control over the
UI and dismissal behavior.