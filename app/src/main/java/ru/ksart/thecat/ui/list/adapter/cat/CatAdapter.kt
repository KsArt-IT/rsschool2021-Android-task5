package ru.ksart.thecat.ui.list.adapter.cat

import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import ru.ksart.thecat.model.data.CatResponse

class CatAdapter(
    private val onClick: (CatResponse) -> Unit
) : PagingDataAdapter<CatResponse, CatViewHolder>(CatDiffCallback()) {

    override fun onBindViewHolder(holder: CatViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CatViewHolder {
        return CatViewHolder.create(parent, onClick)
    }
}
