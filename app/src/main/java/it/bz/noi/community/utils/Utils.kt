package it.bz.noi.community.utils

import it.bz.noi.community.data.models.EventsResponse
import java.util.*

object Utils {
	private const val HTTP_PREFIX = "http://"
	private const val GET_IMAGE_URL =
		"https://images.opendatahub.bz.it/api/Image/GetImageByUrl?imageUrl="

	fun getEventDescription(event: EventsResponse.Event): String? {
		return when (Locale.getDefault().language) {
			"it" -> {
				event.descriptionIT
			}
			"de" -> {
				event.descriptionDE
			}
			else -> {
				event.descriptionEN
			}
		}
	}

	fun getEventName(event: EventsResponse.Event, fallback: String = "N/D"): String {
		return when (Locale.getDefault().language) {
			"it" -> {
				event.nameIT ?: event.name ?: fallback
			}
			"de" -> {
				event.nameDE ?: event.name ?: fallback
			}
			else -> {
				event.nameEN ?: event.name ?: fallback
			}
		}
	}

	fun getEventOrganizer(event: EventsResponse.Event, fallback: String = "N/D"): String {
		return if (event.eventOrganizer.isNullOrEmpty())
			event.eventOrganizerFallback ?: fallback
		else
			event.eventOrganizer
	}

	fun getImageUrl(event: EventsResponse.Event): String? {
		var eventImageUrl = event.imageGallery?.firstOrNull { it.imageUrl != null }?.imageUrl

		if (eventImageUrl?.startsWith(HTTP_PREFIX) == true) {
			eventImageUrl = GET_IMAGE_URL + eventImageUrl
		}
		return eventImageUrl
	}
}
