package ru.ksart.thecat.ui.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.ksart.thecat.R
import ru.ksart.thecat.databinding.FragmentCatBinding
import ru.ksart.thecat.model.data.Breed
import ru.ksart.thecat.model.data.CatResponse
import ru.ksart.thecat.ui.list.adapter.breed.BreedAdapter
import ru.ksart.thecat.ui.list.adapter.cat.CatAdapter
import ru.ksart.thecat.ui.list.adapter.cat.CatLoadStateAdapter
import ru.ksart.thecat.utils.DebugHelper
import timber.log.Timber

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
        Timber.d("onViewCreated")

        initCatList()
        bindCatList()

        initBreedList()
        bindBreedList()
    }

    override fun onDestroyView() {
        Timber.d("onDestroyView")
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
        // использование мульти flows
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.breedList.collectLatest(::showBreedList)
                }
                launch {
                    viewModel.state.collectLatest { state ->
                        if (state.hasNotScrolledForCurrentSearch) {
                            // прокручиваем
//                            views { catList.scrollToPosition(0) }
                            // меняем поиск
                            selectBreed(state.breedQuery, state.lastBreedQueryScrolled)
                        }
                    }
                }
            }
        }
    }

    private fun selectBreed(newId: String, oldId: String) {
        changeBreedSelected(newId, true)
        changeBreedSelected(oldId, false)
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
            catList.run {
                adapter = catAdapter.withLoadStateHeaderAndFooter(
                    header = CatLoadStateAdapter { catAdapter.retry() },
                    footer = CatLoadStateAdapter { catAdapter.retry() }
                )
                catAdapter.stateRestorationPolicy =
                    RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY

                // задан в разметке
//                layoutManager = LinearLayoutManager(requireContext().applicationContext)
                setHasFixedSize(true)
                isNestedScrollingEnabled = false
                // добавление разделителя
                val decoration =
                    DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
                addItemDecoration(decoration)

                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                        DebugHelper.log("CatFragment|recyclerView-onScrolled")
                        if (dy != 0) viewModel.scroll()
                    }
                })
            }
        }
        viewModel.loadState(catAdapter.loadStateFlow)
    }

    private fun bindCatList() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateCatListData
                    .collectLatest { (shouldScroll, pagingData) ->
                        Timber.d("submitData list in")
                        launch {
                            views { emptyListTextView.isVisible = false }
                            delay(5000)
                            Timber.d("-------------------------------------")
                            views {
                                emptyListTextView.isVisible = catAdapter.itemCount == 0
                                Timber.d("submitData list visible=${emptyListTextView.isVisible}")
                                Timber.d("submitData list =${catAdapter.itemCount}")
                            }
                        }
                        catAdapter.submitData(pagingData)
                        // выполниться только последнее действие
                        // Scroll only after the data has been submitted to the adapter,
                        // and is a fresh search
                        if (shouldScroll) {
                            Timber.d("submitData list scrollToPosition=0")
                            views { catList.scrollToPosition(0) }
                        }
                    }
            }
        }
    }

    private fun showCatDetail(item: CatResponse) {
        DebugHelper.log("CatFragment|showCatDetail")
        val action = CatFragmentDirections.actionCatFragmentToCatDetailFragment(item)
        // переход с анимацией
        findNavController().navigate(action, navOptions)
    }

    private fun <T> views(block: FragmentCatBinding.() -> T): T? = binding?.block()
}
