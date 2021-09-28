package ru.ksart.thecat.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.ksart.thecat.model.data.Breed
import ru.ksart.thecat.model.data.CatResponse
import ru.ksart.thecat.model.repositories.CatRepository
import ru.ksart.thecat.utils.DebugHelper
import javax.inject.Inject

@HiltViewModel
class CatViewModel @Inject constructor(
    private val repository: CatRepository,
//    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val state: StateFlow<UiState>

    val accept: (UiAction) -> Unit

    private val shouldScrollToTop = MutableStateFlow(false)

    val stateCatListData: Flow<Pair<Boolean, PagingData<CatResponse>>>

    private val _breedList = MutableStateFlow<List<Breed>>(emptyList())
    val breedList: StateFlow<List<Breed>> get() = _breedList.asStateFlow()

    private val _breedSelected : MutableStateFlow<Pair<String, String>>
    val breedSelected get() = _breedSelected.asStateFlow()

    init {
        searchBreeds()

        val initialQuery: String = ""
        var lastQueryScrolled: String = ""

        _breedSelected = MutableStateFlow(initialQuery to lastQueryScrolled)

        val actionStateFlow = MutableSharedFlow<UiAction>()

        val searches = actionStateFlow
            .filterIsInstance<UiAction.Search>()
            .onEach {
                DebugHelper.log("CatViewModel|searches actionStateFlow search=${it.breedQuery}")
            }
            .distinctUntilChanged()
            .onStart {
                DebugHelper.log("CatViewModel|searches actionStateFlow")
                emit(UiAction.Search(breedQuery = initialQuery))
            }

        val queriesScrolled = actionStateFlow
            .filterIsInstance<UiAction.Scroll>()
            .onEach { scroll ->
                DebugHelper.log("CatViewModel|queriesScrolled actionStateFlow=${scroll.currentBreedQuery}")
            }
            .distinctUntilChanged()
            // This is shared to keep the flow "hot" while caching the last query scrolled,
            // otherwise each flatMapLatest invocation would lose the last query scrolled,
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )
            .onStart { emit(UiAction.Scroll(currentBreedQuery = lastQueryScrolled)) }

        state = searches
            .flatMapLatest { search ->
                combine(
                    queriesScrolled,
                    searchCats(query = search.breedQuery),
                    ::Pair
                )
                    .onEach {
                        DebugHelper.log("CatViewModel|searches state")
                    }
                    // Each unique PagingData should be submitted once, take the latest from
                    // queriesScrolled
                    .distinctUntilChangedBy { it.second }
                    .map { (scroll, pagingData) ->
                        if (search.breedQuery != lastQueryScrolled) {
                            _breedSelected.value = search.breedQuery to lastQueryScrolled
                            lastQueryScrolled = search.breedQuery
                        }
                        UiState(
                            breedQuery = search.breedQuery,
                            pagingData = pagingData,
                            lastBreedQueryScrolled = scroll.currentBreedQuery,
                            // If the search query matches the scroll query, the user has scrolled
                            hasNotScrolledForCurrentSearch = search.breedQuery != scroll.currentBreedQuery
                        )
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = UiState()
            )

        accept = { action ->
            viewModelScope.launch {
                DebugHelper.log("CatViewModel|accept action")
                actionStateFlow.emit(action)
            }
        }

        val statePagingData = state
            .onEach {
                DebugHelper.log("CatViewModel|pagingData")
            }
            .map { it.pagingData }
            .distinctUntilChanged()

        stateCatListData = combine(
            shouldScrollToTop,
            statePagingData,
            ::Pair
        )
            .onEach {
                DebugHelper.log("CatViewModel|stateList in")
            }
            // Each unique PagingData should be submitted once, take the latest from
            // shouldScrollToTop
            .distinctUntilChangedBy { it.second }
            .onEach {
                DebugHelper.log("CatViewModel|stateList out")
            }
    }

    fun loadState(notLoading: Flow<CombinedLoadStates>) {
        combine(
            notLoading// Only emit when REFRESH LoadState for RemoteMediator changes.
                .onEach {
                    DebugHelper.log("CatFragment|notLoading")
                }
                .distinctUntilChangedBy { it.refresh }
                // Only react to cases where Remote REFRESH completes i.e., NotLoading.
                .map { it.refresh is LoadState.NotLoading },

            state
                .onEach {
                    DebugHelper.log("CatFragment|hasNotScrolledForCurrentSearch")
                }
                .map { it.hasNotScrolledForCurrentSearch }
                .distinctUntilChanged(),

            Boolean::and
        )
            .onEach {
                DebugHelper.log("CatFragment|shouldScrollToTop in")
            }
            .distinctUntilChanged()
            .onEach {
                DebugHelper.log("CatFragment|shouldScrollToTop =$it")
                shouldScrollToTop.value = it
            }
    }

    fun scroll() {
        accept(UiAction.Scroll(currentBreedQuery = state.value.breedQuery))
    }

    private fun searchBreeds() {
        viewModelScope.launch {
            _breedList.value = try {
                DebugHelper.log("CatFragment|searchBreeds")
                val list = repository.getBreedsList()
                listOf(Breed(id = "", name = "All Cats", selected = true)) + list
//                listOf(Breed(id = "", name = "All Cats"),Breed(id = "-", name = "-")) + list
            } catch (e: Exception) {
                DebugHelper.log("CatFragment|searchBreeds error", e)
                emptyList()
            }
        }
    }

    private fun searchCats(query: String = ""): Flow<PagingData<CatResponse>> =
        repository.getSearchResultStream(query)
            .onEach {
                DebugHelper.log("CatFragment|searchCats query=$query")
            }
            .cachedIn(viewModelScope)
}

sealed class UiAction {
    class Search(val breedQuery: String = "") : UiAction()
    class Scroll(val currentBreedQuery: String = "") : UiAction()
}

data class UiState(
    val breedQuery: String = "",
    val lastBreedQueryScrolled: String = "",
    val hasNotScrolledForCurrentSearch: Boolean = false,
    val pagingData: PagingData<CatResponse> = PagingData.empty()
)
