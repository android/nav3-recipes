package com.example.nav3recipes.modular.metro

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.android.MetroAppComponentProviders
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

@DependencyGraph(AppScope::class, [ActivityScope::class]) interface MetroGraph : MetroAppComponentProviders, ViewModelGraph {

    @DependencyGraph.Factory
    interface Factory {
        fun create(): MetroGraph
    }
}

abstract class ActivityScope private constructor()