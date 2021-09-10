package it.bz.noi.community.data.api

import it.bz.noi.community.data.models.EventsResponse
import it.bz.noi.community.data.models.UrlParams

class ApiHelper(private val apiService: ApiService) {
    suspend fun getEvents(urlParams: UrlParams): EventsResponse {
        return apiService.getEvents(startDate = urlParams.startDate, endDate = urlParams.endDate)
    }
    suspend fun getEventDetails(eventID: String) = apiService.getEventDetails(eventID)
}