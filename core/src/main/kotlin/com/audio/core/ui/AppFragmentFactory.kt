package com.audio.core.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory

class AppFragmentFactory(
    private val fragments: Map<Class<out Fragment>, () -> Fragment>
) : FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        val fragmentClass: Class<out Fragment> = loadFragmentClass(classLoader, className)
        return fragments[fragmentClass]?.invoke() ?: super.instantiate(classLoader, className)
    }
}
