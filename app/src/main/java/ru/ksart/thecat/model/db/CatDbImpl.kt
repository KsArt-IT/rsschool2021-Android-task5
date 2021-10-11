package ru.ksart.thecat.model.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.ksart.thecat.model.db.entity.Breed
import ru.ksart.thecat.model.db.entity.Cat
import ru.ksart.thecat.model.db.dao.BreedDaoImpl
import ru.ksart.thecat.model.db.dao.CatDaoImpl
import ru.ksart.thecat.model.db.entity.CatBreedCrossRef

@Database(
    entities = [Cat::class, Breed::class, CatBreedCrossRef::class],
    exportSchema = false,
    version = CatDb.DB_VERSION
)
abstract class CatDbImpl : RoomDatabase(), CatDb {

    abstract override fun getCatDao(): CatDaoImpl
    abstract override fun getBreedDao(): BreedDaoImpl
}
