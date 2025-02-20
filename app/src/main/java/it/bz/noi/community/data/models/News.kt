// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.data.models

import android.os.Parcelable
import android.util.Log
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import it.bz.noi.community.utils.Utils
import kotlinx.parcelize.Parcelize
import java.net.MalformedURLException
import java.net.URL
import java.util.*

private const val ID_TAG_IMPORTANT = "important" // ID of the "important" tag

@Keep
@Parcelize
data class News(
	@SerializedName("Id")
	val id: String,
	@SerializedName("ArticleDate")
	val date: Date,
	@SerializedName("Detail")
	val detail: Map<String, Detail>,
	@SerializedName("ContactInfos")
	val contactInfo: Map<String, ContactInfo>? = null,
	@SerializedName("ImageGallery")
	val images: List<NewsImage>? = null,
	@SerializedName("ODHTags")
	val tags: List<Tag>? = null,
	@SerializedName("Highlight")
	val isHighlighted: Boolean = false,
	@SerializedName("VideoItems")
	val videos: Map<String, List<NewsVideo>?>? = null
) : Parcelable

@Keep
@Parcelize
data class Tag(
	@SerializedName("Id")
	val id: String? = null,
) : Parcelable

@Keep
@Parcelize
data class Detail(
	@SerializedName("Title")
	val title: String? = null,
	@SerializedName("AdditionalText")
	val abstract: String? = null,
	@SerializedName("BaseText")
	val text: String? = null,
) : Parcelable

@Keep
@Parcelize
data class ContactInfo(
	@SerializedName("CompanyName")
	val publisher: String? = null,
	@SerializedName("LogoUrl")
	val logo: String? = null,
	@SerializedName("Url")
	val externalLink: String? = null,
	@SerializedName("Email")
	val email: String? = null,
) : Parcelable

@Keep
@Parcelize
data class NewsImage(
	@SerializedName("ImageUrl")
	val url: String? = null
) : Parcelable

@Keep
@Parcelize
data class NewsVideo(
	@SerializedName("Url")
	val url: String,
	var thumbnailUrl: String? = null,
	var videoId: String? = null
) : Parcelable

// funzione per ricavare l'id dall'url del video
fun extractNewsVideoId(videoUrl: String): String? {
	try {
		val url = URL(videoUrl)

		// Estrai il percorso dall'URL (la parte dopo il dominio)
		val path = url.path

		val pathComponents = path.split("/")
		if (pathComponents.isNotEmpty()) {
			// L'ID del video è il componente che segue "external"
			var videoId = pathComponents[pathComponents.size - 1]

			// Rimuovi l'estensione ".m3u8" se presente
			val m3u8Index = videoId.indexOf(".m3u8")
			if (m3u8Index >= 0) {
				videoId = videoId.substring(0, m3u8Index)
			}

			Log.d("extractVideoId", "Video Id: $videoId")
			return videoId
		}
	} catch (e: MalformedURLException) {
		// TODO
	}
	return null
}

/**
 * Get the localized detail for the current app language.
 */
fun News.getLocalizedDetail(): Detail? = detail[Utils.getAppLanguage()]

/**
 * Get the localized contact info for the current app language.
 */
fun News.getLocalizedContactInfo(): ContactInfo? = contactInfo?.get(Utils.getAppLanguage())

/**
 * Get the localized videos for the current app language.
 */
fun News.getLocalizedVideos(): List<NewsVideo>? = videos?.get(Utils.getAppLanguage())

/**
 * Whether the news is important, that is, it has the "important" tag.
 */
val News.isImportant get(): Boolean = tags?.any { it.id == ID_TAG_IMPORTANT } ?: false

data class NewsResponse(
	@SerializedName("TotalResults")
	val totalResult: Int,
	@SerializedName("TotalPages")
	val totalPages: Int,
	@SerializedName("CurrentPage")
	val currentPage: Int,
	@SerializedName("PreviousPage")
	val previousPage: String?,
	@SerializedName("NextPage")
	val nextPage: String?,
	@SerializedName("Items")
	val news: List<News>
)
