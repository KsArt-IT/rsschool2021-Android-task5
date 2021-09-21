package ru.ksart.thecat.model.networking

import retrofit2.http.GET
import retrofit2.http.Query
import ru.ksart.thecat.model.data.Cat

interface CatApi {

    @GET("/")
    suspend fun searchCats(
        @Query("limit") limit: Int = 10,
        @Query("page") page: Int = 0,
        @Query("order") order: String = "Asc",
    ): List<Cat>
}
