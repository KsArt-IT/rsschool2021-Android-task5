package ru.ksart.thecat.ui.list.adapter

import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import ru.ksart.thecat.model.data.CatResponse

class CatAdapter(
    private val onClick: (CatResponse, ImageView) -> Unit
): PagingDataAdapter<CatResponse, CatViewHolder>(CatDiffCallback()) {

    override fun onBindViewHolder(holder: CatViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatViewHolder {
        return CatViewHolder.create(parent, onClick)
    }
}
