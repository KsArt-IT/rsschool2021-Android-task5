package ru.ksart.thecat.ui.list.adapter.breed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.ksart.thecat.databinding.ItemBreedBinding
import ru.ksart.thecat.model.data.BreedResponse
import ru.ksart.thecat.ui.list.UiAction
import timber.log.Timber

class BreedViewHolder(
    private val binding: ItemBreedBinding,
    onClick: (UiAction.Search) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    var item: BreedResponse? = null
        private set

    init {
        binding.run {
            card.setOnClickListener {
                item?.takeIf { it.selected.not() }?.let { breed ->
                    Timber.d("onClick id=${breed.id}")
                    breed.selected = true
                    onClick(UiAction.Search(breedQuery = breed.id))
                }
            }
        }
    }

    fun bind(item: BreedResponse) {
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
