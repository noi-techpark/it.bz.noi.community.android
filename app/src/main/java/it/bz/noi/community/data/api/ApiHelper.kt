package it.bz.noi.community.data.api

import it.bz.noi.community.data.models.EventsResponse
import it.bz.noi.community.data.models.UrlParams
import it.bz.noi.community.data.models.getEventTypeRawFilter
import it.bz.noi.community.data.models.getRawFilter

class ApiHelper(private val apiService: ApiService) {
    suspend fun getEvents(urlParams: UrlParams): EventsResponse {
        val eventTypeRawFilter = urlParams.getEventTypeRawFilter()
        val techSectorRawFilter = urlParams.getRawFilter()
        val rawFilter = eventTypeRawFilter?.let {
            techSectorRawFilter?.let {
                arrayOf(eventTypeRawFilter, techSectorRawFilter)
            } ?: arrayOf(eventTypeRawFilter)
        } ?: techSectorRawFilter?.let {
            arrayOf(techSectorRawFilter)
        }
        return apiService.getEvents(startDate = urlParams.startDate, endDate = urlParams.endDate, rawFilter = rawFilter)
    }
    suspend fun getEventDetails(eventID: String) = apiService.getEventDetails(eventID)
}