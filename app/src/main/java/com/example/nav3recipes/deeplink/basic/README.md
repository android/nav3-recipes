# Deep Link Basic Recipe

This recipe demonstrates how to parse a deep link URL from an Android Intent into a Navigation key.

## How it works

It consists of two activities - `CreateDeepLinkActivity` to construct and trigger the deeplink request, and the `MainActivity` to show how an app can handle that request.

## Demonstrated forms of deeplink

The `MainActivity` has several backStack keys to demonstrate different types of supported deeplinks:
1. `HomeKey` - deeplink with an exact url (no deeplink arguments)
2. `UsersKey` - deeplink with path arguments
3. `SearchKey` - deeplink with query arguments

See `MainActivity.deepLinkMatchers` for the actual url pattern of each.

## Recipe structure

This recipe consists of three main packages:
1. `basic.deeplink` - Contains the two activities
2. `basic.deeplink.ui` - Contains the activity UI code, i.e. global string variables, deeplink URLs etc
3. `basic.deeplink.util` - Contains the classes and helper methods to parse and match the deeplinks
