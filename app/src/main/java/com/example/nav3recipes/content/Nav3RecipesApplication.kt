package com.example.nav3recipes.content

import com.example.nav3recipes.modular.metro.AppGraph
import com.google.android.play.core.splitcompat.SplitCompatApplication
import dagger.hilt.android.HiltAndroidApp
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.android.MetroAppComponentProviders
import dev.zacsweers.metrox.android.MetroApplication

@HiltAndroidApp
class Nav3RecipesApplication : SplitCompatApplication(), MetroApplication {
    override val appComponentProviders: MetroAppComponentProviders by lazy { createGraphFactory<AppGraph.Factory>().create(this) }
}
