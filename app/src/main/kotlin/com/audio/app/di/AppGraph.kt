package com.audio.app.di

import android.app.Application
import com.audio.core.di.CoreGraph
import com.audio.core.ui.ApplicationContext
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

class AppGraph(application: Application) {
    val coreGraph: CoreGraph = CoreGraph(application)

    interface Holder {
        val appGraph: AppGraph
    }
}
