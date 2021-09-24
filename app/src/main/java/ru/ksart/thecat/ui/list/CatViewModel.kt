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
import ru.ksart.thecat.model.data.CatResponse
import ru.ksart.thecat.model.repositories.CatRepository
import ru.ksart.thecat.utils.DebugHelper
import javax.inject.Inject

@HiltViewModel
class CatViewModel @Inject constructor(
    private val repository: CatRepository,
//    private val savedStateHandle: SavedStateHandle
): ViewModel() {

    private val state: StateFlow<UiState>

    private val shouldScrollToTop = MutableStateFlow(false)

    val stateListData: Flow<Pair<Boolean, PagingData<CatResponse>>>

    val accept: (UiAction) -> Unit

    init {
        val actionStateFlow = MutableSharedFlow<UiAction>()

        val searches = actionStateFlow
            .filterIsInstance<UiAction.Search>()
            .distinctUntilChanged()
            .onStart { emit(UiAction.Search()) }

        val queriesScrolled = actionStateFlow
            .filterIsInstance<UiAction.Scroll>()
            .distinctUntilChanged()
            // This is shared to keep the flow "hot" while caching the last query scrolled,
            // otherwise each flatMapLatest invocation would lose the last query scrolled,
            .shareIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                replay = 1
            )
            .onStart { emit(UiAction.Scroll(currentQuery = "")) }

        state = searches
            .flatMapLatest { search ->
                combine(
                    queriesScrolled,
                    searchCats(),
                    ::Pair
                )
                    .onEach {
//                        DebugHelper.log("CatViewModel|state")
                    }
                    // Each unique PagingData should be submitted once, take the latest from
                    // queriesScrolled
                    .distinctUntilChangedBy { it.second }
                    .map { (scroll, pagingData) ->
                        UiState(
                            pagingData = pagingData,
                        )
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
                initialValue = UiState()
            )

        accept = { action ->
            viewModelScope.launch { actionStateFlow.emit(action) }
        }

        val statePagingData = state
            .onEach {
                DebugHelper.log("CatViewModel|pagingData")
            }
            .map { it.pagingData }
            .distinctUntilChanged()

        stateListData = combine(
            shouldScrollToTop,
            statePagingData,
            ::Pair)
            .onEach {
                DebugHelper.log("CatViewModel|stateList")
            }
            // Each unique PagingData should be submitted once, take the latest from
            // shouldScrollToTop
            .distinctUntilChangedBy { it.second }
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
                DebugHelper.log("FragmentCatBinding|shouldScrollToTop")
            }
            .distinctUntilChanged()
            .onEach { shouldScrollToTop.value = it }
    }

    private fun searchCats(): Flow<PagingData<CatResponse>> =
        repository.getSearchResultStream()
            .cachedIn(viewModelScope)
}

sealed class UiAction {
    class Search(val query: String = "") : UiAction()
    class Scroll(val currentQuery: String = "") : UiAction()
}

data class UiState(
    val hasNotScrolledForCurrentSearch: Boolean = false,
    val pagingData: PagingData<CatResponse> = PagingData.empty()
)
