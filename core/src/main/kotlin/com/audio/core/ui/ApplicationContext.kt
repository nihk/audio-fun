package com.audio.core.ui

import android.content.Context
import android.content.ContextWrapper

class ApplicationContext(context: Context) : ContextWrapper(context.applicationContext)
