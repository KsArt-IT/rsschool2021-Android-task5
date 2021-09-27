package ru.ksart.thecat.ui.list.adapter.breed

import androidx.recyclerview.widget.DiffUtil
import ru.ksart.thecat.model.data.Breed

class BreedDiffCallback : DiffUtil.ItemCallback<Breed>() {
    override fun areItemsTheSame(oldItem: Breed, newItem: Breed): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Breed, newItem: Breed): Boolean {
        return oldItem == newItem
    }
}

