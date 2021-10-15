package it.bz.noi.community.data.api

import it.bz.noi.community.data.models.EventsResponse
import it.bz.noi.community.data.models.UrlParams
import it.bz.noi.community.data.models.getRawFilter

class ApiHelper(private val apiService: ApiService) {
    suspend fun getEvents(urlParams: UrlParams): EventsResponse {
        return apiService.getEvents(startDate = urlParams.startDate, endDate = urlParams.endDate, rawFilter = urlParams.getRawFilter())
    }
    suspend fun getEventDetails(eventID: String) = apiService.getEventDetails(eventID)
    suspend fun getRoomMapping(language: String?) = apiService.getRoomMapping(language)
}
