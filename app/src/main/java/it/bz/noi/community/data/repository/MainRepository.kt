package it.bz.noi.community.data.repository

import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.models.UrlParams

class MainRepository(private val apiHelper: ApiHelper) {
    suspend fun getEvents(urlParams: UrlParams) = apiHelper.getEvents(urlParams)
    suspend fun getEventDetails(eventID: String) = apiHelper.getEventDetails(eventID)
    suspend fun getRoomMapping(language: String?) = apiHelper.getRoomMapping(language)
}
