package it.bz.noi.community.data.api

import it.bz.noi.community.data.models.*

class ApiHelper(private val apiService: ApiService) {
	// EVENTS
	suspend fun getEvents(eventsParams: EventsParams): EventsResponse = apiService.getEvents(
		startDate = eventsParams.startDate,
		endDate = eventsParams.endDate,
		rawFilter = eventsParams.getRawFilter()
	)

	suspend fun getEventDetails(eventID: String) = apiService.getEventDetails(eventID)
	suspend fun getEventFilterValues() = apiService.getEventFilterValues()

	// ROOMS
	suspend fun getRoomMapping(language: String?) = apiService.getRoomMapping(language)

	// NEWS
	suspend fun getNews(newsParams: NewsParams): NewsResponse =
		apiService.getNews(
			startDate = newsParams.startDate,
			pageNumber = newsParams.pageNumber,
			pageSize = newsParams.pageSize,
			language = newsParams.language
		)

	suspend fun getNewsDetails(newsId: String, language: String?) =
		apiService.getNewsDetails(newsId = newsId, language = language)
}
