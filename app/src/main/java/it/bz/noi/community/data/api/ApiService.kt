package it.bz.noi.community.data.api

import it.bz.noi.community.data.models.EventsResponse
import retrofit2.http.GET

interface ApiService {
    @GET("v1/EventShort")
    suspend fun getEvents(): EventsResponse
}