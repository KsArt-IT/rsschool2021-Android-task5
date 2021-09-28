package ru.ksart.thecat.ui.list.adapter.breed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ru.ksart.thecat.databinding.ItemBreedBinding
import ru.ksart.thecat.model.data.Breed
import ru.ksart.thecat.model.data.CatResponse
import ru.ksart.thecat.ui.list.UiAction
import ru.ksart.thecat.utils.DebugHelper

class BreedViewHolder(
    private val binding: ItemBreedBinding,
    onClick: (UiAction.Search) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    var item: Breed? = null
        private set

    init {
        binding.run {
            card.setOnClickListener {
                item?.takeIf { it.selected.not() }?.let { breed ->
                    DebugHelper.log("BreedViewHolder|onClick id=${breed.id}")
//                    breed.selected = true
//                    card.isChecked = breed.selected
                    onClick(UiAction.Search(breedQuery = breed.id))
                }
            }
        }
    }

    fun bind(item: Breed) {
        this.item = item

        binding.run {
            nameBreed.text = item.name
            card.isChecked = item.selected
        }
    }

    companion object {
        fun create(
            parent: ViewGroup,
            onClick: (UiAction.Search) -> Unit
        ) = ItemBreedBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ).let { BreedViewHolder(it, onClick) }
    }
}
