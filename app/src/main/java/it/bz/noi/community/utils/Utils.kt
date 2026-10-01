// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.SavedStateHandle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.bz.noi.community.BuildConfig
import it.bz.noi.community.R
import it.bz.noi.community.data.models.Event
import java.net.URLEncoder
import java.text.Normalizer
import java.util.*
import kotlin.reflect.KProperty

object Utils {

	private const val HTTP_PREFIX = "http://"
	private const val GET_IMAGE_URL =
		"https://images.opendatahub.com/api/Image/GetImageByUrl?imageUrl="
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

	/**
	 * Prefers the device's language; falls back to English; falls back to
	 * whatever language IS actually present in the map, rather than giving up.
	 * The API doesn't always populate every language for every field.
	 */
	fun <T> localizedOrFirst(map: Map<String, T>?): T? {
		if (map.isNullOrEmpty())
			return null
		val preferred = Locale.getDefault().language
		return map[preferred] ?: map[FALLBACK_LANGUAGE] ?: map.values.firstOrNull()
	}

	fun getEventDescription(event: Event): String? =
		localizedOrFirst(event.detail?.mapValues { it.value.baseText })

	fun getEventName(event: Event, fallback: String): String =
		localizedOrFirst(event.detail?.mapValues { it.value.title })?.takeUnless { it.isNullOrBlank() } ?: fallback

	fun getEventOrganizer(event: Event, fallback: String): String =
		localizedOrFirst(event.organizerInfos?.mapValues { it.value.companyName })?.takeUnless { it.isNullOrBlank() } ?: fallback

	fun getImageUrl(event: Event): String? {
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

	fun Activity.openLinkInExternalBrowserForResult(url: String, requestCode: Int) {
		val intent = Intent(Intent.ACTION_VIEW).apply {
			data = Uri.parse(url)
		}
		startActivityForResult(intent, requestCode)
	}

	fun Context.writeEmail(
		receiverAddress: String? = null,
		subject: String? = null,
		text: String? = null
	) {
		val intent = Intent(Intent.ACTION_SENDTO).apply {
			data = Uri.parse("mailto:") // only email apps should handle this

			receiverAddress?.apply {
				putExtra(Intent.EXTRA_EMAIL, Array(1) { receiverAddress })
			}
			subject?.apply {
				putExtra(Intent.EXTRA_SUBJECT, subject)
			}
			text?.apply {
				putExtra(Intent.EXTRA_TEXT, text)
			}

		}

		if (intent.resolveActivity(packageManager) == null) return

		startActivity(intent)
	}

	fun Context.showDial(phoneNumber: String) {
		val intent = Intent(Intent.ACTION_DIAL).apply {
			data = Uri.parse("tel:$phoneNumber")
		}
		if (intent.resolveActivity(packageManager) == null) return
		startActivity(intent)
	}

	fun Context.findOnMaps(address: String) {
		val encodedAddress = URLEncoder.encode(address, "utf-8")
		val gmmIntentUri: Uri = Uri.parse("geo:0,0?q=$encodedAddress")
		val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)

		if (mapIntent.resolveActivity(packageManager) == null) {
			MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_NOI_MaterialAlertDialog).apply {
				setMessage(getString(R.string.maps_error_msg))
				setPositiveButton(context.getString(R.string.ok_button)) { _, _ -> }
				show()
			}
			return
		}

		startActivity(mapIntent)
	}

	fun String.removeAccents(): String {
		var string = Normalizer.normalize(this, Normalizer.Form.NFD)
		string = Regex("\\p{InCombiningDiacriticalMarks}+").replace(string, "")
		return string
	}

	fun Context.copyToClipboard(label: String, value: String) {
		val clipboard =
			getSystemService(this, ClipboardManager::class.java) as ClipboardManager
		val clip: ClipData = ClipData.newPlainText(label, value)
		clipboard.setPrimaryClip(clip)
	}
}

public fun <T> savedStateProperty(
	savedStateHandle: SavedStateHandle,
	key: String,
	default: T
): SaveStateProperty<T> = SaveStateProperty(savedStateHandle, key, default)

class SaveStateProperty<T : Any?>(
	val savedStateHandle: SavedStateHandle,
	val key: String,
	val default: T
) {
	operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
		return if (savedStateHandle.contains(key)) {
			savedStateHandle.get<T>(key) as T
		} else {
			default
		}
	}

	operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
		savedStateHandle[key] = value
	}
}

fun getAppVersion() = "${BuildConfig.VERSION_NAME}.${BuildConfig.VERSION_CODE}"

fun <T> List<T>.groupedByInitial(initial: (T) -> Char): Map<Char, List<T>> {
	val contactsByFirstLetter: Map<Char,List<T>> = groupBy { initial(it) }
	val result: MutableMap<Char, List<T>> = mutableMapOf()
	('A'..'Z').forEach { letter: Char ->
		result[letter] = contactsByFirstLetter[letter] ?: emptyList()
	}
	result['#'] = contactsByFirstLetter['#'] ?: emptyList()
	return result
}
