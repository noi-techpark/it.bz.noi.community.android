package it.bz.noi.community.data.models

import com.google.gson.annotations.SerializedName
import it.bz.noi.community.utils.Utils

data class MultiLangFilterValue(
	@SerializedName("Id")
	val id: String,
    @SerializedName("Key")
    val key: String,
    @SerializedName("Type")
    val type: String,
	@SerializedName("Parent")
	val parent: String,
	@SerializedName("Bitmask")
	val bitmask: String,
    @SerializedName("TypeDesc")
    val desc: TypeDesc
)

data class TypeDesc(
    @SerializedName("de")
    val de: String,
    @SerializedName("en")
    val en: String,
    @SerializedName("it")
    val it: String
)

data class FilterValue(
	val key: String,
	val type: String,
	val desc: String,
	var checked: Boolean = false
)

fun MultiLangFilterValue.toFilterValue(language: String): FilterValue {
	val description = when (language) {
		Utils.ITALIAN -> desc.it
		Utils.GERMAN -> desc.de
		else -> desc.en
	}
	return FilterValue(key, type, description)
}

enum class FilterType(val typeDesc: String) {
	EVENT_TYPE("CustomTagging"),
	TECHNOLOGY_SECTOR("TechnologyFields")
}