package ru.ksart.thecat.model.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = CatBreedContract.TABLE_NAME,
    primaryKeys = [CatContract.Columns.CAT_ID, BreedContract.Columns.BREED_ID],
/*
    indices = [
        Index(value = [CatBreedContract.Columns.CAT_ID]),
        Index(value = [CatBreedContract.Columns.BREED_ID])
    ],
*/
    foreignKeys = [
        // связь
        ForeignKey(
            entity = Cat::class,
            parentColumns = [CatContract.Columns.CAT_ID],
            childColumns = [CatContract.Columns.CAT_ID],
            onDelete = ForeignKey.CASCADE // при удалении из родительской таблицы удилиться и дочерняя запись
        ),
        ForeignKey(
            entity = Breed::class,
            parentColumns = [BreedContract.Columns.BREED_ID],
            childColumns = [BreedContract.Columns.BREED_ID],
            onDelete = ForeignKey.CASCADE // при удалении из родительской таблицы удилиться и дочерняя запись
        )
    ]
)
data class CatBreedCrossRef(
    @ColumnInfo(name = CatContract.Columns.CAT_ID, index = true)
    val catId: String,
    @ColumnInfo(name = BreedContract.Columns.BREED_ID, index = true)
    val breedId: String,
)
