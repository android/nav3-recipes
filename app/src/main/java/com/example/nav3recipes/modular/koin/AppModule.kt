package com.example.nav3recipes.modular.koin

import org.koin.androidx.scope.dsl.activityRetainedScope
import org.koin.dsl.module

val appModule = module {
    includes(profileModule,conversationModule)

    activityRetainedScope {
        scoped {
            Navigator(startDestination = ConversationList)
        }
    }
}