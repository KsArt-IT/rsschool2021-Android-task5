package ru.ksart.thecat.model.db

import androidx.paging.PagingSource
import ru.ksart.thecat.model.db.entity.Cat
import ru.ksart.thecat.model.db.entity.CatWithBreeds

interface CatDao {
    fun getAll(): PagingSource<Int, Cat>

    fun getCatsWithBreeds(): PagingSource<Int, CatWithBreeds>

    suspend fun insertAll(cats: List<Cat>)

    suspend fun deleteAll()

    suspend fun deleteById(id: String)

    suspend fun getNextIndex(): Int
}
