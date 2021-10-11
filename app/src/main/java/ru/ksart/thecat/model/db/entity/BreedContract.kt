package ru.ksart.thecat.model.db.entity

object BreedContract {
    const val TABLE_NAME = "breeds"

    object Columns {
        const val BREED_ID = "breed_id"
        const val NAME = "name"
        const val DESCRIPTION = "description"
        const val TEMPERAMENT = "temperament"
        const val ORIGIN = "origin"
        const val WIKIPEDIA = "wikipedia"
    }
}
