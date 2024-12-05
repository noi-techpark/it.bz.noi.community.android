// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.data.api

import it.bz.noi.community.data.models.*
import retrofit2.http.*

/**
 * Interface for calling the different endpoints
 */
interface OpendatahubApiService {
    @GET("v1/EventShort")
    suspend fun getEvents(
		@Query("removenullvalues") removeNullValues: Boolean = true,
		@Query("optimizedates") optimizeDates: Boolean = true,
        @Query("eventlocation") eventLocation: String = "NOI",
        @Query("pagenumber") pageNumber: Int = 1,
        @Query("pagesize") pageSize: Int = 20,
        @Query("startdate") startDate: String,
        @Query("enddate") endDate: String? = null,
        @Query("rawfilter") rawFilter: String?,
		@Query("publishedon") publishedOn: String? = "noi-communityapp",
    ): EventsResponse

	@GET("v1/EventShort/{id}")
	suspend fun getEventDetails(
		@Path("id") eventID: String,
	): EventsResponse.Event

    /*@GET("v1/EventShort/Detail/{id}")
    suspend fun getEventDetails(
        @Path("id") eventID: String,
		@Query("optimizedates") optimizeDates: Boolean = true,
    ): EventDetailsResponse*/

    @GET("v1/EventShort/RoomMapping")
    suspend fun getRoomMapping(
		@Query("language") language: String?
	): Map<String, String>

	@GET("https://tourism.opendatahub.com/v1/EventShortTypes?rawfilter=or(eq(Type,\"TechnologyFields\"),and(eq(Type,\"CustomTagging\"),eq(Parent,\"EventType\")))")
	suspend fun getEventFilterValues(): List<MultiLangEventsFilterValue>

	@GET("v1/Article")
	suspend fun getNews(
		@Query("removenullvalues") removeNullValues: Boolean = true,
		@Query("articletype") endDate: String = "newsfeednoi",
		@Query("rawsort") rawFilter: String = "-ArticleDate",
		@Query("fields") fields: String = "Id,ArticleDate,Detail,ContactInfos,ImageGallery,ODHTags",
		@Query("pagesize") pageSize: Int,
		@Query("pagenumber") pageNumber: Int,
		@Query("startdate") startDate: String,
		@Query("language") language: String?,
		@Query("publishedon") publishedOn: String? = "noi-communityapp",
	): NewsResponse

	@GET("v1/Article/{id}")
	suspend fun getNewsDetails(
		@Path("id") newsId: String,
		@Query("removenullvalues") removeNullValues: Boolean = true,
		@Query("fields") fields: String = "Id,ArticleDate,Detail,ContactInfos,ImageGallery,ODHTags",
		@Query("language") language: String?
	): News
}
