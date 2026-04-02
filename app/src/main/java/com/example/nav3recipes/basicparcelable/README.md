# Basic Parcelable Recipe

This recipe shows a basic example of how to create a persistent back stack that survives configuration changes, without using `kotlinx.serialization`. Instead, it uses Android's `Parcelable` and the [`kotlin-parcelize`][1] plugin to save and restore the navigation state.

## How it works

In this example, `RouteA` and `RouteB` implement a marker interface, `Route`, which itself extends `Parcelable`. They are also annotated with `@Parcelize` from the `kotlin-parcelize` plugin, which automatically generates a `Parcelable` implementation:

```kotlin
sealed interface Route : Parcelable

@Parcelize
data object RouteA : Route

@Parcelize
data class RouteB(val id: String) : Route
```

To make the back stack persistent, this recipe defines the `rememberParcelableBackStack` function. To ensure that `NavDisplay` and other composables are aware of changes to the back stack, the back stack is stored in a `SnapshotStateList`.

```kotlin
@Composable
fun <T : Parcelable> rememberParcelableBackStack(vararg elements: T): SnapshotStateList<T> {
    return rememberSaveable {
        mutableStateListOf(*elements)
    }
}
```

This acts as an alternative to the built-in `rememberNavBackStack` from `androidx.navigation3.runtime` which relies on `kotlinx.serialization`. Use this approach if your application strictly prefers `Parcelable` and avoids depending on `kotlinx.serialization`.

[1]: https://developer.android.com/kotlin/parcelize