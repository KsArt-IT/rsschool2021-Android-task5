package ru.ksart.thecat.model.repositories

import ru.ksart.thecat.model.networking.CatApi
import javax.inject.Inject

class CatRepositoryImpl @Inject constructor(
    private val api: CatApi
): CatRepository {
}
