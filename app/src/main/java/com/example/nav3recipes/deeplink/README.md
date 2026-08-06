# Deep Link Recipes

This module contains the main recipes for deep linking with Navigation3.

## Recipe structure

The deep link module consists of two main packages:

### 1. usecases
Shows common cases for customizing deep link components
- `matcher` - a custom `DeepLinkMatcher` to parse deep links from `DeepLinkRequest` extras
- `colocatedkey` - colocating deep link URI patterns directly with navigation keys using a `DeepLinkKey` interface

### 2. handlerequests
Shows how to handle different types of deep link requests
- `staticuri` - handles deep links with a static Uri using `UriDeepLinkMatcher`.
- `uriwitharguments` - handles deep link with Uri arguments using `UriDeepLinkMatcher`
- `syntheticbackstack` - deep link between apps with a synthetic back stack using `DeepLinkMatcher.withBackStack` and correct "Up" navigation behavior
