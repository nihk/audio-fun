package com.audio.app

import android.app.Application
import com.audio.app.di.AppGraph

class App : Application(), AppGraph.Holder {
    override val appGraph: AppGraph by lazy {
        AppGraph(this)
    }
}
