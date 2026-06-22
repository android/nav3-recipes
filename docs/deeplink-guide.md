# Deep Link Guide

This guide covers the basic principles of deep linking and demonstrates how to apply them
in Navigation 3. 

The implementation covers parsing intents into navigation keys and managing synthetic backStacks for 
proper "Back" and "Up" navigation behavior.

But first, let's go over some key concepts of navigation and deep linking.

# Principles of Navigation
[Princples of Navigation](https://developer.android.com/guide/navigation/principles) highlights
key concepts for implementing navigation systems that provide a consistent and pleasant user
experience. 

**Important Terms & Concepts**: Consider familiarizing with terms and
concepts [here](#important-terms--concepts) before continuing with this guide.

Three principles are particularly relevant for deep linking.

## Up and Back are identical within your app's task
When deep linking to your app's Task, Up and Back should both return 
to a screen that the user would most likely have seen before the current screen. 

This implies that Up and Back behaves differently when your Activity is started in another 
App's Task. The Back button should return to the screen before deep linking, which is likely the 
screen of the other App. The Up button should redirect to a screen *within your app* that the 
user would most likely have seen before the current screen.

## The Up button never exits your app
The Up button should always navigate within your App's screens and never exit to another app 
or to the device's home screen. This means the Up button should be
disabled on your App's start destination.

## Deep linking simulates manual navigation
Deep linking jumps directly to any destination on your App, with the backStack populated with
just the one destination. To support natural Up/Back to the start destination, you should 
build a *synthetic back stack* that simulates the path the user would have taken to reach 
that destination manually.

# Implementing Deep Links in Navigation 3

This guide applies the [Principles of Navigation](#principles-of-navigation) to demonstrate how to
handle deep links in your app.

Note that each step of this guide can be implemented in various ways depending on your app's
architecture and requirements. The approach shown here is just one possible way to handle deep links
with Navigation 3.

Let's start with the basics: how to parse an Intent's URL into a navigation key.

## Parsing an intent into a navigation key

When your app receives a deep link, you convert the incoming `Intent` into navigation keys.

This process involves the following steps:

1.  **Define matchers**: Create `UriDeepLinkMatcher` instances that describe the deep links your app supports and which can create corresponding navigation keys.
2.  **Model the request**: Create a `DeepLinkRequest` to model the incoming deep link. You can use the `DeepLinkRequest.fromIntent(intent)` helper to create one from an `Intent`, capturing the `uri`, `action`, and `mimeType` of the `Intent`.
3.  **Match the request**: Call `matcher.match(request)` for each of your matchers, collecting the results.
4.  **Determine the best result**: Filter out `null` results and then find the best match by making use of `MatchResult`'s implementation of the `Comparable` interface.

### Example Implementation

The `com.example.nav3recipes.deeplink.basic` package provides an example of this flow in `MainActivity.kt`, using the `UriDeepLinkMatcher` to match requests.

In your app, define the list of supported deep link matchers:

```kotlin
internal val deepLinkMatchers: List<DeepLinkMatcher<out NavKey>> = listOf(
    // URL pattern with exact match: "https://www.nav3recipes.com/home"
    UriDeepLinkMatcher(DeepLinkUri(URL_HOME_EXACT), HomeKey.serializer()),
    
    // URL pattern with Path arguments: "https://www.nav3recipes.com/users/with/{filter}"
    UriDeepLinkMatcher(DeepLinkUri(URL_USERS_WITH_FILTER), UsersKey.serializer()),
    
    // URL pattern with Query arguments: "https://www.nav3recipes.com/users/search?firstName={firstName}&ageMin={ageMin}..."
    UriDeepLinkMatcher(DeepLinkUri(URL_SEARCH), SearchKey.serializer()),
)
```

In your Activity's `onCreate` method, retrieve and parse the intent, find the highest-ranking match result, and extract the strongly-typed key:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val key: NavKey = intent.data?.let {
        val request = DeepLinkRequest.fromIntent(intent)

        deepLinkMatchers.asSequence()
            .mapNotNull { matcher ->
                @Suppress("UNCHECKED_CAST")
                matcher.match(request) as? MatchResult<NavKey>
            }
            .maxOrNull()
            ?.key
    } ?: HomeKey // Fallback to home if no URI or no match

    setContent {
        val backStack = rememberNavBackStack(key)
        // ... setup NavDisplay
    }
}
```

For more details, refer to the [Basic Deep Link Recipe](../app/src/main/java/com/example/nav3recipes/deeplink/basic/README.md).

## Synthetic backStack & managing Tasks

Now we are ready to apply the Principles of Navigation. Their application is demonstrated
in the [deeplink.advanced](../app/src/main/java/com/example/nav3recipes/deeplink/advanced) and [deeplink.app](/advanceddeeplinkapp/src/main/java/com/example/nav3recipes/deeplink/advanced)
modules.

### Identifying the Task

First, how do you check which Task your Activity is started on? One of the most
common way to start an Activity in a new Task is by attaching the `Intent.FLAG_ACTIVITY_NEW_TASK`
flag to the deep link Intent.

In the Activity's `onCreate` method, you can access the Intent and check if that flag is present.

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val flags = intent.flags
    val isNewTask = flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0
    // ...
}
```
If `isNewTask` is true, the Activity is started in a new Task. Otherwise, the Activity
is started in the current Task.

### Applying Principle 1: Up and Back in New Task

If the Activity is started in a new Task, you will need to build a synthetic backStack to support
both Up and Back.

First, create an interface with a `parent` field that all deep link keys inherit.

```kotlin
interface DeepLinkKey: NavKey {
    val parent: NavKey
}

object HomeKey: NavKey

object UserListKey: DeepLinkKey {
    override val parent = HomeKey
}

class UserDetailKey(user: User): DeepLinkKey {
    override val parent = UserListKey
}
```

After you parse the deep link url into a key, build a synthetic backStack by iterating
up the key's parents.

```kotlin
fun buildSyntheticBackStack(
    deeplinkKey: DeepLinkKey,
): List<NavKey> {
    return buildList {
        var node: DeepLinkKey? = startKey
        while (node != null) {
            // ensure the parent is added to the start of the list
            add(0, node)
            val parent = if (node is DeepLinkKey) {
                node.parent
            } else null
            node = parent
        }
    }
}
```

**Note:** There are no hard rules as to what the "parent" key should be. In general it should
be the screen that is most natural / likely to come before the child key if the user
had manually navigated to the child key.

Now pass that returned backStack to the NavDisplay

```kotlin
val syntheticBackStack = buildBackStack(deeplinkKey)

setContent {
    val backStack: NavBackStack<NavKey> = rememberNavBackStack(*(syntheticBackStack.toTypedArray()))
    NavDisplay(
        backStack = backStack,
        // ...
    ) {
        // ...
    }
}
```

### Applying Principle 1: Up and Back in Original Task

If the Activity is started in the current Task, Up and Back buttons are handled differently.

#### Back button
You simply need to add the deeplink key to the backStack

```kotlin
setContent {
    // val deeplinkKey = parse deep link into key...
    val backStack: NavBackStack<NavKey> = rememberNavBackStack(deeplinkKey)
    NavDisplay(
        backStack = backStack,
        // ...
    )
}
```

Optionally you can also create a synthetic backStack appropriate to your app's needs.

#### Up button
Up button should never exit your app or return to the previous app. So you need to restart your
App in a new Task and then build a synthetic backStack as demonstrated above.

To restart the App, you will need a few things:
1.  the Context of the Activity
2.  the Activity itself
3.  the deep link url of the current screen's parent (remember, the current screen is technically
    already popped from the backStack once the user clicked Up)

Then build a TaskStackBuilder with an Intent containing the deep link url:

```kotlin
private fun createTaskStackBuilder(
    deeplinkKey: NavKey?,
    activity: Activity?,
    context: Context
): TaskStackBuilder {

    // The intent to restart the current activity
    val intent = if (activity != null) {
        Intent(context, activity.javaClass)
    } else {
        val launchIntent =
            context.packageManager.getLaunchIntentForPackage(context.packageName)
        launchIntent ?: Intent()
    }

    // Pass in the deeplink url of the parent key
    if (deeplinkKey != null && deeplinkKey is NavDeepLinkRecipeKey) {
        intent.data = deeplinkKey.deeplinkUrl.toUri()
    }

    // Ensure that the MainActivity is restarted as the root of a new Task
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

    // Lastly, attach the intent to the TaskStackBuilder
    return TaskStackBuilder.create(context).addNextIntentWithParentStack(intent)
}
```

Now start the activity:

```kotlin
fun navigateUp(
    // pass in required context, activity etc
) {
    if (onOriginalTask) {
        val taskStackBuilder = createTaskStackBuilder(deeplinkKey, activity, context)
        // ensure current activity is finished
        activity?.finish()
        // invoke restart
        taskStackBuilder.startActivities()
    }
}
```

You Activity should see that the `Intent.FLAG_ACTIVITY_NEW_TASK` is present and build a
synthetic backStack as demonstrated above.

### Applying Principle 2: The Up button never exits your app

Aside from restarting your Activity on a new Task, make sure your App's start destination does
not show the Up button

```kotlin
TopAppBar(
    // ...
    navigationIcon = {
        /**
         * Only display Up button if not on Root screen
         */
        if (currentKey != backStack.first()) {
            IconButton(onClick = {
                backStack.navigateUp()
            }) {
                // ...
            }
        }
    },
)
```
### Applying Principle 3: Deep linking simulates manual navigation

In general this means simulating a backStack as if the user had manually navigated from the
start destination to the deep linked destination, which is demonstrated in the above sections.

There is no hard definition for what the "correct" parent but some possible considerations
include:

1.  shortest path from start destination to deeplinked destination
2.  a parent that allows navigating to sibling destinations
3.  conditional parenting based on app state

## Summary

To summarize, the tables below show the recommended behavior of Up and Back buttons 
depending on the Task.

**Existing Task**

| Task        | Target                      | Synthetic backStack                             |
|-------------|-----------------------------|-------------------------------------------------|
| Up Button   | Deep linked Screen's Parent | Restart Activity on new Task & build backStack  |
| Back Button | Screen before deep linking  | None                                            |

**New Task**

| Task        | Target                      | Synthetic backStack                            |
|-------------|-----------------------------|------------------------------------------------|
| Up Button   | Deep linked Screen's Parent | Build backStack on Activity creation           |
| Back Button | Deep linked Screen's Parent | Build backStack on Activity creation           |


### Important Terms & Concepts

**Task**: A collection of Activities arranged in a stack. A cohesive unit that can move from
the foreground to the background and vice versa. On a mobile device, each Task is represented by
one of the windows in the "recent apps" view when you swipe up from the bottom
of the screen. Each one of these windows (or Task) is usually marked with the launcher icon of 
the App that is at the root of the Task stack. For more info see [this doc](https://developer.android.com/guide/components/activities/tasks-and-back-stack).

**BackStack**: Can be used to described a Task's stack. More commonly used to describe a stack of
destinations (or representations of destinations) that the user navigates through, starting from 
the app's start destination to the current destination. Common operations on a backStack 
include pushing new destinations onto the top of the stack (navigating forward), and popping
off destinations from the top of the stack (navigating back).

**Synthetic BackStack** A manually constructed stack of destinations that simulates
the path the user *would* have taken from the root destination to the current destination. This
path should represent the most natural or most likely sequence of screens that users would have
naturally navigated through in order to reach the current screen.

**Activity** A window in which an App draws its UI. An App may consist of one or more Activities.
See [this documentation](https://developer.android.com/guide/components/activities/intro-activities) for more details on Activities. Each App should have exactly
one "Launcher" Activity that serves as the entry point to the App, with optional additional
Activities that the user can navigate to.

**Task vs App vs Activity** As mentioned, a Task is a stack of Activities. You can think of the
App of the root Activity as the owner of this Task. It is also possible to start the Activity of
*another* app within this Task, such as when you deep link. For example *App A* owns *Task A*, and
App A deep links into *App B*'s Activity. In this scenario if you go to the "recept apps" 
view (swipe up from the bottom of the screen), you will see that even though you are seeing
App B's screen, the launcher icon attached to the window is App A's. You will need to launch
App B's Activity in a brand new Task with App B as the root in order to be in App B's own Task.