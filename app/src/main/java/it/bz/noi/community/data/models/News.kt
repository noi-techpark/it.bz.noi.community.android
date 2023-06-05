// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.data.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import it.bz.noi.community.utils.Utils
import kotlinx.parcelize.Parcelize
import java.util.*

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
	val contactInfo: Map<String, ContactInfo>,
	@SerializedName("ImageGallery")
	val images: List<NewsImage>? = null,
	@SerializedName("ODHTags")
	val tags: List<Tag>? = null
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

fun News.getDetail(): Detail? {
	return detail[Utils.getAppLanguage()]
}

fun News.getContactInfo(): ContactInfo? {
	return contactInfo[Utils.getAppLanguage()]
}

fun News.hasImportantFlag(): Boolean {
	return tags != null && tags.filter { it.id == "important" }.isNotEmpty()
}

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
