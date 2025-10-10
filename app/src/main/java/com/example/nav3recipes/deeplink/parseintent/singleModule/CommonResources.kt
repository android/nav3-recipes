package com.example.nav3recipes.deeplink.parseintent.singleModule

/**
 * String resources
 */
internal const val STRING_LITERAL_FILTER = "filter"
internal const val STRING_LITERAL_HOME = "home"
internal const val STRING_LITERAL_USERS = "users"
internal const val STRING_LITERAL_SEARCH = "search"
internal const val STRING_LITERAL_INCLUDE = "include"
internal const val PATH_BASE = "https://www.nav3recipes.com"
internal const val PATH_INCLUDE = "$STRING_LITERAL_USERS/$STRING_LITERAL_INCLUDE"
internal const val PATH_SEARCH = "$STRING_LITERAL_USERS/$STRING_LITERAL_SEARCH"
internal const val URL_HOME_EXACT = "$PATH_BASE/$STRING_LITERAL_HOME"

internal const val URL_USERS_WITH_FILTER = "$PATH_BASE/$PATH_INCLUDE/{$STRING_LITERAL_FILTER}"
internal val URL_SEARCH = "$PATH_BASE/$PATH_SEARCH" +
        "?${SearchKey::ageMin.name}={${SearchKey::ageMin.name}}" +
        "&${SearchKey::ageMax.name}={${SearchKey::ageMax.name}}" +
        "&${SearchKey::firstName.name}={${SearchKey::firstName.name}}" +
        "&${SearchKey::location.name}={${SearchKey::location.name}}"

/**
 * User data
 */
internal const val FIRST_NAME_JOHN = "John"
internal const val FIRST_NAME_TOM = "Tom"
internal const val FIRST_NAME_MARY = "Mary"
internal const val FIRST_NAME_JULIE = "Julie"
internal const val LOCATION_CA = "CA"
internal const val LOCATION_BC = "BC"
internal const val LOCATION_BR = "BR"
internal const val LOCATION_US = "US"
internal const val EMPTY = ""
internal val LIST_USERS = listOf(
    User(FIRST_NAME_JOHN, 15, LOCATION_CA),
    User(FIRST_NAME_JOHN, 22, LOCATION_BC),
    User(FIRST_NAME_TOM, 25, LOCATION_CA),
    User(FIRST_NAME_TOM, 68, LOCATION_BR),
    User(FIRST_NAME_JULIE, 48, LOCATION_BR),
    User(FIRST_NAME_JULIE, 33, LOCATION_US),
    User(FIRST_NAME_JULIE, 9, LOCATION_BR),
    User(FIRST_NAME_MARY, 64, LOCATION_US),
    User(FIRST_NAME_MARY, 5, LOCATION_CA),
    User(FIRST_NAME_MARY, 52, LOCATION_BC),
    User(FIRST_NAME_TOM, 94, LOCATION_BR),
    User(FIRST_NAME_JULIE, 46, LOCATION_CA),
    User(FIRST_NAME_JULIE, 37, LOCATION_BC),
    User(FIRST_NAME_JULIE, 73 ,LOCATION_US),
    User(FIRST_NAME_MARY, 51, LOCATION_US),
    User(FIRST_NAME_MARY, 63, LOCATION_BR),
)
