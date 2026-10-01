// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.data.models

import com.google.gson.annotations.SerializedName

data class VenuesResponse(
	@SerializedName("Items")
	val venues: List<Venue>
)

data class Venue(
	@SerializedName("Id")
	val id: String,
	@SerializedName("Detail")
	val detail: Map<String, VenueDetail>? = null,
	@SerializedName("RoomDetails")
	val roomDetails: List<RoomDetail>? = null,
) {
	data class VenueDetail(
		@SerializedName("Title")
		val title: String? = null,
	)

	data class RoomDetail(
		@SerializedName("Id")
		val id: String,
		@SerializedName("Shortname")
		val shortname: String? = null,
		@SerializedName("Mapping")
		val mapping: Mapping? = null,
	) {
		data class Mapping(
			@SerializedName("maps")
			val maps: Maps? = null,
		) {
			data class Maps(
				@SerializedName("roommapping")
				val roomMapping: String? = null,
			)
		}
	}
}
