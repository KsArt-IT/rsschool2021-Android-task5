package ru.ksart.thecat.model.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.ksart.thecat.model.db.entity.Breed
import ru.ksart.thecat.model.db.entity.BreedContract

@Dao
interface BreedDaoImpl {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(breeds: List<Breed>)

    @Query("SELECT * FROM ${BreedContract.TABLE_NAME} ORDER BY ${BreedContract.Columns.NAME} ASC")
    suspend fun getAll(): List<Breed>

    @Query("DELETE FROM ${BreedContract.TABLE_NAME}")
    suspend fun deleteAll()
}
