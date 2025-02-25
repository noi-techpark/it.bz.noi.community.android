// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.data.api

import it.bz.noi.community.data.models.*

class ApiHelper(
	private val opendatahubApiService: OpendatahubApiService,
	private val communityApiService: CommunityApiService,
	private val vimeoApiService: VimeoApiService) {

	// EVENTS
	suspend fun getEvents(eventsParams: EventsParams): EventsResponse = opendatahubApiService.getEvents(
		startDate = eventsParams.startDate,
		endDate = eventsParams.endDate,
		rawFilter = eventsParams.getRawFilter()
	)

	suspend fun getEventDetails(eventID: String) = opendatahubApiService.getEventDetails(eventID)
	suspend fun getEventFilterValues() = opendatahubApiService.getEventFilterValues()

	// ROOMS
	suspend fun getRoomMapping(language: String?) = opendatahubApiService.getRoomMapping(language)

	// NEWS
	suspend fun getNews(newsParams: NewsParams): NewsResponse =
		opendatahubApiService.getNews(
			startDate = newsParams.startDate,
			pageNumber = newsParams.pageNumber,
			pageSize = newsParams.pageSize,
			language = newsParams.language,
			rawFilter = newsParams.getRawFilter()
		)

	suspend fun getNewsDetails(newsId: String, language: String?) =
		opendatahubApiService.getNewsDetails(newsId = newsId, language = language)

	suspend fun getVideoThumbnail(url: String) = vimeoApiService.getVideoThumbnail(url = url)

	// CONTACTS
	suspend fun getAccounts(accessToken: String): AccountsResponse = communityApiService.getAccounts(accessToken)
	suspend fun getContacts(accessToken: String): ContactResponse = communityApiService.getContacts(accessToken)
}
