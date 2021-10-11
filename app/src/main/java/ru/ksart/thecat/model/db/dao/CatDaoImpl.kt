package ru.ksart.thecat.model.db.dao

import androidx.paging.PagingSource
import androidx.room.*
import ru.ksart.thecat.model.db.entity.Cat
import ru.ksart.thecat.model.db.entity.CatContract
import ru.ksart.thecat.model.db.CatDao
import ru.ksart.thecat.model.db.entity.CatWithBreeds

@Dao
abstract class CatDaoImpl : CatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract override suspend fun insertAll(cats: List<Cat>)

    @Query("SELECT * FROM ${CatContract.TABLE_NAME} ORDER BY ${CatContract.Columns.INDEX_RESPONSE} ASC")
    abstract override fun getAll(): PagingSource<Int, Cat>

    @Query("DELETE FROM ${CatContract.TABLE_NAME}")
    abstract override suspend fun deleteAll()

    @Query("DELETE FROM ${CatContract.TABLE_NAME} WHERE ${CatContract.Columns.CAT_ID} = :id")
    abstract override suspend fun deleteById(id: String)

    @Query("SELECT MAX(${CatContract.Columns.INDEX_RESPONSE}) + 1 FROM ${CatContract.TABLE_NAME}")
    abstract override suspend fun getNextIndex(): Int

    @Transaction
    @Query("SELECT * FROM ${CatContract.TABLE_NAME}")
    abstract override fun getCatsWithBreeds(): PagingSource<Int, CatWithBreeds>
}
