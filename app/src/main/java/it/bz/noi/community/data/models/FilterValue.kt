// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.data.models

data class FilterValue(
	val key: String,
	val type: String,
	val desc: String,
	var checked: Boolean = false
)
