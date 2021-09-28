package ru.ksart.thecat.model.data

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@Parcelize
@JsonClass(generateAdapter = true)
data class Breed(
    var selected: Boolean = false,
    @Json(name = "id")
    val id: String,
    @Json(name = "name")
    val name: String = "",
    @Json(name = "description")
    val description: String = "",
    @Json(name = "temperament")
    val temperament: String = "",
    @Json(name = "origin")
    val origin: String = "",
    @Json(name = "wikipedia_url")
    val wikipedia: String = "",
    @Json(name = "image")
    val breedImage: BreedImage? = null,
) : Parcelable
//https://api.thecatapi.com/v1/breeds
