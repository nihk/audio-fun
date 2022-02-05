package com.audio.core.ui

import android.os.Bundle
import androidx.fragment.app.Fragment

data class NavigationDirections(
    val screen: Class<out Fragment>,
    val arguments: Bundle? = null
)
