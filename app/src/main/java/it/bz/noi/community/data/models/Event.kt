// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.data.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.util.Date


data class EventsResponse(
	@SerializedName("Items")
	val events: List<Event>
)

@Keep
@Parcelize
data class Event(
	@SerializedName("Id")
	val eventId: String? = null,
	@SerializedName("EventDescription")
	val name: String? = null,
	@SerializedName("EventDescriptionEN")
	val nameEN: String? = null,
	@SerializedName("EventDescriptionIT")
	val nameIT: String? = null,
	@SerializedName("EventDescriptionDE")
	val nameDE: String? = null,
	@SerializedName("EventTextEN")
	val descriptionEN: String? = null,
	@SerializedName("EventTextIT")
	val descriptionIT: String? = null,
	@SerializedName("EventTextDE")
	val descriptionDE: String? = null,
	@SerializedName("AnchorVenue")
	val location: String? = null,
	@SerializedName("Display5")
	val eventOrganizer: String? = null,
	@SerializedName("CompanyName")
	val eventOrganizerFallback: String? = null,
	@SerializedName("StartDate")
	val startDate: Date,
	@SerializedName("EndDate")
	val endDate: Date,
	@SerializedName("TechnologyFields")
	val technologyFields: List<String>? = null,
	@SerializedName("ImageGallery")
	val imageGallery: List<ImageGallery>? = null,
	@SerializedName("AnchorVenueRoomMapping")
	val roomName: String? = null,
	@SerializedName("WebAddress")
	val webAddress: String? = null
) : Parcelable {
	@Keep
	@Parcelize
	data class ImageGallery(
		@SerializedName("ImageUrl")
		val imageUrl: String? = null
	) : Parcelable
}
