package it.bz.noi.community.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import it.bz.noi.community.data.models.EventsResponse
import java.util.*

object Utils {

	private const val HTTP_PREFIX = "http://"
	private const val GET_IMAGE_URL =
		"https://images.opendatahub.bz.it/api/Image/GetImageByUrl?imageUrl="
	private const val FULLVIEW_PARAM = "fullview"
	private const val HIDEZOOM_PARAM = "hidezoom"
	const val ENGLISH = "en"
	const val ITALIAN = "it"
	const val GERMAN = "de"
	const val FALLBACK_LANGUAGE = "en"

	private const val NEWS_TOPIC_IT = "newsfeednoi_it"
	private const val NEWS_TOPIC_DE = "newsfeednoi_de"
	private const val NEWS_TOPIC_EN = "newsfeednoi_en"
	val allNoiNewsTopics: List<String> = listOf(NEWS_TOPIC_EN, NEWS_TOPIC_IT, NEWS_TOPIC_DE)

	fun getAppLanguage(): String? {
		val language = Locale.getDefault().language
		if (language in listOf(ENGLISH, ITALIAN, GERMAN))
			return language
		else
			return null
	}

	fun getEventDescription(event: EventsResponse.Event): String? {
		return when (Locale.getDefault().language) {
			ITALIAN -> {
				event.descriptionIT
			}
			GERMAN -> {
				event.descriptionDE
			}
			else -> {
				event.descriptionEN
			}
		}
	}

	fun getEventName(event: EventsResponse.Event, fallback: String = "N/D"): String {
		return when (Locale.getDefault().language) {
			ITALIAN -> {
				event.nameIT ?: event.name ?: fallback
			}
			GERMAN -> {
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

	fun addParamsToUrl(originalUrl: String, fullview: Boolean, hidezoom: Boolean): String {
		val newUriBuilder = Uri.parse(originalUrl).buildUpon()

		if (fullview)
			newUriBuilder.appendQueryParameter(FULLVIEW_PARAM, "1")
		if (hidezoom)
			newUriBuilder.appendQueryParameter(HIDEZOOM_PARAM, "1")

		return newUriBuilder.build().toString()
	}

	fun getPreferredNoiNewsTopic(): String {
		return when (Locale.getDefault().language) {
			ITALIAN -> NEWS_TOPIC_IT
			GERMAN -> NEWS_TOPIC_DE
			else -> NEWS_TOPIC_EN
		}
	}

	fun Context.openLinkInExternalBrowser(url: String) {
		val intent = Intent(Intent.ACTION_VIEW).apply {
			data = Uri.parse(url)
		}
		startActivity(intent)
	}
}
