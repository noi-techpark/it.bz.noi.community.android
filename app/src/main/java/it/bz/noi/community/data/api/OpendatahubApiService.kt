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

	companion object {
		private const val EVENT_FIELDS =
			"Id,DateBegin,DateEnd,Detail,EventUrls,ImageGallery,OrganizerInfos,VenueIds,EventDate"
	}

	@GET("v1/Event")
	suspend fun getEvents(
		@Query("removenullvalues") removeNullValues: Boolean = true,
		@Query("denormalize") denormalize: Boolean = true,
		// Filters out inactive occurrences (e.g. EventDate.Active = null, which
		// would otherwise be shown) and keeps the item count consistent with
		// TotalResults (verified live). Aligned with iOS.
		@Query("optimizedates") optimizeDates: Boolean = true,
		@Query("rawsort") rawSort: String = "DateBegin",
		@Query("pagenumber") pageNumber: Int = 1,
		@Query("pagesize") pageSize: Int = 20,
		@Query("begindate") beginDate: String,
		@Query("enddate") endDate: String? = null,
		@Query("rawfilter") rawFilter: String,
		@Query("publishedon") publishedOn: String? = "noi-communityapp",
		@Query("fields") fields: String = EVENT_FIELDS,
	): EventsResponse

	@GET("v1/Event/{id}")
	suspend fun getEventDetails(
		@Path("id") eventID: String,
		@Query("fields") fields: String = EVENT_FIELDS,
	): Event

	@GET("v1/Tag")
	suspend fun getEventFilterValues(
		@Query("validforentity") validForEntity: String = "event",
		// Per lo swagger ufficiale, "types" va passato come stringa unica separata
		// da virgola, non ripetuto come parametro: ripeterlo perde silenziosamente
		// dei tag (verificato dal vivo su entrambi gli ambienti).
		@Query("types") types: String = "customtagging,technologyfields",
		@Query("fields") fields: String = "Id,TagName,Types",
		@Query("pagesize") pageSize: Int = 0,
	): TagsResponse

	@GET("v1/Venue")
	suspend fun getVenues(
		@Query("idlist") idList: String,
		// Il wildcard "RoomDetails.[*].Shortname" non funziona su questo endpoint
		// (verificato dal vivo, restituisce un array vuoto): va richiesto
		// RoomDetails per intero ed estratto lato client solo quanto serve.
		@Query("fields") fields: String = "Id,Detail,RoomDetails",
		@Query("pagesize") pageSize: Int = 0,
	): VenuesResponse

	@GET("v1/Article")
	suspend fun getNews(
		@Query("removenullvalues") removeNullValues: Boolean = true,
		@Query("articletype") endDate: String = "newsfeednoi",
		@Query("rawsort") rawSort: String = "-ArticleDate",
		@Query("fields") fields: String = "Id,ArticleDate,Detail,ContactInfos,ImageGallery,VideoItems,ODHTags,Highlight",
		@Query("pagesize") pageSize: Int,
		@Query("pagenumber") pageNumber: Int,
		@Query("startdate") startDate: String,
		@Query("language") language: String?,
		@Query("publishedon") publishedOn: String? = "noi-communityapp",
		@Query("rawfilter") rawFilter: String?,
	): NewsResponse

	@GET("v1/Article/{id}")
	suspend fun getNewsDetails(
		@Path("id") newsId: String,
		@Query("removenullvalues") removeNullValues: Boolean = true,
		@Query("fields") fields: String = "Id,ArticleDate,Detail,ContactInfos,ImageGallery,VideoItems,ODHTags",
		@Query("language") language: String?
	): News

	@GET("https://tourism.opendatahub.com/v1/Tag?validforentity=article&types=noicommunitycategory&fields=Id,TagName&pagesize=0")
	suspend fun getNewsFilterValues(): NewsFilterResponse

	@GET("v1/Article")
	suspend fun getNewsCount(
		@Query("removenullvalues") removeNullValues: Boolean = true,
		@Query("articletype") endDate: String = "newsfeednoi",
		@Query("fields") fields: String = "Id",
		@Query("pagesize") pageSize: Int = 1,
		@Query("pagenumber") pageNumber: Int = 1,
		@Query("startdate") startDate: String,
		@Query("publishedon") publishedOn: String? = "noi-communityapp",
		@Query("rawfilter") rawFilter: String?,
	): NewsResponse
}
