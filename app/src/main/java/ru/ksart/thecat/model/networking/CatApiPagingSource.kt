package ru.ksart.thecat.model.networking

import androidx.paging.PagingSource
import androidx.paging.PagingState
import retrofit2.HttpException
import ru.ksart.thecat.model.data.CatResponse
import timber.log.Timber
import java.io.IOException

class CatApiPagingSource(
    private val catApi: CatApi,
    private val query: String,
    private val onLoad: (Int) -> Unit
) : PagingSource<Int, CatResponse>() {

    override fun getRefreshKey(state: PagingState<Int, CatResponse>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, CatResponse> {
        val position = params.key ?: CatApi.CAT_STARTING_PAGE_INDEX
        return try {
            Timber.d("load catApi.searchCats breed=$query")
            val response =
                catApi.searchCats(page = position, limit = params.loadSize, breedId = query)
            if (response.isSuccessful) {
                val catResponse = response.body() ?: emptyList()
                Timber.d("load list=${catResponse.size}")
                onLoad(catResponse.size)
                val nextKey = if (catResponse.isEmpty()) {
                    null
                } else {
                    position + (params.loadSize / CatApi.NETWORK_PAGE_SIZE)
                }
                Timber.d("load nextKey=$nextKey")
                LoadResult.Page(
                    data = catResponse,
                    prevKey = if (position == CatApi.CAT_STARTING_PAGE_INDEX) null else position - 1,
                    nextKey = nextKey
                )
            } else {
                val e = Exception("Unknown error")
                Timber.e(e)
                LoadResult.Error(e)
            }
        } catch (e: IOException) {
            Timber.e(e, "IOException")
            LoadResult.Error(e)
        } catch (e: HttpException) {
            Timber.e(e, "HttpException")
            LoadResult.Error(e)
        }
    }
}
