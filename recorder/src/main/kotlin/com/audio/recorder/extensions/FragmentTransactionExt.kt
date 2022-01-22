package com.audio.recorder.extensions

import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentTransaction

internal inline fun <reified F : DialogFragment> FragmentTransaction.add(
    tag: String? = null,
    args: Bundle? = null
): FragmentTransaction = add(F::class.java, args, tag)
