// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.data.models

import com.google.gson.annotations.SerializedName
import it.bz.noi.community.utils.Utils

data class NewsFilterResponse(
	@SerializedName("Items")
	val filters: List<MultiLangNewsFilterValue>
)

data class MultiLangNewsFilterValue(
	@SerializedName("Id")
	val id: String,
    @SerializedName("Types")
    val types: List<String>,
    @SerializedName("TagName")
    val tagName: TagName
)

data class TagName(
    @SerializedName("de")
    val de: String?,
    @SerializedName("en")
    val en: String?,
    @SerializedName("it")
    val it: String?
)

fun MultiLangNewsFilterValue.toFilterValue(language: String): FilterValue {
	val description = when (language) {
		Utils.ITALIAN -> tagName.it
		Utils.GERMAN -> tagName.de
		else -> tagName.en
	}
	val type = if (types.isEmpty()) "N/D" else types[0]
	return FilterValue(id, type, description ?: "N/D")
}
