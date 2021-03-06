package ru.ksart.thecat.model.repositories

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import ru.ksart.thecat.model.data.BreedResponse
import ru.ksart.thecat.model.data.CatResponse
import ru.ksart.thecat.model.db.CatDao
import ru.ksart.thecat.model.networking.CatApi
import ru.ksart.thecat.model.networking.CatApiPagingSource
import timber.log.Timber
import javax.inject.Inject

class CatRepositoryImpl @Inject constructor(
    private val catApi: CatApi,
    private val catDao: CatDao,
) : CatRepository {

    override suspend fun getBreedsList(): List<BreedResponse> = withContext(Dispatchers.IO) {
        catApi.searchBreeds()
    }

    @ExperimentalPagingApi
    override fun getSearchResultStream(query: String): Flow<PagingData<CatResponse>> {
        Timber.d("repository search cat")
        return Pager(
            config = PagingConfig(pageSize = CatApi.NETWORK_PAGE_SIZE, enablePlaceholders = false),
            pagingSourceFactory = { CatApiPagingSource(catApi, query) },
//            remoteMediator = CatRemoteMediator(catDao, catApi)
        ).flow
    }
}
