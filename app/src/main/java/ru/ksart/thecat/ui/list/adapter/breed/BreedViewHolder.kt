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
                item?.let { breed ->
                    DebugHelper.log("BreedViewHolder|onClick id=${breed.id}")
                    card.isChecked = card.isChecked.not()
                    onClick(UiAction.Search(breedQuery = breed.id))
                }
            }
/*
            binding.root.setOnClickListener {
                DebugHelper.log("BreedViewHolder|onClick id=${item?.id}")
                item?.let { onClick(UiAction.Search(breedQuery = it.id)) }
            }
*/
        }
    }

    fun bind(item: Breed) {
        this.item = item

        binding.run {
            nameBreed.text = item.name
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
