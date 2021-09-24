package ru.ksart.thecat.model.repositories

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import retrofit2.HttpException
import ru.ksart.thecat.model.data.Cat
import ru.ksart.thecat.model.data.CatResponse
import ru.ksart.thecat.model.db.CatDao
import ru.ksart.thecat.model.db.CatDb
import ru.ksart.thecat.model.networking.CatApi
import ru.ksart.thecat.utils.DebugHelper
import java.io.IOException
import javax.inject.Inject

@OptIn(ExperimentalPagingApi::class)
class CatRemoteMediator @Inject constructor(
    private val dao: CatDao,
    private val catApi: CatApi,
) : RemoteMediator<Int, CatResponse>() {

    override suspend fun initialize(): InitializeAction {
        // Require that remote REFRESH is launched on initial load and succeeds before launching
        // remote PREPEND / APPEND.
        return InitializeAction.LAUNCH_INITIAL_REFRESH
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, CatResponse>
    ): MediatorResult {
/*
        val position = params.key ?: CatApi.CAT_STARTING_PAGE_INDEX

        return try {
            DebugHelper.log("CatApiPagingSource|load catApi.searchCats")
            val response  = catApi.searchCats(page = position, limit = params.loadSize)
            if (response.isSuccessful) {
                val catResponse = response.body() ?: emptyList()
                DebugHelper.log("CatApiPagingSource|load list=${catResponse.size}")
                val nextKey = if (catResponse.isEmpty()) {
                    null
                } else {
                    position + (params.loadSize / CatApi.NETWORK_PAGE_SIZE)
                }
                DebugHelper.log("CatApiPagingSource|load nextKey=$nextKey")
                PagingSource.LoadResult.Page(
                    data = catResponse,
                    prevKey = if (position == CatApi.CAT_STARTING_PAGE_INDEX) null else position - 1,
                    nextKey = nextKey
                )
            } else {
                PagingSource.LoadResult.Error(Exception("Unknown error"))
            }
*/
        return try {
            DebugHelper.log("CatRemoteMediator|load")
/*
            val data = redditApi.getTop(
                subreddit = subredditName,
                after = loadKey,
                before = null,
                limit = when (loadType) {
                    LoadType.REFRESH -> state.config.initialLoadSize
                    else -> state.config.pageSize
                }
            ).data
*/

        val response = catApi.searchCats(
            limit = 10,
            page = 0,
        )
            val items = response.takeIf { it.isSuccessful }?.body() ?: emptyList()
            DebugHelper.log("CatRemoteMediator|load lise=${items.size}")

/*
                if (loadType == LoadType.REFRESH) {
                    dao.deleteByPage(subredditName)
                }

                dao.insertAll(items)
*/

            MediatorResult.Success(endOfPaginationReached = items.isEmpty())
        } catch (e: IOException) {
            DebugHelper.log("CatRemoteMediator|load IOException: ", e)
            MediatorResult.Error(e)
        } catch (e: HttpException) {
            DebugHelper.log("CatRemoteMediator|load HttpException: ", e)
            MediatorResult.Error(e)
        }
    }
}

