package ru.ksart.thecat.model.repositories

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.ksart.thecat.model.data.Breed
import ru.ksart.thecat.model.data.CatResponse

interface CatRepository {
    fun getSearchResultStream(query: String, onLoad: (Int) -> Unit): Flow<PagingData<CatResponse>>
    suspend fun getBreedsList(): List<Breed>
}
