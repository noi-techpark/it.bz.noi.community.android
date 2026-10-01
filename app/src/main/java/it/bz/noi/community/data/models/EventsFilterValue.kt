// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.data.models

import com.google.gson.annotations.SerializedName

data class TagsResponse(
	@SerializedName("Items")
	val tags: List<EventTag>
)

/**
 * Named EventTag (not Tag) to avoid clashing with the unrelated top-level
 * `Tag` class already declared in this package by News.kt.
 */
data class EventTag(
	@SerializedName("Id")
	val id: String,
	@SerializedName("TagName")
	val tagName: Map<String, String> = emptyMap(),
	@SerializedName("Types")
	val types: List<String> = emptyList(),
) {
	/**
	 * A tag's Types array can have more than one entry (e.g. "digital" has both
	 * "technologyfields" and "customtagging"). Only the first is used to decide
	 * its filter category: classifying by Types.contains(...) instead would let a
	 * dual-category tag match both the CustomTagging-OR-group and the
	 * TechnologyFields-OR-group at once, and AND-ing those two groups together
	 * could then silently collapse to just one group's constraint whenever that
	 * shared tag is selected (X AND (X OR Y) = X).
	 */
	val category: String?
		get() = types.firstOrNull()
}

fun EventTag.toFilterValue(language: String): FilterValue {
	val description = tagName[language] ?: tagName.values.firstOrNull() ?: id
	return FilterValue(key = id, type = category ?: "", desc = description)
}

enum class EventsFilterType(val category: String) {
	EVENT_TYPE("customtagging"),
	TECHNOLOGY_SECTOR("technologyfields")
}
