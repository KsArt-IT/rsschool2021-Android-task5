package ru.ksart.thecat.model.repositories

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.ksart.thecat.model.data.CatResponse

interface CatRepository {
    fun getSearchResultStream(): Flow<PagingData<CatResponse>>
}
