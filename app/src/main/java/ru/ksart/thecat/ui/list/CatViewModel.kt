package ru.ksart.thecat.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.cachedIn
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.ksart.thecat.model.data.Breed
import ru.ksart.thecat.model.data.CatResponse
import ru.ksart.thecat.model.repositories.CatRepository
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CatViewModel @Inject constructor(
    private val repository: CatRepository,
//    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val state: StateFlow<UiState>
    private val _breedStateChannel = Channel<UiState>()
    val breedState = _breedStateChannel.receiveAsFlow()

    val accept: (UiAction) -> Unit

    private val shouldScrollToTop = MutableStateFlow(false)

    private var initialQuery: String
    private var lastQuery: String

    val stateCatListData: Flow<Pair<Boolean, PagingData<CatResponse>>>

    private val _breedList = MutableStateFlow<List<Breed>>(emptyList())
    val breedList = _breedList.asStateFlow()

    init {
        searchBreeds()

        initialQuery = ""
        lastQuery = ""

        val actionStateFlow = MutableSharedFlow<UiAction>()

        accept = { action ->
            viewModelScope.launch {
                Timber.d("accept action=$action")
                actionStateFlow.emit(action)
            }
        }

        val searches = actionStateFlow
            .filterIsInstance<UiAction.Search>()
            .onEach {
                Timber.d("searches actionStateFlow search=${it.breedQuery}")
            }
            .distinctUntilChanged()
/*
            .onStart {
                DebugHelper.log("CatViewModel|searches actionStateFlow")
                emit(UiAction.Search(breedQuery = initialQuery))
            }
*/

        val queriesScrolled = actionStateFlow
            .filterIsInstance<UiAction.Scroll>()
            .distinctUntilChanged()
            .onEach { scroll ->
                Timber.d("queriesScrolled actionStateFlow=${scroll.currentBreedQuery}")
            }
            // This is shared to keep the flow "hot" while caching the last query scrolled,
            // otherwise each flatMapLatest invocation would lose the last query scrolled,
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )
//            .onStart { emit(UiAction.Scroll(currentBreedQuery = lastQueryScrolled)) }

        state = searches
            .flatMapLatest { search ->
                combine(
                    queriesScrolled,
                    searchCats(query = search.breedQuery),
                    ::Pair
                )
                    .onEach {
                        Timber.d("state in")
                    }
                    // Each unique PagingData should be submitted once, take the latest from
                    // queriesScrolled
                    .distinctUntilChangedBy { it.second }
                    .map { (scroll, pagingData) ->
                        UiState(
                            breedQuery = search.breedQuery,
                            pagingData = pagingData,
                            lastBreedQuery = lastQuery,
                            // If the search query matches the scroll query, the user has scrolled
                            hasNotScrolledForCurrentSearch = search.breedQuery != scroll.currentBreedQuery
                        )
                    }
                    .onEach {
                        Timber.d("state new=${it.breedQuery} old=${it.lastBreedQuery}")
                        lastQuery = it.breedQuery
                        _breedStateChannel.send(it)
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = UiState()
            )

        val statePagingData = state
            .map { it.pagingData }
            .distinctUntilChanged()

        stateCatListData = combine(
            shouldScrollToTop,
            statePagingData,
            ::Pair
        )
            .onEach {
                Timber.d("in")
            }
            // Each unique PagingData should be submitted once, take the latest from
            // shouldScrollToTop
            .distinctUntilChangedBy { it.second }
            .onEach {
                Timber.d("out")
            }
    }

    fun loadState(notLoading: Flow<CombinedLoadStates>) {
        combine(
            notLoading // Only emit when REFRESH LoadState for RemoteMediator changes.
                .distinctUntilChangedBy { it.refresh }
                // Only react to cases where Remote REFRESH completes i.e., NotLoading.
                .map { it.refresh is LoadState.NotLoading },

            state
                .map { it.hasNotScrolledForCurrentSearch }
                .distinctUntilChanged(),

            Boolean::and
        )
            .onEach {
                Timber.d("shouldScrollToTop in")
            }
            .distinctUntilChanged()
            .onEach {
                Timber.d("shouldScrollToTop=$it")
                shouldScrollToTop.value = it
            }
    }

    fun scroll() {
        Timber.d("breedQuery=${state.value.breedQuery}")
        accept(UiAction.Scroll(currentBreedQuery = state.value.breedQuery))
    }

    private fun searchBreeds() {
        viewModelScope.launch {
            _breedList.value = try {
                Timber.d("in")
                val list = repository.getBreedsList()
                // загрузим список по породе или весь без породы
                lastQuery = list.firstOrNull {
                    it.id == lastQuery
                }?.id ?: ""
                initialQuery = list.firstOrNull {
                    it.id == initialQuery
                }?.id ?: ""
                accept(UiAction.Search(breedQuery = initialQuery))
                accept(UiAction.Scroll(currentBreedQuery = lastQuery))
                // добавим беспородных
                listOf(Breed(id = "", name = "All Cats", selected = true)) + list
//                listOf(Breed(id = "", name = "All Cats"),Breed(id = "-", name = "-")) + list
            } catch (e: Exception) {
                Timber.e(e)
                emptyList()
            }
        }
    }

    private fun searchCats(query: String): Flow<PagingData<CatResponse>> =
        repository.getSearchResultStream(query)
            .onEach {
                Timber.d("query=$query")
            }
            .cachedIn(viewModelScope)
}

sealed class UiAction {
    class Search(val breedQuery: String) : UiAction()
    class Scroll(val currentBreedQuery: String) : UiAction()
}

data class UiState(
    val breedQuery: String = "",
    val lastBreedQuery: String = "",
    val hasNotScrolledForCurrentSearch: Boolean = false,
    val pagingData: PagingData<CatResponse> = PagingData.empty()
)
