package ru.ksart.thecat.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import ru.ksart.thecat.databinding.FragmentCatBinding
import ru.ksart.thecat.model.data.CatResponse
import ru.ksart.thecat.ui.list.adapter.CatAdapter
import ru.ksart.thecat.ui.list.adapter.CatLoadStateAdapter
import ru.ksart.thecat.utils.DebugHelper

@AndroidEntryPoint
class CatFragment : Fragment() {

    private var binding: FragmentCatBinding? = null

    private val viewModel by viewModels<CatViewModel>()

    private val catAdapter
        get() = requireNotNull(views { recyclerView.adapter as? CatAdapter }) { "adapter not initialized" }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentCatBinding.inflate(inflater).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initList()
        bindList()
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun initList() {
        DebugHelper.log("CatFragment|initList")
        views {
            recyclerView.run {
                adapter = CatAdapter(::showDetail)
                    .apply {
                        withLoadStateHeaderAndFooter(
                            header = CatLoadStateAdapter { retry() },
                            footer = CatLoadStateAdapter { retry() }
                        )
                    }
                // задан в разметке
//                layoutManager = LinearLayoutManager(requireContext().applicationContext)
//                setHasFixedSize(true)
//                isNestedScrollingEnabled = false
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                        DebugHelper.log("CatFragment|recyclerView-onScrolled")
                        if (dy != 0) viewModel.accept(UiAction.Scroll())
                    }
                })
            }
            viewModel.loadState(catAdapter.loadStateFlow)
        }
    }

    private fun bindList() {
        DebugHelper.log("CatFragment|bindList")
        lifecycleScope.launchWhenStarted {
            viewModel.stateListData.collectLatest { (shouldScroll, pagingData) ->
                showList(pagingData)
                // Scroll only after the data has been submitted to the adapter,
                // and is a fresh search
                if (shouldScroll) views { recyclerView.scrollToPosition(0) }
            }
        }
    }

    private suspend fun showList(pagingData: PagingData<CatResponse>) {
        catAdapter.submitData(pagingData)
        DebugHelper.log("CatFragment|showList list=${catAdapter.itemCount}")
        views { emptyList.isVisible = catAdapter.itemCount == 0 }
    }

    private fun showDetail(item: CatResponse, imageView: ImageView) {
        DebugHelper.log("CatFragment|showDetail")
        val extras = FragmentNavigatorExtras(
            imageView to item.id
        )
        val action = CatFragmentDirections.actionCatFragmentToCatDetailFragment(item)
        findNavController().navigate(action, extras)
    }

    private fun <T> views(block: FragmentCatBinding.() -> T): T? = binding?.block()
}
