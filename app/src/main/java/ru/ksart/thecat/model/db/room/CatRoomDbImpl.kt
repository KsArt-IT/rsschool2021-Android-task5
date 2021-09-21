package ru.ksart.thecat.model.db.room

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.ksart.thecat.model.data.Cat
import ru.ksart.thecat.model.db.CatDao
import ru.ksart.thecat.model.db.CatDb

@Database(
    entities = [Cat::class],
    exportSchema = false,
    version = CatDb.DB_VERSION
)
abstract class CatRoomDbImpl : RoomDatabase(), CatDb {

    abstract override fun getCatDao(): CatRoomDao
}
