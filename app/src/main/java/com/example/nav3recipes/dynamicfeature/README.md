# Dynamic Feature Module Recipe

This recipe demonstrates how to integrate Dynamic Feature Modules (DFMs) with Navigation 3 using `androidx.navigation3:navigation3-dynamic-features`. Make sure you are familiar with [Play Feature Delivery](https://developer.android.com/guide/playcore/feature-delivery#customize_delivery) before proceeding.

## How it works

This example defines screens across three modules with different delivery modes:

- `Home`: A screen in the base `:app` module with buttons to navigate to each dynamic feature module.
- `InstallTimeHome`: The initial key for `:dynamicfeature:installtime`, which is installed with the app at delivery time.
- `OnDemandHome`: The initial key for `:dynamicfeature:ondemand`, which is downloaded on demand when navigated to. `:dynamicfeature:ondemand` also defines private sub-screen keys (`OnDemandDetail` and `OnDemandSettings`) that `:app` does not depend on.

### `DynamicFeatureNavEntryDecorator`

`rememberDynamicFeatureNavEntryDecorator` creates a `NavEntryDecorator` that drives module installation through `SplitInstallManager` and swaps in the module's destination once installation completes.

Pass it to `NavDisplay` in `entryDecorators` (alongside `rememberSaveableStateHolderNavEntryDecorator()`) and build your `entryProvider` with `dynamicFeatureDecorator.entryProvider`, registering dynamic modules via `dynamicFeatureModule`. See [`DynamicFeatureActivity.kt`](DynamicFeatureActivity.kt) for the full setup.

Navigating to a dynamic module works the same as any other screen: add its initial key (`InstallTimeHome` or `OnDemandHome`) to the back stack. If the module is not yet installed, `DynamicFeatureNavEntryDecorator` starts installation, renders `loadingContent` (or `defaultLoadingContent`), runs `SplitCompat.install`, and transitions to the module's real entry once installed. If the entry leaves the back stack before installation finishes, the active install session is canceled automatically.

Your `Application` class must extend `SplitCompatApplication` (or call `SplitCompat.install(this)` in `attachBaseContext`) so code and resources of newly downloaded modules can be used immediately without restarting the app (and for `--local-testing` installs).

### `DynamicFeatureModuleEntryProvider`

Each dynamic feature module implements `DynamicFeatureModuleEntryProvider<NavKey>` with a public zero-argument constructor to register its `initialKey` and any internal sub-screen keys:

```kotlin
@Serializable
private sealed interface OnDemandSubKey : NavKey

@Serializable
private data class OnDemandDetail(val id: String) : OnDemandSubKey

class OnDemandModuleEntryProvider : DynamicFeatureModuleEntryProvider<NavKey> {
    override fun EntryProviderScope<NavKey>.buildEntries() {
        entry<OnDemandHome> {
            val navigator = rememberNavigator()
            OnDemandModuleScreen(
                onNavigateToDetail = { id -> navigator.goTo(OnDemandDetail(id)) },
                // ...
            )
        }
        entry<OnDemandDetail> { key ->
            // ...
        }
    }
}
```

### Sharing navigation state via Hilt `@EntryPoint`

Because dynamic feature modules depend on `:app` (reversed dependency direction) and `DynamicFeatureModuleEntryProvider` is instantiated via reflection, `:app` annotates `DynamicFeatureActivity` with `@AndroidEntryPoint` and exposes `@ActivityRetainedScoped` `DynamicFeatureNavigator` through a Hilt `@EntryPoint`. The dynamic feature module adds `implementation(libs.hilt.android)` and retrieves the navigator via `EntryPointAccessors`:

```kotlin
// Inside :app
@EntryPoint
@InstallIn(ActivityComponent::class)
interface DynamicFeatureEntryPoint {
    fun navigator(): DynamicFeatureNavigator
}

// Inside :dynamicfeature:ondemand
@Composable
private fun rememberNavigator(): DynamicFeatureNavigator {
    val activity = checkNotNull(LocalActivity.current)
    return remember(activity) {
        EntryPointAccessors.fromActivity<DynamicFeatureEntryPoint>(activity).navigator()
    }
}
```

> **Note:** `DynamicFeatureNavigator` retains the back stack across configuration changes, but does not survive process death. Back it with `SavedStateHandle` if your app needs to restore the back stack after process death.

### ProGuard rules

`navigation3-dynamic-features` bundles consumer keep rules that keep `DynamicFeatureModuleEntryProvider` implementations and their public zero-argument constructors when R8 minification is enabled, so no manual rules are needed in `proguard-rules.pro`.

## How to test locally

Running the `:app` configuration from Android Studio installs all dynamic feature modules immediately by default.

To simulate on-demand downloading and installation locally, use [bundletool](https://github.com/google/bundletool/releases):

> **Tip:** If you installed `bundletool` via Homebrew (`brew install bundletool`), you can replace `java -jar bundletool.jar` with `bundletool` in the commands below. Pass `--device-id <serial>` if multiple devices or emulators are connected.

1. Build the debug app bundle:
    ```shell
    ./gradlew :app:bundleDebug
    ```

2. Build local-testing APKs from the bundle:
    ```shell
    java -jar bundletool.jar build-apks --local-testing --bundle app/build/outputs/bundle/debug/app-debug.aab --output app-debug.apks --overwrite
    ```

3. Install the APKs onto your connected device or emulator:
    ```shell
    java -jar bundletool.jar install-apks --apks app-debug.apks
    ```

4. Launch **Nav3Recipes** from the device launcher, open **Dynamic Feature Module Navigation**, and tap **Go to On Demand Module Screen** to observe the download progress dialog and transition into the on-demand module.

## Implementation notes

- This recipe does not implement automatic retries for failed installations (`SplitInstallSessionStatus.FAILED` displays an error dialog and removes the entry when dismissed). See [Handle request errors](https://developer.android.com/guide/playcore/feature-delivery/on-demand#handle_request_errors) for retry guidance.
