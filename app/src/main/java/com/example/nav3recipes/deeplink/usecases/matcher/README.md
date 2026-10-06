# Custom DeepLinkMatcher Recipe (Hilt Multi-Module Injection)

This recipe demonstrates how feature modules independently contribute custom `DeepLinkMatcher` implementations into the main app using Dagger Hilt multibindings (`@IntoSet`).

## How it works

The architecture is divided into decoupled components:

- **Feature Modules (`HomeModule`, `ProfileModule`)**:
  - Each module defines its own navigation key (`HomeKey`, `ProfileKey`) and a corresponding `RequestExtrasKey<String>`.
  - Each module provides its own `DeepLinkMatcher` instance into a multibound set (`Set<DeepLinkMatcher<NavKey, MatchResult<NavKey>>>`) via `@Provides @IntoSet`.
- **Main App (`MainActivity`)**:
  - Annotated with `@AndroidEntryPoint`.
  - Injects the set of matchers and resolves incoming `DeepLinkRequest` intents by iterating across the set until a match is found.
  - If no matcher matches the request, it routes to `FallbackKey`.
- **Launcher Sandbox (`CustomDeepLinkMatcherActivity`)**:
  - Allows constructing and launching deep links targeting by feature modules.

## Key Concepts

1. **Multibound Matchers via Dagger Hilt**:
   Feature modules contribute matchers to `ActivityComponent` using `@IntoSet`:
   ```kotlin
   @Module
   @InstallIn(ActivityComponent::class)
   object HomeMatcherModule {
       @Provides
       @IntoSet
       fun provideHomeMatcher(): DeepLinkMatcher<NavKey, DeepLinkMatcher.MatchResult<NavKey>> {
           return JsonDeepLinkMatcher(HomeDeepLinkKey, HomeKey.serializer())
       }
   }
   ```

2. **Decoupled Request Resolution**:
   `MainActivity` does not statically depend on each feature's matcher factory. It evaluates the injected set:
   ```kotlin
   val key = deepLinkMatchers.firstNotNullOfOrNull { matcher ->
       matcher.match(request)
   }?.key ?: FallbackKey
   ```

3. **Custom `DeepLinkMatcher`**:
   `JsonDeepLinkMatcher<T>` extends `DeepLinkMatcher<NavKey, MatchResult<NavKey>>` and deserializes JSON payloads from `DeepLinkRequest.extras` using Kotlinx Serialization.
