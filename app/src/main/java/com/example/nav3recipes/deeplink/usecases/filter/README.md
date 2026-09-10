# Composite DeepLinkMatcher.Filter Recipe

This recipe demonstrates how to combine multiple `DeepLinkMatcher.Filter` instances using infix functions (`and`, `or`) to create composite filtering logic in Navigation 3.

## How it works

`DeepLinkMatcher` natively evaluates a list of filters using implicit logical AND (`filters.all { it.filterRequest(request) }`). By defining infix operator functions (`and`, `or`), developers can construct flexible boolean expressions combining intent actions, MIME types, or custom criteria.

This recipe consists of two activities:
- `CompositeFilterDeepLinkActivity`: An interactive playground allowing you to configure the intent's action and MIME type, preview whether the composite filter will match, and launch the deep link request.
- `MainActivity`: Constructs a `DeepLinkRequest(intent)`, matches it using a `UriDeepLinkMatcher` configured with a composite filter (`actionFilter(ACTION_VIEW) and (mimeTypeFilter("image/png") or mimeTypeFilter("image/jpeg"))`), and navigates to either `ViewerKey` or `FallbackKey`.

## Key Concepts

1. **Infix `and` Operator**:
   Combines two filters using logical AND with short-circuiting:
   ```kotlin
   infix fun Filter.and(other: Filter): Filter = Filter { request ->
       filterRequest(request) && other.filterRequest(request)
   }
   ```

2. **Infix `or` Operator**:
   Combines two filters using logical OR with short-circuiting:
   ```kotlin
   infix fun Filter.or(other: Filter): Filter = Filter { request ->
       filterRequest(request) || other.filterRequest(request)
   }
   ```

3. **Operator `!` (NOT)**:
   Inverts the result of a filter using logical NOT:
   ```kotlin
   operator fun Filter.not(): Filter = Filter { request ->
       !filterRequest(request)
   }
   ```

4. **Composite Filter Expressions**:
   Operators allow expressive and readable composition:
   ```kotlin
   val imageMimeTypeFilter = DeepLinkMatcher.mimeTypeFilter("image/png") or
       DeepLinkMatcher.mimeTypeFilter("image/jpeg")
   val compositeFilter = DeepLinkMatcher.actionFilter(Intent.ACTION_VIEW) and
       imageMimeTypeFilter and !DeepLinkMatcher.mimeTypeFilter("application/pdf")

   val matcher = UriDeepLinkMatcher(
       uriPattern = VIEWER_URI_PATTERN.toUri(),
       serializer = serializer<ViewerKey>(),
       filters = listOf(compositeFilter)
   )
   ```
