package it.bz.noi.community.data.models

import com.google.gson.annotations.SerializedName

data class EventsResponse(
    @SerializedName("Items")
    val events: List<Event>
) {
    data class Event(
        @SerializedName("Id")
        val eventId: String,
        @SerializedName("EventDescriptionEN")
        val name: String,
        @SerializedName("EventTextEN")
        val description: String,
        @SerializedName("TechnologyFields")
        val technologyFields: List<String>? = null,
        @SerializedName("AnchorVenue")
        val location: String,
        @SerializedName("StartDate")
        val startDate: String,
        @SerializedName("EndDate")
        val endDate: String,
        @SerializedName("ImageGallery")
        val imageGallery: List<ImageGallery>,
        @SerializedName("AnchorVenueRoomMapping")
        val roomName: String
    ) {
        data class ImageGallery(
            @SerializedName("ImageUrl")
            val imageUrl: String
        )
    }
}