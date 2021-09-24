package ru.ksart.thecat.model.networking

import androidx.paging.PagingSource
import androidx.paging.PagingState
import retrofit2.HttpException
import ru.ksart.thecat.model.data.CatResponse
import ru.ksart.thecat.utils.DebugHelper
import java.io.IOException

class CatApiPagingSource(
    private val catApi: CatApi,
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
                LoadResult.Page(
                    data = catResponse,
                    prevKey = if (position == CatApi.CAT_STARTING_PAGE_INDEX) null else position - 1,
                    nextKey = nextKey
                )
            } else {
                val e = Exception("Unknown error")
                DebugHelper.log("CatApiPagingSource|load error(Unknown error): ",e)
                LoadResult.Error(e)
            }
        } catch (e: IOException) {
            DebugHelper.log("CatApiPagingSource|load error(IOException): ",e)
            LoadResult.Error(e)
        } catch (e: HttpException) {
            DebugHelper.log("CatApiPagingSource|load error(HttpException): ",e)
            LoadResult.Error(e)
        }
    }
}
