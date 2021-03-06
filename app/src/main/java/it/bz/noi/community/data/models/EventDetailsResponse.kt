package it.bz.noi.community.data.models

import com.google.gson.annotations.SerializedName

data class EventDetailsResponse(
    @SerializedName("EventDescription")
    val eventDescription: String,
    @SerializedName("StartDate")
    val startDate: String,
    @SerializedName("EndDate")
    val endDate: String
)