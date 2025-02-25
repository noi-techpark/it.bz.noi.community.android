// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later
package it.bz.noi.community.data.api

import it.bz.noi.community.data.models.VimeoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface VimeoApiService {
	@GET("api/oembed.json")
	suspend fun getVideoThumbnail(
		@Query("url") url: String
	): Response<VimeoResponse>
}
