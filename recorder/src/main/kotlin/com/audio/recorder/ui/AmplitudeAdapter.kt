package com.audio.recorder.ui

import android.annotation.SuppressLint
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.audio.recorder.databinding.AmplitudeItemBinding
import kotlin.math.roundToInt

internal class AmplitudeAdapter : RecyclerView.Adapter<AmplitudeViewHolder>() {
    private val amplitudes = mutableListOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmplitudeViewHolder {
        return LayoutInflater.from(parent.context)
            .let { inflater -> AmplitudeItemBinding.inflate(inflater, parent, false) }
            .let { binding -> AmplitudeViewHolder(binding) }
    }

    override fun onBindViewHolder(holder: AmplitudeViewHolder, position: Int) {
        holder.bind(amplitudes[position])
    }

    override fun getItemCount(): Int = amplitudes.size

    @SuppressLint("NotifyDataSetChanged")
    fun update(newList: List<Int>) {
        if (amplitudes == newList) return

        if (amplitudes.isEmpty() && newList.isNotEmpty()) {
            amplitudes.addAll(newList)
            notifyDataSetChanged()
        } else {
            val numItemsInserted = newList.size - amplitudes.size
            val currentLastIndex = amplitudes.size
            amplitudes.apply {
                clear()
                addAll(newList)
            }
            notifyItemRangeInserted(currentLastIndex, numItemsInserted)
        }
    }
}

class AmplitudeViewHolder(val binding: AmplitudeItemBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(amplitude: Int) {
        binding.bar.scaleY = 1f
        binding.bar.updateLayoutParams {
            height = 1.dp().roundToInt()
        }
        if (amplitude > 0) {
            binding.bar.animate().scaleY(-amplitude.dp())
        }
    }

    private fun Int.dp(): Float {
        return this.toFloat() * binding.root.context.resources.displayMetrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT
    }
}
