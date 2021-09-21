package ru.ksart.thecat.model.db

interface CatDb {
    fun getCatDao(): CatDao

    companion object {
        const val DB_VERSION = 1
        const val DB_NAME = "cat_database.db"
    }
}
