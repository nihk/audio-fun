package com.audio.recorder.ui

import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.audio.recorder.databinding.AmplitudeItemBinding
import kotlin.math.roundToInt

class AmplitudeAdapter : ListAdapter<Int, AmplitudeViewHolder>(AmplitudeDiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmplitudeViewHolder {
        return LayoutInflater.from(parent.context)
            .let { inflater -> AmplitudeItemBinding.inflate(inflater, parent, false) }
            .let { binding -> AmplitudeViewHolder(binding) }
    }

    override fun onBindViewHolder(holder: AmplitudeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class AmplitudeViewHolder(val binding: AmplitudeItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(amplitude: Int) {
        binding.bar.updateLayoutParams {
            height = amplitude.dp()
        }
    }

    private fun Int.dp(): Int {
        return (this.toFloat() * binding.root.context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT).roundToInt()
    }
}

object AmplitudeDiffCallback : DiffUtil.ItemCallback<Int>() {
    override fun areItemsTheSame(oldItem: Int, newItem: Int): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Int, newItem: Int): Boolean {
        return oldItem == newItem
    }
}
