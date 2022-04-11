package it.bz.noi.community.data.models

import com.google.gson.annotations.SerializedName
import it.bz.noi.community.utils.Utils
import java.util.*

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
)

data class Tag(
	@SerializedName("Id")
	val id: String?
)

data class Detail(
	@SerializedName("Title")
	val title: String?,
	@SerializedName("AdditionalText")
	val abstract: String?,
	@SerializedName("BaseText")
	val text: String?,
)

data class ContactInfo(
	@SerializedName("CompanyName")
	val publisher: String?,
	@SerializedName("LogoUrl")
	val logo: String?,
	@SerializedName("Url")
	val externalLink: String?,
	@SerializedName("Email")
	val email: String?,
)

data class NewsImage(
	@SerializedName("ImageUrl")
	val url: String?
)

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
