package com.example.nav3recipes.navigator.basic

import org.junit.Test
import kotlin.test.assertEquals

class NavigatorTest {

    private data object A : RouteV2(isTopLevel = true)

    private data object A1 : RouteV2()
    private data object B : RouteV2(isTopLevel = true)
    private data object B1 : RouteV2()
    private data object C : RouteV2(isTopLevel = true)
    private data object D : RouteV2(isShared = true)

    @Test
    fun backStackContainsOnlyStartRoute(){
        val navigator = Navigator<RouteV2>(startRoute = A)
        assertEquals(listOf<RouteV2>(A), navigator.backStack)
    }

    @Test
    fun navigatingToTopLevelRoute_addsRouteToTopOfStack(){
        val navigator = Navigator<RouteV2>(startRoute = A)
        navigator.navigate(B)
        assertEquals(listOf(A, B), navigator.backStack)
    }

    @Test
    fun navigatingToChildRoute_addsToCurrentTopLevelStack() {
        val navigator = Navigator<RouteV2>(startRoute = A)
        navigator.navigate(B)
        navigator.navigate(B1)
        assertEquals(listOf(A, B, B1), navigator.backStack)
    }

    @Test
    fun navigatingToNewTopLevelRoute_popsOtherStacksExceptStartStack() {
        val navigator = Navigator<RouteV2>(startRoute = A)
        navigator.navigate(A1) // [A, A1]
        navigator.navigate(C) // [A, A1, C]
        navigator.navigate(B) // [A, A1, B]
        val expected = listOf(A, A1, B)
        assertEquals(expected, navigator.backStack)
    }

    @Test
    fun navigatingToSharedRoute_whenItsAlreadyOnStack_movesItToNewStack() {
        val navigator = Navigator<RouteV2>(startRoute = A)
        navigator.navigate(D) // [A, D]
        navigator.navigate(C) // [A, D, C]
        navigator.navigate(D) // [A, C, D]
        val expected = listOf(A, C, D)
        assertEquals(expected, navigator.backStack)
    }

    @Test
    fun navigatingToStartRoute_whenOtherRoutesAreOnStack_popsAllOtherRoutes() {
        val navigator = Navigator<RouteV2>(startRoute = A)
        navigator.navigate(B) // [A, B]
        navigator.navigate(C) // [A, B, C]
        navigator.navigate(A) // [A]
        val expected : List<RouteV2> = listOf(A)
        assertEquals(expected, navigator.backStack)
    }

    @Test
    fun navigatingToStartRoute_whenItHasSubRoutes_retainsSubRoutes() {
        val navigator = Navigator<RouteV2>(startRoute = A)
        navigator.navigate(A1) // [A, A1]
        navigator.navigate(B) // [A, A1, B]
        navigator.navigate(A) // [A, A1]
        val expected : List<RouteV2> = listOf(A, A1)
        assertEquals(expected, navigator.backStack)
    }

    @Test
    fun repeatedlyNavigatingToTopLevelRoute_retainsSubRoutes(){
        val navigator = Navigator<RouteV2>(startRoute = A)
        navigator.navigate(B)
        navigator.navigate(B1)
        navigator.navigate(B)

        val expected = listOf(A, B, B1)
        assertEquals(expected, navigator.backStack)
    }

    @Test
    fun navigatingToTopLevelRoute_whenTopLevelRoutesCanExistTogether_retainsSubRoutes(){
        val navigator = Navigator<RouteV2>(startRoute = A, canTopLevelRoutesExistTogether = true)
        navigator.navigate(A)
        navigator.navigate(A1)
        navigator.navigate(B)
        navigator.navigate(B1)
        navigator.navigate(C)
        navigator.navigate(B)

        val expected = listOf(A, A1, C, B, B1)
        assertEquals(expected, navigator.backStack)
    }

    @Test
    fun navigatingBack_isChronological(){
        val navigator = Navigator<RouteV2>(startRoute = A)
        navigator.navigate(A1)
        navigator.navigate(B)
        navigator.navigate(B1)
        assertEquals(listOf(A, A1, B, B1), navigator.backStack)
        navigator.goBack()
        assertEquals(listOf(A, A1, B), navigator.backStack)
        navigator.goBack()
        assertEquals(listOf(A, A1), navigator.backStack)
        navigator.goBack()
        assertEquals(listOf<RouteV2>(A), navigator.backStack)

    }
}