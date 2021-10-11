package ru.ksart.thecat.model.db.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CatWithBreeds(
    @Embedded val cat: Cat,
    @Relation(
        parentColumn = CatContract.Columns.CAT_ID,
        entityColumn = BreedContract.Columns.BREED_ID,
//        entity = CatBreedCrossRef::class,
        associateBy = Junction(CatBreedCrossRef::class),
//        projection = ["id"]
    )
    val breeds: List<Breed>
)
