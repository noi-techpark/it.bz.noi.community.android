package it.bz.noi.community.data.models

import com.google.gson.annotations.SerializedName

data class VimeoResponse(
	@SerializedName("video_id")
	val id: Int,
	@SerializedName("thumbnail_url")
	val thumbnailUrl: String?,
	@SerializedName("thumbnail_url_with_play_button")
	val thumbnailUrlWithPlayButton: String?
)
