package it.bz.noi.community.data.repository

import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.models.NewsResponse
import it.bz.noi.community.data.models.UrlParams

class MainRepository(private val apiHelper: ApiHelper) {
	// EVENTS
    suspend fun getEvents(urlParams: UrlParams) = apiHelper.getEvents(urlParams)
    suspend fun getEventDetails(eventID: String) = apiHelper.getEventDetails(eventID)
	suspend fun getEventFilterValues() = apiHelper.getEventFilterValues()

	// ROOMS
	suspend fun getRoomMapping(language: String?) = apiHelper.getRoomMapping(language)

	// NEWS
	suspend fun getNews(startDate: String, pageNumber: Int, language: String?) =
		apiHelper.getNews(startDate = startDate, pageNumber = pageNumber, language = language)
	suspend fun getNewsDetails(newsId: String, language: String?) =
		apiHelper.getNewsDetails(newsId = newsId, language = language)
}
