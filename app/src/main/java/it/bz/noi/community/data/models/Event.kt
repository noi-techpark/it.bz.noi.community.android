package it.bz.noi.community.data.models

import com.google.gson.annotations.SerializedName
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

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
	) {
		data class ImageGallery(
			@SerializedName("ImageUrl")
			val imageUrl: String? = null
		)
	}
}

/**
 * Format event [Date] to [String] and vice-versa.
 */
interface EventDateParserI {
	fun parse(string: String): Date
}

class EventDateParser : EventDateParserI {

	private val df1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", LOCALE_EN_US_POSIX).apply {
		timeZone = TimeZone.getTimeZone("Europe/Rome")
	}

	private val df2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", LOCALE_EN_US_POSIX).apply {
		timeZone = TimeZone.getTimeZone("Europe/Rome")
	}

	private val df3 =
		SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZ", LOCALE_EN_US_POSIX).apply {
			timeZone = TimeZone.getTimeZone("Europe/Rome")
		}

	private val dateFormatters = listOf(df1, df2, df3)

	override fun parse(dateStr: String): Date {

		for (formatter in dateFormatters) {
			try {
				return formatter.parse(dateStr)
			} catch (ignore: ParseException) {
			}
		}
		throw Exception("Cannot parse string $dateStr")
	}

	companion object {
		val LOCALE_EN_US_POSIX = Locale("en_US_POSIX")
	}

}
