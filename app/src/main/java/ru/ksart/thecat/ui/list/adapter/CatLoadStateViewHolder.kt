package ru.ksart.thecat.ui.list.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.recyclerview.widget.RecyclerView
import ru.ksart.thecat.databinding.ItemLoadStateFooterBinding
import ru.ksart.thecat.utils.DebugHelper

class CatLoadStateViewHolder(
    private val binding: ItemLoadStateFooterBinding,
    retry: () -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    init {
        binding.retryButton.setOnClickListener { retry.invoke() }
    }

    fun bind(loadState: LoadState) {
        DebugHelper.log("------------------------------------------")
        DebugHelper.log("CatLoadStateViewHolder|bind")
        binding.run {
            if (loadState is LoadState.Error) {
                errorMessage.text = loadState.error.localizedMessage
            }
            progress.isVisible = loadState is LoadState.Loading
            retryButton.isVisible = loadState is LoadState.Error
            errorMessage.isVisible = loadState is LoadState.Error
        }
    }

    companion object {
        fun create(
            parent: ViewGroup,
            retry: () -> Unit
        ) = ItemLoadStateFooterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        ).let { CatLoadStateViewHolder(it, retry) }
    }
}