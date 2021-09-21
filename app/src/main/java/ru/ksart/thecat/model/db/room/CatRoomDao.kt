package ru.ksart.thecat.model.db.room

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.ksart.thecat.model.data.Cat
import ru.ksart.thecat.model.db.CatContract
import ru.ksart.thecat.model.db.CatDao

@Dao
abstract class CatRoomDao: CatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract override suspend fun insertAll(cats: List<Cat>)

    @Query("SELECT * FROM ${CatContract.TABLE_NAME} ORDER BY ${CatContract.Columns.INDEX_RESPONSE} ASC")
    abstract override fun getAll(): PagingSource<Int, Cat>

    @Query("DELETE FROM ${CatContract.TABLE_NAME} WHERE ${CatContract.Columns.ID} = :id")
    abstract override suspend fun deleteById(id: String)

    @Query("SELECT MAX(${CatContract.Columns.INDEX_RESPONSE}) + 1 FROM ${CatContract.TABLE_NAME}")
    abstract override suspend fun getNextIndex(): Int
}
