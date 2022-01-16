package com.audio.recordings.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.audio.recordings.data.Recording
import com.audio.recordings.databinding.RecordingItemBinding

class RecordingsAdapter : ListAdapter<Recording, RecordingViewHolder>(RecordingItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingViewHolder {
        return LayoutInflater.from(parent.context)
            .let { inflater -> RecordingItemBinding.inflate(inflater, parent, false) }
            .let { binding -> RecordingViewHolder(binding) }
    }

    override fun onBindViewHolder(holder: RecordingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class RecordingViewHolder(
    private val binding: RecordingItemBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(recording: Recording) {
        binding.name.text = recording.name
    }
}

class RecordingItemCallback : DiffUtil.ItemCallback<Recording>() {
    override fun areItemsTheSame(oldItem: Recording, newItem: Recording): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Recording, newItem: Recording): Boolean {
        return oldItem == newItem
    }
}
