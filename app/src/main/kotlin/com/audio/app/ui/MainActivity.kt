package com.audio.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.audio.app.di.MainEntryPoint
import com.audio.core.di.entryPoint
import com.audio.databinding.MainActivityBinding
import com.audio.recordings.ui.RecordingsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = entryPoint<MainEntryPoint>().fragmentFactory
        super.onCreate(savedInstanceState)
        val binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (supportFragmentManager.findFragmentById(binding.fragmentContainer.id) == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                replace<RecordingsFragment>(binding.fragmentContainer.id)
            }
        }
    }
}
