# Colocated DeepLinkKey Recipe

This recipe demonstrates how to colocate deep link URI pattern templates directly with navigation key (`NavKey`) definitions using a `DeepLinkKey` interface in Navigation 3.

## How it works

This recipe consists of two activities:
- `DeepLinkKeyActivity`: A sandbox UI that allows testing deep links for different destinations (`HomeKey`, `UserKey`, `ProductKey`) by constructing URI strings and launching `MainActivity`.
- `MainActivity`: Constructs a `DeepLinkRequest(intent)`, matches it against a list of `UriDeepLinkMatcher`s created from the colocated URI patterns of each key, and displays the matched destination in `NavDisplay`.

## Key Concepts

1. **Colocated URI Templates (`DeepLinkKey`)**:
   Instead of hardcoding URI pattern strings in activities or external routing tables, each navigation key implements `DeepLinkKey` and defines its supported URI pattern directly in its declaration:
   ```kotlin
   interface DeepLinkKey : NavKey

   @Serializable
   data class UserKey(val id: Int) : DeepLinkKey {
       companion object {
           const val URI_PATTERN = "https://www.nav3recipes.com/user/{id}"
       }
   }
   ```

2. **Clean `UriDeepLinkMatcher` Registration**:
   `UriDeepLinkMatcher` instances can be created cleanly using the colocated `URI_PATTERN` constants and `serializer<T>()`:
   ```kotlin
   private val deepLinkMatchers = listOf(
       UriDeepLinkMatcher(HomeKey.URI_PATTERN.toUri(), serializer<HomeKey>()),
       UriDeepLinkMatcher(UserKey.URI_PATTERN.toUri(), serializer<UserKey>()),
       UriDeepLinkMatcher(ProductKey.URI_PATTERN.toUri(), serializer<ProductKey>()),
   )
   ```

3. **`bestMatch` Resolution (`matches.maxOrNull()`)**:
   When matching an incoming request URI, Navigation 3 scores matches based on specificity (exact path segments vs. placeholders, number of query parameters, etc.). Calling `matches.mapNotNull { it.match(request) }.maxOrNull()` automatically selects the highest-scoring (most specific) match.
