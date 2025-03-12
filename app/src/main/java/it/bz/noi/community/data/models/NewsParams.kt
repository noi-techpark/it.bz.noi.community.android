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
	var highlight: Boolean?,

	var selectedFilters: List<FilterValue> = emptyList()
)

/**
 * Factory for creating [NewsParams].
 */
fun NewsParams(nextPageNumber: Int, pageSize: Int, from: Date, moreHighlights: Boolean?, selectedFilters: List<FilterValue>) =
	NewsParams(
		startDate = DateUtils.parameterDateFormatter().format(from),
		pageSize = pageSize,
		pageNumber = nextPageNumber,
		language = Utils.getAppLanguage(),
		highlight = moreHighlights,
		selectedFilters = selectedFilters
	)

fun getNewsCountParams(from: Date, selectedFilters: List<FilterValue>) =
	NewsParams(
		startDate = DateUtils.parameterDateFormatter().format(from),
		pageSize = 1,
		pageNumber = 1,
		language = null,
		highlight = null,
		selectedFilters = selectedFilters
	)

fun NewsParams.getRawFilter(): String? {
	var rawFilter: String?  = null

	if (highlight != null) {
		rawFilter = if (highlight == true) {
			"eq(Highlight,\"true\")"
		} else {
			"or(eq(Highlight,\"false\"),isnull(Highlight))"
		}

		if (selectedFilters.isNotEmpty()) {
			rawFilter = "and($rawFilter,"
			val rawFiltersList = selectedFilters.map {
				"in(TagIds.[],\"${it.key}\")"
			}

			rawFilter += if (rawFiltersList.size == 1) {
				rawFiltersList[0]
			} else {
				rawFiltersList.joinToString(prefix = "or(", separator = ",", postfix = ")")
			}

			rawFilter += ")"
		}
	} else {
		if (selectedFilters.isNotEmpty()) {
			val rawFiltersList = selectedFilters.map {
				"in(TagIds.[],\"${it.key}\")"
			}

			rawFilter = if (rawFiltersList.size == 1) {
				rawFiltersList[0]
			} else {
				rawFiltersList.joinToString(prefix = "or(", separator = ",", postfix = ")")
			}
		}
	}

	return rawFilter
}

