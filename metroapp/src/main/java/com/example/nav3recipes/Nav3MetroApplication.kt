package com.example.nav3recipes

import android.app.Application
import com.example.nav3recipes.modular.metro.MetroGraph
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.android.MetroAppComponentProviders
import dev.zacsweers.metrox.android.MetroApplication

class Nav3MetroApplication : Application(), MetroApplication {
    override val appComponentProviders: MetroAppComponentProviders by lazy { createGraphFactory<MetroGraph.Factory>().create() }
}