// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.data.models

data class NewsParams(
	var startDate: String,
	var pageSize: Int = 10,
	var pageNumber: Int,
	var language: String?,
	var highlight: Boolean = false
)

fun NewsParams.getRawFilter(): String {
	if (highlight)
		return "eq(Highlight,\"true\")"
	return "or(eq(Highlight,\"false\"),isnull(Highlight))"
}
