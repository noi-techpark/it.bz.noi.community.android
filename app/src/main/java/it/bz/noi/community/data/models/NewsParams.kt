// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.data.models

import it.bz.noi.community.utils.DateUtils
import it.bz.noi.community.utils.Utils
import java.util.Date

data class NewsParams(
	var startDate: String,
	var pageSize: Int = 10,
	var pageNumber: Int,
	var language: String?,
	var highlight: Boolean = false,

	var selectedFilters: List<FilterValue> = emptyList()
)

/**
 * Factory for creating [NewsParams].
 */
fun NewsParams(nextPageNumber: Int, pageSize: Int, from: Date, moreHighlights: Boolean) =
	NewsParams(
		startDate = DateUtils.parameterDateFormatter().format(from),
		pageSize = pageSize,
		pageNumber = nextPageNumber,
		language = Utils.getAppLanguage(),
		highlight = moreHighlights
	)

fun NewsParams.getRawFilter(): String {
	var rawFilter: String?

	if (highlight) {
		rawFilter = "eq(Highlight,\"true\")"
	} else {
		rawFilter = "or(eq(Highlight,\"false\"),isnull(Highlight))"
	}

	// FIXME
	if (selectedFilters.isNotEmpty()) {
		var tagFilter = "and ("
		val rawFiltersList = selectedFilters.map {
			"in(TagIds.[],\"${it.key}\")"
		}

		if (rawFiltersList.size == 1) {
			tagFilter += rawFiltersList[0]
		} else {
			tagFilter += rawFiltersList.joinToString(prefix = "or(", separator = ",", postfix = ")")
		}

		tagFilter += ")"

		rawFilter += tagFilter
	}

	return rawFilter
}

