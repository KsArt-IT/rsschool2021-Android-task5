package ru.ksart.thecat.model.db

import androidx.paging.PagingSource
import ru.ksart.thecat.model.data.Cat

interface CatDao {
    fun getAll(): PagingSource<Int, Cat>

    suspend fun insertAll(cats: List<Cat>)

    suspend fun deleteById(id: String)

    suspend fun getNextIndex(): Int
}
