package ru.ksart.thecat.model.db

import ru.ksart.thecat.model.db.dao.BreedDaoImpl

interface CatDb {
    fun getCatDao(): CatDao
    fun getBreedDao(): BreedDaoImpl

    companion object {
        const val DB_VERSION = 1
        const val DB_NAME = "cat_database.db"
    }
}
