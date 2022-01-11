package nick.template.ui.extensions

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction

// This should only be used for adding DialogFragments.
inline fun <reified F : Fragment> FragmentTransaction.add(
    tag: String? = null,
    args: Bundle? = null
): FragmentTransaction = add(F::class.java, args, tag)
