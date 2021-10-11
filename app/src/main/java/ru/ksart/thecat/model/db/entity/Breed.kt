package ru.ksart.thecat.model.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = BreedContract.TABLE_NAME)
data class Breed(
    @PrimaryKey
    @ColumnInfo(name = BreedContract.Columns.BREED_ID)
    val id: String,
    @ColumnInfo(name = BreedContract.Columns.NAME)
    val name: String,
    @ColumnInfo(name = BreedContract.Columns.DESCRIPTION)
    val description: String,
    @ColumnInfo(name = BreedContract.Columns.TEMPERAMENT)
    val temperament: String,
    @ColumnInfo(name = BreedContract.Columns.ORIGIN)
    val origin: String,
    @ColumnInfo(name = BreedContract.Columns.WIKIPEDIA)
    val wikipedia: String,
)

