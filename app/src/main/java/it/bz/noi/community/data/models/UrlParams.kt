package it.bz.noi.community.data.models

import com.google.gson.annotations.SerializedName

data class UrlParams(
    var startDate: String,
    var endDate: String? = null
)
