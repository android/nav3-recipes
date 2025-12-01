# Deep Link Guide

This guide explains how to handle deep links with Navigation 3. It covers parsing
intents into navigation keys and managing backStacks for proper "Back" and "Up"
navigation behavior.

## 1. Parsing an intent into a navigation key

When your app receives a deep link, you need to convert the incoming `Intent`
(specifically the data URI) into a `NavKey` that your navigation system can
understand.

This process involves four main steps:

1.  **Define Supported Deep Links**: Create `DeepLinkPattern` objects that
    describe the URLs your app supports. These patterns link a URI structure to
    a specific `NavKey` class.
2.  **Parse the Request**: Convert the incoming `Intent`'s data URI into a
    readable format, such as a `DeepLinkRequest`.
3.  **Match Request to Pattern**: Compare the incoming request against your list
    of supported patterns to find a match.
4.  **Decode to Key**: Use the match result to extract arguments and create an
    instance of the corresponding `NavKey`.

### Example Implementation

The `com.example.nav3recipes.deeplink.basic` package provides an example of
this flow.

**Step 1: Define Patterns**

In a file (e.g., `MainActivity`), define a list of supported deeplink patterns.
You can leverage the `kotlinx.serialization` library by annotating your Navigation keys with
`@Serializable`, and then use the generated `Serializer` to map deep link arguments to a
key argument.

For example:

```kotlin
internal val deepLinkPatterns: List<DeepLinkPattern<out NavKey>> = listOf(
    // URL pattern with exact match: "https://www.nav3recipes.com/home"
    DeepLinkPattern(HomeKey.serializer(), (URL_HOME_EXACT).toUri()),
    
    // URL pattern with Path arguments: "https://www.nav3recipes.com/users/with/{filter}"
    DeepLinkPattern(UsersKey.serializer(), (URL_USERS_WITH_FILTER).toUri()),
    
    // URL pattern with Query arguments: "https://www.nav3recipes.com/users/search?{firstName}&{age}..."
    DeepLinkPattern(SearchKey.serializer(), (URL_SEARCH.toUri())),
)
```
The sample `DeepLinkPattern` class takes the defined URL pattern and serializer for the associated
key, then maps each argument ("{...}") to a field in the key. Using the serializer,
`DeepLinkPattern` stores the metadata for each argument such as its KType and its argument name.


**Step 2: Parse the Request**

In your Activity's `onCreate` method, you will first need to retrieve the data URI from the
intent. Then you should parse that URI into a readable format. In this recipe, we use a
`DeepLinkRequest` class which parses the URI's path segments and query parameters into a map. This
map will make it easier to compare against the patterns defined in Step 1.

**Step 3: Match Request to Pattern**

Once the request is parsed, iterate through your list of `deepLinkPatterns`. For each pattern,
use a matcher (e.g., `DeepLinkMatcher`) to check if the request's path and arguments align with
the pattern's structure. The matcher should return a result object containing the matched arguments
if successful, or null if not.

**Step 4: Decode to Key**

If a match is found, use the matched arguments to instantiate the corresponding `NavKey`.
Since we used `kotlinx.serialization` in Step 1, we can leverage a custom Decoder (`KeyDecoder`)
to decode the map of arguments directly into the strongly-typed key object.

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val uri: Uri? = intent.data
    
    val key: NavKey = uri?.let {
        // Step 2: Parse request
        val request = DeepLinkRequest(uri)
        
        // Step 3: Find match
        val match = deepLinkPatterns.firstNotNullOfOrNull { pattern ->
            DeepLinkMatcher(request, pattern).match()
        }
        
        // Step 4: Decode to NavKey
        match?.let {
            KeyDecoder(match.args).decodeSerializableValue(match.serializer)
        }
    } ?: HomeKey // Fallback to home if no match or no URI

    setContent {
        val backStack = rememberNavBackStack(key)
        // ... setup NavDisplay
    }
}
```

For more details, refer to the [Basic Deep Link Recipe](../app/src/main/java/com/example/nav3recipes/deeplink/basic/README.md)
and the `MainActivity` in `com.example.nav3recipes.deeplink.basic`.

## 2. Building a synthetic backStack & managing the Task stack

Handling deep links isn't just about showing the correct screen; it's also about
ensuring the user has a natural navigation experience when they press "Back" or
"Up". This often requires building a synthetic backStack.

### The Problem: New Task vs. Existing Task

When a user clicks a deep link, your activity might launch in a new Task or in
an existing one.

*   **Existing Task**: If an app is already open, the deep link might just
    push a new screen onto the current stack. The "Back" button should return
    the user to where they were before the deep link.
*   **New Task**: If your app wasn't already open (or if the intent flags forced a new
    task), there is no existing backStack. Yet the "Back" button should conceptually go "up" to
    the parent of the current screen, rather than exiting the app.

### Solution: Synthetic BackStack

A synthetic backStack is a manually constructed list of keys that represents
the path the user *would* have taken from the root screen to the current screen.

**Strategy**

1.  **Identify the Context**: Determine if you are in a new task or an existing
    one. Intent flags like `FLAG_ACTIVITY_NEW_TASK` would signal a new task.
2.  **Construct the Stack**:
    *   **Up Button**:
        *   In an **Existing Task**, you will need to restart the Activity in a new Task
            and build a backStack to ensure this parent exists
        *   In a **New Task**, "Back" should behave like "Up" (go to the
            parent). You will need to build a backStack to ensure this parent exists.
    *   **Back Button**:
        *   In an **Existing Task**, "Back" goes to the previous screen (the one
            that triggered the deep link).
        *   In a **New Task**, "Back" should behave like "Up" (go to the
            parent). You will need to build a backStack to ensure this parent exists.

### Task & backStack illustration

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

**Implementation Tips**

*   **Restarting for consistency**: If you want to enforce a specific backStack
    structure (like always having the app's home screen at the bottom), you
    might need to restart the Activity with `Intent.FLAG_ACTIVITY_NEW_TASK` and
    `Intent.FLAG_ACTIVITY_CLEAR_TASK` when handling a deep link, then build the
    full stack manually.

For a comprehensive demonstration of simulating "App A" deep linking into "App
B" and managing these stacks, see the [Advanced Deep Link Recipe](../app/src/main/java/com/example/nav3recipes/deeplink/advanced/README.md)
and the module `com.example.nav3recipes.deeplink.app`.
