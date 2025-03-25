// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.data.models

import android.content.Context
import com.google.gson.annotations.SerializedName
import it.bz.noi.community.R
import it.bz.noi.community.utils.Utils

data class NewsFilterResponse(
	@SerializedName("Items")
	val filters: List<MultiLangNewsFilterValue>
)

data class MultiLangNewsFilterValue(
	@SerializedName("Id")
	val id: String,
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

fun MultiLangNewsFilterValue.toFilterValue(language: String, context: Context): FilterValue {
	val description = when (language) {
		Utils.ITALIAN -> tagName.it
		Utils.GERMAN -> tagName.de
		else -> tagName.en
	}
	return FilterValue(id, "", description ?: context.resources.getString(R.string.label_no_value))
}
