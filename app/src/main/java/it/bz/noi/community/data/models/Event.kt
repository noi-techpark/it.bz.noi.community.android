package it.bz.noi.community.data.models

import com.google.gson.annotations.SerializedName

data class EventsResponse(
    @SerializedName("Items")
    val events: List<Event>
) {
    data class Event(
        @SerializedName("Id")
        val eventId: String? = null,
        @SerializedName("EventDescription")
        val name: String? = null,
		@SerializedName("EventDescriptionEN")
		val nameEN: String? = null,
        @SerializedName("EventTextEN")
        val description: String? = null,
        @SerializedName("AnchorVenue")
        val location: String? = null,
        @SerializedName("Display5")
        val eventOrganizer: String? = null,
        @SerializedName("CompanyName")
        val eventOrganizerFallback: String? = null,
        @SerializedName("StartDate")
        val startDate: String,
        @SerializedName("EndDate")
        val endDate: String,
        @SerializedName("TechnologyFields")
        val technologyFields: List<String>? = null,
        @SerializedName("ImageGallery")
        val imageGallery: List<ImageGallery>? = null,
        @SerializedName("AnchorVenueRoomMapping")
        val roomName: String? = null
    ) {
        data class ImageGallery(
            @SerializedName("ImageUrl")
            val imageUrl: String? = null
        )
    }
}
