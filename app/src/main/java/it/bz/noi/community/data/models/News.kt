package it.bz.noi.community.data.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import it.bz.noi.community.utils.Utils
import kotlinx.parcelize.Parcelize
import java.util.*

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
	val images: List<NewsImage>?,
	@SerializedName("ODHTags")
	val tags: List<Tag>?
) : Parcelable

@Parcelize
data class Tag(
	@SerializedName("Id")
	val id: String?
) : Parcelable

@Parcelize
data class Detail(
	@SerializedName("Title")
	val title: String?,
	@SerializedName("AdditionalText")
	val abstract: String?,
	@SerializedName("BaseText")
	val text: String?,
) : Parcelable

@Parcelize
data class ContactInfo(
	@SerializedName("CompanyName")
	val publisher: String?,
	@SerializedName("LogoUrl")
	val logo: String?,
	@SerializedName("Url")
	val externalLink: String?,
	@SerializedName("Email")
	val email: String?,
) : Parcelable

@Parcelize
data class NewsImage(
	@SerializedName("ImageUrl")
	val url: String?
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
