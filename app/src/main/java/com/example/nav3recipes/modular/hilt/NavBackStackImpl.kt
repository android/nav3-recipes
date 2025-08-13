package com.example.nav3recipes.modular.hilt

import com.example.nav3recipes.navigator.NavBackStack
import com.example.nav3recipes.navigator.Route

internal class NavBackStackImpl(
    private val topLevelBackStack: TopLevelBackStack<Route>
) : NavBackStack {
    override fun add(route: Route) {
        topLevelBackStack.add(route)
    }

    override fun pop() {
        topLevelBackStack.removeLast()
    }

    override val size: Int
        get() = topLevelBackStack.backStack.size
}