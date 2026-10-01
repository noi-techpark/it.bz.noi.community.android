// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.data.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import it.bz.noi.community.utils.Utils
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
	@SerializedName("DateBegin")
	val startDate: Date = Date(),
	@SerializedName("DateEnd")
	val endDate: Date = Date(),
	@SerializedName("Detail")
	val detail: Map<String, EventDetail>? = null,
	@SerializedName("OrganizerInfos")
	val organizerInfos: Map<String, OrganizerInfo>? = null,
	@SerializedName("EventUrls")
	val eventUrls: List<EventUrl>? = null,
	@SerializedName("ImageGallery")
	val imageGallery: List<ImageGallery>? = null,
	@SerializedName("VenueIds")
	val venueIds: List<String>? = null,
	@SerializedName("EventDate")
	val eventDate: List<EventDateItem>? = null,

	// Not present in the raw API response: populated by resolve(venuesById) after
	// the batched /v1/Venue call, since venue/room resolution needs a second
	// network round-trip that happens once per page in MainRepository.
	val resolvedLocationName: String? = null,
	val resolvedMapUrl: String? = null,
) : Parcelable {

	@Keep
	@Parcelize
	data class EventDetail(
		@SerializedName("Title")
		val title: String? = null,
		@SerializedName("BaseText")
		val baseText: String? = null,
	) : Parcelable

	@Keep
	@Parcelize
	data class OrganizerInfo(
		@SerializedName("CompanyName")
		val companyName: String? = null,
	) : Parcelable

	@Keep
	@Parcelize
	data class EventUrl(
		@SerializedName("Url")
		val url: Map<String, String>? = null,
		@SerializedName("Type")
		val type: String? = null,
	) : Parcelable

	@Keep
	@Parcelize
	data class ImageGallery(
		@SerializedName("ImageUrl")
		val imageUrl: String? = null
	) : Parcelable

	@Keep
	@Parcelize
	data class EventDateItem(
		@SerializedName("VenueRoomDetailsIds")
		val venueRoomDetailsIds: List<String>? = null,
	) : Parcelable
}

/**
 * Signup URL, derived on demand from EventUrls where Type == "default" (the
 * one production record found with the misspelled "deafult" simply won't
 * match here, so no signup button shows for it - a graceful degradation, not
 * a crash).
 */
val Event.signupUrl: String?
	get() = eventUrls
		?.firstOrNull { it.type == "default" }
		?.url
		?.let { Utils.localizedOrFirst(it) }

/**
 * Merge venue/room data (fetched separately and batched across a page of
 * events) into this Event.
 *
 * VenueIds only identifies the building (e.g. "NOI Techpark"); the specific
 * room (e.g. "NOISE") lives under EventDate[0].VenueRoomDetailsIds and must be
 * resolved against that venue's own RoomDetails. Falls back to the building
 * name if there's no room-specific match, and to a null map URL (the caller
 * falls back to the generic R.string.url_map), exactly mirroring the old
 * on-click fallback behavior.
 */
fun Event.resolve(venuesById: Map<String, Venue>): Event {
	val venue = venueIds?.firstOrNull()?.let { venuesById[it] }
	val roomId = eventDate?.firstOrNull()?.venueRoomDetailsIds?.firstOrNull()
	val room = roomId?.let { rid -> venue?.roomDetails?.firstOrNull { it.id == rid } }

	val locationName = room?.shortname?.takeUnless { it.isBlank() }
		?: venue?.detail?.let { Utils.localizedOrFirst(it.mapValues { (_, d) -> d.title }) }

	val mapUrl = room?.mapping?.maps?.roomMapping?.takeUnless { it.isBlank() }

	return copy(resolvedLocationName = locationName, resolvedMapUrl = mapUrl)
}
