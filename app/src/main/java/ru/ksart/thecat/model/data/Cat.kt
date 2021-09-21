package ru.ksart.thecat.model.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import ru.ksart.thecat.model.db.CatContract

@Entity(tableName = CatContract.TABLE_NAME)
@JsonClass(generateAdapter = true)
data class Cat(
    @PrimaryKey
    @Json(name = "id")
    @ColumnInfo(name = CatContract.Columns.ID)
    val id: String,
    @Json(name = "url")
    @ColumnInfo(name = CatContract.Columns.IMAGE_URL)
    val url: String,
    @ColumnInfo(name = CatContract.Columns.INDEX_RESPONSE)
    var indexInResponse: Int = -1,
)
