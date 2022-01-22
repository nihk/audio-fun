package com.audio.recordings.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.audio.recordings.data.Event
import com.audio.recordings.data.Recording
import com.audio.recordings.databinding.RecordingItemBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

internal class RecordingsAdapter : ListAdapter<Recording, RecordingViewHolder>(RecordingItemCallback()) {
    private val events = MutableSharedFlow<Event>(extraBufferCapacity = 1)
    fun events(): Flow<Event> = events

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordingViewHolder {
        return LayoutInflater.from(parent.context)
            .let { inflater -> RecordingItemBinding.inflate(inflater, parent, false) }
            .let { binding -> RecordingViewHolder(binding, events::tryEmit) }
    }

    override fun onBindViewHolder(holder: RecordingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

internal class RecordingViewHolder(
    private val binding: RecordingItemBinding,
    private val onEvent: (Event) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(recording: Recording) {
        binding.name.text = recording.name
        binding.delete.setOnClickListener { onEvent(Event.DeleteRecordingEvent(recording)) }
        binding.root.setOnClickListener { onEvent(Event.ToPlaybackEvent(recording)) }
    }
}

internal class RecordingItemCallback : DiffUtil.ItemCallback<Recording>() {
    override fun areItemsTheSame(oldItem: Recording, newItem: Recording): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: Recording, newItem: Recording): Boolean {
        return oldItem == newItem
    }
}
