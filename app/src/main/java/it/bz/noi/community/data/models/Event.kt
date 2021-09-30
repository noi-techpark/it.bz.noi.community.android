package it.bz.noi.community.data.models

import com.google.gson.annotations.SerializedName
import it.bz.noi.community.utils.Constants
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.lang.RuntimeException
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
		@Serializable(with = EventDateSerializer::class)
        val startDate: Date,
        @SerializedName("EndDate")
		@Serializable(with = EventDateSerializer::class)
        val endDate: Date,
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

/**
 * Format event [Date] to [String] and vice-versa.
 */
interface EventDateFormatterI {
	fun parse(string: String): Date
	fun format(dateTime: Date): String
}

class EventDateFormatter : EventDateFormatterI {

	private val df1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZ", LOCALE_EN_US).apply {
		timeZone = TimeZone.getTimeZone("Europe/Rome")
	}

	private val df2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", LOCALE_EN_US).apply {
		timeZone = TimeZone.getTimeZone("Europe/Rome")
	}

	private val df3 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", LOCALE_EN_US).apply {
		timeZone = TimeZone.getTimeZone("Europe/Rome")
	}

	override fun parse(dateStr: String): Date {
		var date = df1.parse(dateStr)
		if (date == null)
			date = df2.parse(dateStr)
		if (date == null)
			date = df3.parse(dateStr)
		if (date == null)
			throw RuntimeException("Cannot decode date string $dateStr")
		return date
	}

	// TODO spostare qui parameterDateFormatter()
	override fun format(date: Date): String = Constants.parameterDateFormatter().format(date)

	companion object {
		val LOCALE_EN_US = Locale("en_US_POSIX")
	}

}

/**
 * Serializes [Date] with [EventDateFormatter].
 */
object EventDateSerializer : KSerializer<Date> {

	private val formatter = EventDateFormatter()

	override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

	override fun serialize(encoder: Encoder, value: Date) = encoder.encodeString(formatter.format(value))

	override fun deserialize(decoder: Decoder): Date = formatter.parse(decoder.decodeString())
}
