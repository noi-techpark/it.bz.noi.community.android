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
	var highlight: Boolean = false
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

fun NewsParams.getRawFilter(): String =
	if (highlight) "eq(Highlight,\"true\")" else "or(eq(Highlight,\"false\"),isnull(Highlight))"
