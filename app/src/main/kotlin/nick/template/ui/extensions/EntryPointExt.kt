package nick.template.ui.extensions

import android.app.Activity
import androidx.fragment.app.Fragment
import dagger.hilt.android.EntryPointAccessors

inline fun <reified T> Activity.entryPoint(): T {
    return EntryPointAccessors.fromActivity(this, T::class.java)
}

inline fun <reified T> Fragment.entryPoint(): T {
    return EntryPointAccessors.fromFragment(this, T::class.java)
}
