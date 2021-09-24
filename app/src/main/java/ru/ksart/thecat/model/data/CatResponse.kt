package ru.ksart.thecat.model.data

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class CatResponse(
    @Json(name = "id")
    val id: String,
    @Json(name = "url")
    val url: String,
    @Json(name = "breeds")
    val breeds: List<Breeds> = emptyList(),
//    var indexInResponse: Int = -1,
):Parcelable
