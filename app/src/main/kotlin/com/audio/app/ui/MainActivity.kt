package com.audio.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import com.audio.app.di.AppGraph
import com.audio.app.di.MainGraph
import com.audio.databinding.MainActivityBinding
import com.audio.recordings.ui.RecordingsDirections

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val mainGraph = MainGraph(
            appGraph = (application as AppGraph.Holder).appGraph,
            fragmentManager = supportFragmentManager
        )
        supportFragmentManager.fragmentFactory = mainGraph.fragmentFactory

        super.onCreate(savedInstanceState)
        val binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (supportFragmentManager.findFragmentById(binding.fragmentContainer.id) == null) {
            supportFragmentManager.commit {
                val directions = RecordingsDirections()
                replace(binding.fragmentContainer.id, directions.screen, directions.arguments)
            }
        }
    }
}
