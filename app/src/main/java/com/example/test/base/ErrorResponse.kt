package com.example.test.base

import android.os.Parcelable
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize

@JsonClass(generateAdapter = true)
@Parcelize
data class ErrorResponse(
    @param:Json(name = "image_url")
    val imageUrl: String?,
    @param:Json(name = "subtitle")
    val subtitle: String?,
    @param:Json(name = "title")
    val title: String?,
    @param:Json(name = "type")
    val type: String?
) : Parcelable
