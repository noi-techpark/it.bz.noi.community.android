package it.bz.noi.community.data.models

import com.google.gson.annotations.SerializedName

data class EventsResponse(
    @SerializedName("Items")
    val events: List<Event>
) {
    data class Event(
        @SerializedName("EventId")
        val eventId: Long,
        @SerializedName("EventDescription")
        val name: String,
        @SerializedName("EventLocation")
        val location: String,
        @SerializedName("StartDate")
        val startDate: String,
        @SerializedName("EndDate")
        val endDate: String
    )
}