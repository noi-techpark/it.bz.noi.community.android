package it.bz.noi.community.data.repository

import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.models.EventsParams
import it.bz.noi.community.data.models.NewsParams

class MainRepository(private val apiHelper: ApiHelper) {
	// EVENTS
	suspend fun getEvents(eventsParams: EventsParams) = apiHelper.getEvents(eventsParams)
	suspend fun getEventDetails(eventID: String) = apiHelper.getEventDetails(eventID)
	suspend fun getEventFilterValues() = apiHelper.getEventFilterValues()

	// ROOMS
	suspend fun getRoomMapping(language: String?) = apiHelper.getRoomMapping(language)

	// NEWS
	suspend fun getNews(newsParams: NewsParams) = apiHelper.getNews(newsParams)
	suspend fun getNewsDetails(newsId: String, language: String?) =
		apiHelper.getNewsDetails(newsId = newsId, language = language)
}
