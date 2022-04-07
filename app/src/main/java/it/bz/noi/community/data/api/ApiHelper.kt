package it.bz.noi.community.data.api

import it.bz.noi.community.data.models.EventsResponse
import it.bz.noi.community.data.models.NewsResponse
import it.bz.noi.community.data.models.UrlParams
import it.bz.noi.community.data.models.getRawFilter

class ApiHelper(private val apiService: ApiService) {
	// EVENTS
	suspend fun getEvents(urlParams: UrlParams): EventsResponse = apiService.getEvents(
		startDate = urlParams.startDate,
		endDate = urlParams.endDate,
		rawFilter = urlParams.getRawFilter()
	)

	suspend fun getEventDetails(eventID: String) = apiService.getEventDetails(eventID)
	suspend fun getEventFilterValues() = apiService.getEventFilterValues()

	// ROOMS
	suspend fun getRoomMapping(language: String?) = apiService.getRoomMapping(language)

	// NEWS
	suspend fun getNews(startDate: String, pageNumber: Int, language: String?): NewsResponse =
		apiService.getNews(startDate = startDate, pageNumber = pageNumber, language = language)

	suspend fun getNewsDetails(newsId: String, language: String?) =
		apiService.getNewsDetails(newsId = newsId, language = language)
}
