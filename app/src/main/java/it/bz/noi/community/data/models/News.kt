package it.bz.noi.community.data.models

import com.google.gson.annotations.SerializedName
import it.bz.noi.community.utils.Utils
import java.util.*

data class News(
	@SerializedName("Id")
	val id: String,

	@SerializedName("ArticleDate")
	val date: Date,

	@SerializedName("Detail.de.Title")
	val titleDE: String?,
	@SerializedName("Detail.en.Title")
	val titleEN: String?,
	@SerializedName("Detail.it.Title")
	val titleIT: String?,

	@SerializedName("Detail.de.AdditionalText") // FIXME da confermare
	val abstractDE: String?,
	@SerializedName("Detail.en.AdditionalText") // FIXME da confermare
	val abstractEN: String?,
	@SerializedName("Detail.it.AdditionalText") // FIXME da confermare
	val abstractIT: String?,

	@SerializedName("Detail.de.BaseText")
	val textDE: String?,
	@SerializedName("Detail.en.BaseText")
	val textEN: String?,
	@SerializedName("Detail.it.BaseText")
	val textIT: String?,

	@SerializedName("ContactInfos.de.CompanyName")
	val publisherDE: String?,
	@SerializedName("ContactInfos.en.CompanyName")
	val publisherEN: String?,
	@SerializedName("ContactInfos.it.CompanyName")
	val publisherIT: String?,

	@SerializedName("ContactInfos.de.Logo")
	val logoDE: String?,
	@SerializedName("ContactInfos.en.Logo")
	val logoEN: String?,
	@SerializedName("ContactInfos.it.Logo")
	val logoIT: String?,

	@SerializedName("ContactInfos.de.Url")
	val externalLinkDE: String?,
	@SerializedName("ContactInfos.en.Url")
	val externalLinkEN: String?,
	@SerializedName("ContactInfos.it.Url")
	val externalLinkIT: String?,

	@SerializedName("ContactInfos.de.Email")
	val emailDE: String?,
	@SerializedName("ContactInfos.en.Email")
	val emailEN: String?,
	@SerializedName("ContactInfos.it.Email")
	val emailIT: String?,

	@SerializedName("ImageGallery")
	val images: List<NewsImage>?
)

data class NewsImage(
	@SerializedName("ImageUrl")
	val url: String?
)

fun News.getTitle(): String? {
	return when (Locale.getDefault().language) {
		Utils.ITALIAN -> titleIT
		Utils.GERMAN -> titleDE
		else -> titleEN
	}
}

fun News.getAbstract(): String? {
	return when (Locale.getDefault().language) {
		Utils.ITALIAN -> abstractIT
		Utils.GERMAN -> abstractDE
		else -> abstractEN
	}
}

fun News.getText(): String? {
	return when (Locale.getDefault().language) {
		Utils.ITALIAN -> textIT
		Utils.GERMAN -> textDE
		else -> textEN
	}
}

fun News.getPublisher(): String? {
	return when (Locale.getDefault().language) {
		Utils.ITALIAN -> publisherIT
		Utils.GERMAN -> publisherDE
		else -> publisherEN
	}
}

fun News.getLogo(): String? {
	return when (Locale.getDefault().language) {
		Utils.ITALIAN -> logoIT
		Utils.GERMAN -> logoDE
		else -> logoEN
	}
}

fun News.getExternalLink(): String? {
	return when (Locale.getDefault().language) {
		Utils.ITALIAN -> externalLinkIT
		Utils.GERMAN -> externalLinkDE
		else -> externalLinkEN
	}
}

fun News.getEmail(): String? {
	return when (Locale.getDefault().language) {
		Utils.ITALIAN -> emailIT
		Utils.GERMAN -> emailDE
		else -> emailEN
	}
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
	val news: News
)
