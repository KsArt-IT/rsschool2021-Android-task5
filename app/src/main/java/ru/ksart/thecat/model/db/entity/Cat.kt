package ru.ksart.thecat.model.db.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = CatContract.TABLE_NAME)
data class Cat(
    @PrimaryKey
    @ColumnInfo(name = CatContract.Columns.CAT_ID)
    val id: String,
    @ColumnInfo(name = CatContract.Columns.IMAGE_URL)
    val url: String,
    @ColumnInfo(name = CatContract.Columns.INDEX_RESPONSE)
    var indexInResponse: Int = -1,
) : Parcelable
