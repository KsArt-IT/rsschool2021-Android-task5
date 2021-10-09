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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.ksart.thecat.R
import ru.ksart.thecat.databinding.FragmentCatBinding
import ru.ksart.thecat.model.data.CatResponse
import ru.ksart.thecat.ui.list.adapter.breed.BreedAdapter
import ru.ksart.thecat.ui.list.adapter.cat.CatAdapter
import ru.ksart.thecat.ui.list.adapter.cat.CatLoadStateAdapter
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

        initBreedList()

        bindViewModel()
    }

    override fun onDestroyView() {
        Timber.d("onDestroyView")
        binding = null
        _catAdapter = null
        super.onDestroyView()
    }

    private fun initBreedList() {
        Timber.d("breed list init")
        views {
            breedList.run {
                adapter = BreedAdapter(viewModel::accept.invoke())
                // тут нельзя это использовать
//                setHasFixedSize(true)
                isNestedScrollingEnabled = false
            }
        }
    }

    private fun initCatList() {
        Timber.d("cat list init")
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

    private fun showCatDetail(item: CatResponse) {
        Timber.d(item.id)
        val action = CatFragmentDirections.actionCatFragmentToCatDetailFragment(item)
        // переход с анимацией
        findNavController().navigate(action, navOptions)
    }

    private fun bindViewModel() {
        // использование мульти flows
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Breed List
                launch { viewModel.breedList.collectLatest(breedAdapter::submitList) }
                launch {
                    viewModel.breedState.onEach { delay(250) }.collectLatest(::changeBreedState)
                }
                // CatList
                launch {
                    viewModel.stateCatListData
                        .collectLatest { (shouldScroll, pagingData) ->
                            Timber.d("submitData list in")
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
                launch {
                    viewModel.stateCatListSize.collectLatest {
                        Timber.d("submitData list empty=${it} count=${catAdapter.itemCount}")
                        views { emptyListTextView.isVisible = it }
                    }
                }
            }
        }
    }

    private fun changeBreedState(state: UiState) {
        Timber.d("breed state - change")
        if (state.breedQuery != state.lastBreedQuery) {
            Timber.d("breed state - select=${state.breedQuery} old=${state.lastBreedQuery}")
            // снимем старый выбор
            changeBreedSelected(state.lastBreedQuery, false)
            // установим новый выбор
            changeBreedSelected(state.breedQuery, true)
        }
        if (state.hasNotScrolledForCurrentSearch) {
            Timber.d("breed state - scroll cat list to top")
            // прокручиваем
            views { catList.scrollToPosition(0) }
        }
    }

    private fun changeBreedSelected(id: String, checked: Boolean) {
        Timber.d("breed selected size=${breedAdapter.currentList.size}")
        breedAdapter.currentList.firstOrNull {
            Timber.d("breed selected id=$id")
            it.id == id
        }?.apply {
            selected = checked
        }?.let {
            breedAdapter.currentList.indexOf(it).also { index ->
                Timber.d("breed selected = $id id=$index")
            }.let(breedAdapter::notifyItemChanged)
        }
    }

    private fun <T> views(block: FragmentCatBinding.() -> T): T? = binding?.block()
}
