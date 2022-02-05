package com.audio.core.di

import android.app.Application
import com.audio.core.ui.ApplicationContext
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

class CoreGraph(application: Application) {
    val ioContext: CoroutineContext = Dispatchers.IO
    val appScope: CoroutineScope = GlobalScope
    val appContext: ApplicationContext = ApplicationContext(application)
}
