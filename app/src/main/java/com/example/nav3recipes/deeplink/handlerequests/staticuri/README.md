# Deep Link Static URI Recipe

This recipe demonstrates how deep link with a static Uri.

## Recipe components

The recipe contains two activities:
1. `StaticUriDeepLinkActivity` to construct and start an Intent with the deep link uri 
2. `MainActivity` is the target Activity of the deep link, represents an app that users can deep link to.

## How the demonstrated deep link works

1. The deep link source (`StaticUriDeepLinkActivity`) defines the uri and creates an Intent to deep link with.
2. The app (`MainActivity`) declares a navigation key (`HomeKey`). To indicate that `HomeKey` supports deep linking, the app declares a `UriDeepLinkMatcher` with the `HomeKey` serializer along with the uri pattern that `HomeKey` supports.
3. `MainActivity` onCreate instantiates a `DeepLinkRequest` with the intent and matches it with the `UriDeepLinkMatcher` to get a `MatchResult`. If the `MatchResult` is non-null, the app navigates to the key returned by the result. Otherwise, the deep link is not supported and the app navigates to a `Fallback` screen.