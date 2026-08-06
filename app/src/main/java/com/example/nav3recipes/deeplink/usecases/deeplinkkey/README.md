# DeepLinkKey Recipe

This recipe demonstrates how to associate deep link URI pattern templates with navigation keys using a `DeepLinkKey` interface in Navigation 3.

## How it works

This recipe consists of two activities:
- `DeepLinkKeyActivity`: A sandbox UI that allows testing deep links for different destinations (`HomeKey`, `UserKey`, `ProductKey`) by constructing URI strings and launching `MainActivity`.
- `MainActivity`: Constructs a `DeepLinkRequest(intent)`, matches it against a list of `UriDeepLinkMatcher`s created from each key's `uriPattern`, and displays the matched destination in `NavDisplay`.

## Key Concepts

1. **`DeepLinkKey` Interface**:
   Associates a navigation key with its supported URI pattern:
   - For singleton destinations (`HomeKey`), the object implements both `DeepLinkKey` and `NavKey`.
   - For parameterized destinations (`UserKey`), the companion object implements `DeepLinkKey`.
   ```kotlin
   interface DeepLinkKey {
       val uriPattern: String
   }

   @Serializable
   data object HomeKey : DeepLinkKey, NavKey {
       override val uriPattern: String = "https://www.nav3recipes.com/home"
   }

   @Serializable
   data class UserKey(val id: Int) : NavKey {
       companion object : DeepLinkKey {
           override val uriPattern: String = "https://www.nav3recipes.com/user/{id}"
       }
   }
   ```

2. **`UriDeepLinkMatcher` Registration**:
   `UriDeepLinkMatcher` instances are created using `Key.uriPattern` and `serializer<T>()`:
   ```kotlin
   private val deepLinkMatchers = listOf(
       UriDeepLinkMatcher(HomeKey.uriPattern.toUri(), serializer<HomeKey>()),
       UriDeepLinkMatcher(UserKey.uriPattern.toUri(), serializer<UserKey>()),
       UriDeepLinkMatcher(ProductKey.uriPattern.toUri(), serializer<ProductKey>()),
   )
   ```

3. **`bestMatch` Resolution**:
   Matches are evaluated using `matches.maxOrNull()`, which selects the most specific match based on exact path segments, query parameters, and scheme/host.
