package ru.ksart.thecat.model.db.entity

object CatContract {
    const val TABLE_NAME = "cats"

    object Columns {
        const val CAT_ID = "cat_id"
        const val IMAGE_URL = "url"
        const val INDEX_RESPONSE = "index_response"
    }
}
