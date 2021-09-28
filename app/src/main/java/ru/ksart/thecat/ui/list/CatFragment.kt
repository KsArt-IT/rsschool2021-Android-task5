package ru.ksart.thecat.ui.list

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import okhttp3.internal.notifyAll
import ru.ksart.thecat.R
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

    private var _catAdapter: CatAdapter? = null
    private val catAdapter get() = requireNotNull(_catAdapter) { "Cat adapter is not initialized" }

    private val breedAdapter
        get() = requireNotNull(views { breedList.adapter as? BreedAdapter }) { "Breed adapter is not initialized" }

    // flip animation+Navigation
    private val navOptions by lazy {
        NavOptions.Builder()
            .setEnterAnim(R.animator.card_flip_right_in)
            .setExitAnim(R.animator.card_flip_right_out)
            .setPopEnterAnim(R.animator.card_flip_left_in)
            .setPopExitAnim(R.animator.card_flip_left_out)
            .build()
    }

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
    }

    override fun onDestroyView() {
        binding = null
        _catAdapter = null
        super.onDestroyView()
    }

    private fun initBreedList() {
        DebugHelper.log("CatFragment|initBreedList")
        views {
            breedList.run {
                adapter = BreedAdapter(viewModel::accept.invoke())
                // тут нельзя это использовать
//                setHasFixedSize(true)
                isNestedScrollingEnabled = false
            }
        }
    }

    private fun bindBreedList() {
        lifecycleScope.launchWhenStarted { viewModel.breedList.collectLatest(::showBreedList) }
        lifecycleScope.launchWhenStarted {
            viewModel.breedSelected.collectLatest { (newId, oldId) ->
                views { catList.scrollToPosition(0) }
                changeBreedSelected(newId, true)
                changeBreedSelected(oldId, false)
            }
        }
    }

    private fun changeBreedSelected(id: String, checked: Boolean) {
        breedAdapter.currentList.firstOrNull {
            it.id == id
        }?.apply {
            selected = checked
        }?.let {
            breedAdapter.currentList.indexOf(it).also { index ->
                DebugHelper.log("CatFragment|breed selected = $id id=$index")
            }.let(breedAdapter::notifyItemChanged)
        }
    }

    private fun showBreedList(list: List<Breed>) {
        DebugHelper.log("CatFragment|showBreedList list=${list.size}")
        breedAdapter.submitList(list)
    }

    private fun initCatList() {
        DebugHelper.log("CatFragment|initCatList")
        _catAdapter = CatAdapter(::showCatDetail)
        views {
            catList.adapter = catAdapter.withLoadStateHeaderAndFooter(
                header = CatLoadStateAdapter { catAdapter.retry() },
                footer = CatLoadStateAdapter { catAdapter.retry() }
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
        }
        viewModel.loadState(catAdapter.loadStateFlow)
    }

    private fun bindCatList() {
        DebugHelper.log("CatFragment|bindCatList")
        lifecycleScope.launchWhenStarted {
            viewModel.stateCatListData.collectLatest { (shouldScroll, pagingData) ->
                DebugHelper.log("CatFragment|showCatList")
                catAdapter.submitData(pagingData)
                // выполниться только последнее действие
                DebugHelper.log("CatFragment|showCatList list=${catAdapter.itemCount}")
                // Scroll only after the data has been submitted to the adapter,
                // and is a fresh search
                if (shouldScroll) {
                    DebugHelper.log("CatFragment|shouldScroll")
                    views { catList.scrollToPosition(0) }
                }
            }
        }
    }

    private fun showCatDetail(item: CatResponse) {
        DebugHelper.log("CatFragment|showCatDetail")
        val action = CatFragmentDirections.actionCatFragmentToCatDetailFragment(item)
        findNavController().navigate(action, navOptions)
    }

    private fun <T> views(block: FragmentCatBinding.() -> T): T? = binding?.block()
}
