package ru.ksart.thecat.ui.list.adapter.breed

import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import ru.ksart.thecat.model.data.BreedResponse
import ru.ksart.thecat.ui.list.UiAction

class BreedAdapter(
    private val onClick: (UiAction.Search) -> Unit
) : ListAdapter<BreedResponse, BreedViewHolder>(BreedDiffCallback()) {

    override fun onBindViewHolder(holder: BreedViewHolder, position: Int) {
        getItem(position)?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BreedViewHolder {
        return BreedViewHolder.create(parent, onClick)
    }
}
