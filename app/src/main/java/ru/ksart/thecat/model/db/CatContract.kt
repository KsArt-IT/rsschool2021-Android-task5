package ru.ksart.thecat.model.db

object CatContract {
    const val TABLE_NAME = "cats"

    object Columns {
        const val ID = "id"
        const val IMAGE_URL = "url"
        const val INDEX_RESPONSE = "index_response"
    }
}
