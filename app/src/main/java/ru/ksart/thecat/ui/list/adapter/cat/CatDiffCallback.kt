package ru.ksart.thecat.ui.list.adapter.cat

import androidx.recyclerview.widget.DiffUtil
import ru.ksart.thecat.model.data.CatResponse

class CatDiffCallback : DiffUtil.ItemCallback<CatResponse>() {
    override fun areItemsTheSame(oldItem: CatResponse, newItem: CatResponse): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CatResponse, newItem: CatResponse): Boolean {
        return oldItem == newItem
    }
}
