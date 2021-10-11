package ru.ksart.thecat.ui.list.adapter.breed

import androidx.recyclerview.widget.DiffUtil
import ru.ksart.thecat.model.data.BreedResponse

class BreedDiffCallback : DiffUtil.ItemCallback<BreedResponse>() {
    override fun areItemsTheSame(oldItem: BreedResponse, newItem: BreedResponse): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: BreedResponse, newItem: BreedResponse): Boolean {
        return oldItem == newItem
    }
}
