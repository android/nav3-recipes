# Dynamic Feature Module Recipe

This recipe demonstrates how to integrate Dynamic Feature Module (DFM) with Navigation 3. Make sure that you're already familiar with [Play Feature Delivery](https://developer.android.com/guide/playcore/feature-delivery#customize_delivery) to proceed with this recipe.

## How it works

This example defines three keys for screens in three different modules and delivery options:

- `Home`: A screen in the main `:app` module that displays buttons to navigate into the screens from the dynamic feature modules.
- `InstallTimeModule.Home`: A screen in the `:dynamicfeature:installtime` module, which is installed at delivery time. For example, this might be used for an asset-rich onboarding module that is needed when the app is first installed, but which can be deleted after the onboarding is complete.
- `OnDemandModule.Home`: A screen in the `:dynamicfeature:ondemand` module, which is installed on-demand. For example, this might be used for a module with a large file size that only a small subset of the userbase is expected to use.

### `DynamicModule`

`DynamicModule` is an abstract class that is used to model a dynamic feature module. Namely, it stores both the `moduleName` and the `entryBuilderClassName` for the corresponding `DynamicModuleEntryBuilder` (described in the next section).

```kotlin
object ExampleModule : DynamicModule(
    entryBuilderClassName = "com.example.module.ExampleEntryBuilder",
    moduleName = "example"
) {
    @Serializable
    data object Screen : AppNavKey
}
```

### `DynamicModuleEntryBuilder`

Each dynamic feature module **must** include an implementation of `DynamicModuleEntryBuilder`, which is used to add the entries for that module to the `entryProvider` in the main `:app` module.

```kotlin
// Inside :example module

@Suppress("unused")
class ExampleEntryBuilder : DynamicModuleEntryBuilder {
    override fun EntryProviderScope<NavKey>.build() {
        appEntry<ExampleModule.Screen> {
            ExampleScreen()
        }
    }
}

@Composable
private fun ExampleScreen() {
    // ...
}
```

### `buildDynamicEntries()`

An extension function of `EntryProviderScope` which resolves the `DynamicModuleEntryBuilder` for that module and calls it's `build()` function.

```kotlin
// Inside :app module

val ALL_DYNAMIC_MODULES_MAP = listOf(
   ExampleModule,
   // ...
).associateBy { it.moduleName }

NavDisplay(
    // ...
    entryProvider = entryProvider {
        // ...
        dynamicFeatureManager.installedModules
            .mapNotNull { ALL_DYNAMIC_MODULES_MAP[it] }
            .forEach { buildDynamicEntries(it) }
    }
)
```

### `DynamicFeatureManager`

A class that manages dynamic feature module installation.

To navigate to a key contained within a dynamic feature module, use the `installModule` method, which automatically handles installing the respective module if it isn't already installed and executing a callback when the module is installed. If the module is already installed, this callback is invoked immediately. 

```kotlin
// Inside :app module

// Initialize the manager
val dynamicFeatureManager = retainDynamicFeatureManager()

// E.g. in a Button's onClick
dynamicFeatureManager.installModule(
    moduleName = ExampleModule.moduleName,
    onModuleInstalled = {
        backStack.add(ExampleModule.Screen)
    }
)
```

To monitor the installation progress, attach the manager into `DynamicFeatureDownloadProgressDialog` composable:

```kotlin
DynamicFeatureDownloadProgressDialog(dynamicFeatureManager)
```

### Proguard rules

To make sure that the class referred to by the `entryBuilderClassName` can be accessed when R8 minification is turned on, add this rule into `proguard-rules.pro`:

```
-keep class * implements com.example.nav3recipes.dynamicfeature.DynamicModuleEntryBuilder {
   public <init>();
}
```

## How to test locally

To test if the implementation is working, simply run the app and navigate into the target module's entries. Android Studio will include all the dynamic feature modules on the run configuration by default.

To simulate the module downloading and installation, use [bundletool](https://github.com/google/bundletool/releases).

> **Tip:** If you installed `bundletool` via Homebrew (`brew install bundletool`), you can replace `java -jar bundletool.jar` with just `bundletool` in the commands below.

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

## Implementation notes

`DynamicFeatureManager`
* This recipe only supports a single installation session at any given time. If your app needs to use multiple sessions concurrently, you should adapt the manager to track multiple sessions simultaneously.
* This recipe doesn't implement automatic retries for things like network errors or internal errors. See [Handle request errors](https://developer.android.com/guide/playcore/feature-delivery/on-demand#handle_request_errors) for additional information.
* If your app [accesses resources and assets from a different module](https://developer.android.com/guide/playcore/feature-delivery/on-demand#access_resource_different_module), note that this recipe doesn't automatically reinstall `SplitCompat` after a module has been installed.
