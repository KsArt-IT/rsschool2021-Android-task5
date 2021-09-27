package ru.ksart.thecat.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.PagingData
import androidx.paging.filter
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withContext
import ru.ksart.thecat.databinding.FragmentCatBinding
import ru.ksart.thecat.model.data.Breed
import ru.ksart.thecat.model.data.CatResponse
import ru.ksart.thecat.ui.list.adapter.breed.BreedAdapter
import ru.ksart.thecat.ui.list.adapter.cat.CatAdapter
import ru.ksart.thecat.ui.list.adapter.cat.CatLoadStateAdapter
import ru.ksart.thecat.utils.DebugHelper

@AndroidEntryPoint
class CatFragment : Fragment() {

    private var binding: FragmentCatBinding? = null

    private val viewModel by viewModels<CatViewModel>()

    private val catAdapter
        //        get() = views { catList.adapter as? CatAdapter }
        get() = requireNotNull(views { catList.adapter as? CatAdapter }) { "Cat adapter is not initialized" }

    private val breedAdapter
        get() = requireNotNull(views { breedList.adapter as? BreedAdapter }) { "Breed adapter is not initialized" }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentCatBinding.inflate(inflater).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initCatList()
        bindCatList()

        initBreedList()
        bindBreedList()

        // для анимированного перехода
        postponeEnterTransition()
        (view.parent as? ViewGroup)?.doOnPreDraw { startPostponedEnterTransition() }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    private fun initBreedList() {
        DebugHelper.log("CatFragment|initBreedList")
        views {
            breedList.run {
                adapter = BreedAdapter { action ->
                    catList.scrollToPosition(0)
                    viewModel.accept(action)
                }
            }
        }
    }

    private fun bindBreedList() {
        lifecycleScope.launchWhenStarted { viewModel.breedList.collectLatest(::showBreedList) }
    }

    private fun showBreedList(list: List<Breed>) {
        DebugHelper.log("CatFragment|showBreedList list=${list.size}")
        breedAdapter.submitList(list)
    }

    private fun initCatList() {
        DebugHelper.log("CatFragment|initCatList")
        views {
            val adapterInit = CatAdapter(::showCatDetail)
            catList.adapter = CatAdapter(::showCatDetail)
            adapterInit.withLoadStateHeaderAndFooter(
                header = CatLoadStateAdapter { adapterInit.retry() },
                footer = CatLoadStateAdapter { adapterInit.retry() }
            )
            // задан в разметке
//                layoutManager = LinearLayoutManager(requireContext().applicationContext)
            catList.setHasFixedSize(true)
            catList.isNestedScrollingEnabled = false
            catList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                        DebugHelper.log("CatFragment|recyclerView-onScrolled")
                    if (dy != 0) viewModel.scroll()
                }
            })
            viewModel.loadState(adapterInit.loadStateFlow)
        }
    }

    private fun bindCatList() {
        DebugHelper.log("CatFragment|bindCatList")
        lifecycleScope.launchWhenStarted {
            viewModel.isEmptyList.collectLatest {
                views {
//                    emptyList.isVisible = catAdapter.itemCount == 0
                    DebugHelper.log("CatFragment|showCatList empty=${views { emptyList.isVisible }} list=${catAdapter.itemCount}")
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.stateCatListData.collectLatest { (shouldScroll, pagingData) ->
                DebugHelper.log("CatFragment|showCatList")
                viewModel.changeList()
                catAdapter.submitData(pagingData)
                // Scroll only after the data has been submitted to the adapter,
                // and is a fresh search
                if (shouldScroll) {
                    DebugHelper.log("CatFragment|shouldScroll")
                    views { catList.scrollToPosition(0) }
                }
            }
        }
    }

    private fun showCatDetail(item: CatResponse, imageView: ImageView) {
        DebugHelper.log("CatFragment|showCatDetail")
        val extras = FragmentNavigatorExtras(
            imageView to item.id
        )
        val action = CatFragmentDirections.actionCatFragmentToCatDetailFragment(item)
        findNavController().navigate(action, extras)
    }

    private fun <T> views(block: FragmentCatBinding.() -> T): T? = binding?.block()
}
