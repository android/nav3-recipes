# Dynamic Feature Module Recipe

This recipe demonstrates how to integrate Dynamic Feature Module (DFM) with Navigation3. Make sure that you're already familiar with [Play Feature Delivery](https://developer.android.com/guide/playcore/feature-delivery#customize_delivery) to proceed with this recipe.

## How it works

This example defines three routes on three different modules and delivery options:
- `RegularModuleScreen`: A screen that displays two buttons to navigate into other features, located inside base `:app` module.
- `InstallTimeModuleScreen`: A screen with install time delivery option, located inside `:dynamicfeature:installtime` module.
- `OnDemandModuleScreen`: A screen with on-demand delivery option, located inside `:dynamicfeature:ondemand` module.

### `DynamicFeatureContentProvider<T>`

An interface for communication with `dynamicFeatureEntry()`, every entry on the dynamic feature modules **must** implement this interface with `T` as the key/arguments for the entry.
```kotlin
// Inside :example module

@Suppress("unused")
class ExampleContentProvider : DynamicFeatureContentProvider<ExampleKey> {
    @Composable
    override fun Content(key: ExampleKey) {
        ExampleScreen()
    }
}

@Composable
private fun ExampleScreen() {
    // ...
}
```

### `dynamicFeatureEntry()`

A modified `entry` function of `EntryProviderScope` which resolves content from the class name that implements `DynamicFeatureContentProvider` using Reflection.
```kotlin
// Inside :app module

NavDisplay(
    // ...
    entryProvider = entryProvider {
        // ...
        dynamicFeatureEntry<ExampleKey>(
            clazzName = "fully.qualified.class.name.of.ExampleContentProvider"
        )
    }
)
```

### `DynamicFeatureManager`

A class that manages dynamic feature module installation, comes with a download state to monitor the download progress.

To install a module, simply invoke the function:
```kotlin
// Inside :app module

// Initialize the manager
val dynamicFeatureManager = retainDynamicFeatureManager()

// E.g. in a Button's onClick
dynamicFeatureManager.installModule(moduleName = "example") {
    // action when module is installed
    backStack.add(ExampleKey)
}
```

> Note: every navigation into dynamic feature module entries must be performed through the install module function to avoid skipping download on-demand modules that leads to crash.

To monitor the download progress, attach the manager into `DynamicFeatureDownloadProgressDialog` composable:
```kotlin
DynamicFeatureDownloadProgressDialog(dynamicFeatureManager)
```

### Proguard rules
To make sure that the dynamic feature content can be accessed when R8 minification turned on, add this rule into `proguard-rules.pro`:
```
-keep class * implements fully.qualified.class.name.of.DynamicFeatureContentProvider {
   public <init>();
}
```

## How to test locally

To test if the implementation is working, simply run the app and navigate into the target module's entries. Android Studio will include all the dynamic feature modules on the run configuration by default.

To simulate the module downloading and installation, use [bundletool](https://github.com/google/bundletool/releases).

1. Build the project AAB.
```shell
./gradlew :app:bundleDebug
```

2. Convert the built AAB into APKS with local testing enabled using `bundletool`.
```shell
java -jar bundletool.jar build-apks --local-testing --bundle <project-path>/app/build/outputs/bundle/debug/app-debug.aab --output app-debug.apks --overwrite
```

3. Install the converted APKS using `bundletool`.
```shell
java -jar bundletool.jar install-apks --apks app-debug.apks
```

4. Run the app via the launcher icon.

Now you should be able to see the download progress dialog when navigating to an on-demand module entry for the first time.
