package ru.ksart.thecat.ui.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ru.ksart.thecat.R
import ru.ksart.thecat.databinding.ItemCatBinding
import ru.ksart.thecat.model.data.CatResponse

class CatViewHolder(
    private val binding: ItemCatBinding,
    onClick: (CatResponse, ImageView) -> Unit
): RecyclerView.ViewHolder(binding.root) {

    var item: CatResponse? = null
    private set

    init {
        binding.root.setOnClickListener {
            item?.let { onClick(it, binding.image) }
        }
    }

    fun bind(item: CatResponse) {
        this.item = item

        binding.run {
            image.apply { transitionName = item.id }
                .load(item.url) {
                    crossfade(true)
                    placeholder(R.drawable.ic_download)
                    error(R.drawable.ic_error)
//                    transformations(CircleCropTransformation())
                    build()
                }
        }
    }

    companion object {
        fun create(
            parent: ViewGroup,
            onClick: (CatResponse, ImageView) -> Unit
        ) = ItemCatBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ).let { CatViewHolder(it, onClick) }
    }
}
