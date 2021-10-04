package ru.ksart.thecat.model.networking

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import ru.ksart.thecat.model.data.Breed
import ru.ksart.thecat.model.data.CatResponse

interface CatApi {

    @GET("images/search")
    suspend fun searchCats(
        @Query("limit") limit: Int = NETWORK_PAGE_SIZE,
        @Query("page") page: Int = CAT_STARTING_PAGE_INDEX,
        @Query("breed_id") breedId: String = "",
        @Query("order") order: String = "Asc",
    ): Response<List<CatResponse>>

    @GET("breeds")
    suspend fun searchBreeds(): List<Breed>

    companion object {
        const val BASE_URL = "https://api.thecatapi.com/v1/"
        const val CAT_STARTING_PAGE_INDEX = 0
        const val NETWORK_PAGE_SIZE = 10
    }
}
