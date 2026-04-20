# Retain Recipe

This recipe demonstrates how to retain values for items on the back stack.
It also shows how to implement a custom NavEntryDecorator.

## How it works

To use retain with nav 3, you need to define and install a NavEntryDecorator.
This decorator will mange a `RetainedValuesStoreRegistry` that wraps destinations in a unique `RetainedValuesStore`.

The decorator implemented in this sample will keep retained values for as long as the item is in the backstack.
If an item is popped and then re-added, it will get new retained values.

For more information on retain, see the documentation for
[`retain`](https://developer.android.com/reference/kotlin/androidx/compose/runtime/retain/package-summary?hl=en#retain(kotlin.Function0)),
[`RetainedValuesStore`](https://developer.android.com/reference/kotlin/androidx/compose/runtime/retain/RetainedValuesStore?hl=en),
and [`RetainedValuesStoreRegistry`](https://developer.android.com/reference/kotlin/androidx/compose/runtime/retain/RetainedValuesStoreRegistry?hl=en).

For information on NavEntryDecorator, see [the official documentation](https://developer.android.com/guide/navigation/navigation-3/naventrydecorators).