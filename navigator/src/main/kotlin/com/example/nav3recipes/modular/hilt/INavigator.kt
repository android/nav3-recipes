package com.example.nav3recipes.modular.hilt

interface INavigator {
    fun goTo(destination: Any)
    fun goBack()
}