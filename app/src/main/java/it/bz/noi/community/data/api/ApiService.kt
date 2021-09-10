package it.bz.noi.community.data.api

import it.bz.noi.community.data.models.EventsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("v1/EventShort")
    suspend fun getEvents(
        @Query("pagenumber") pageNumber: Int = 1,
        @Query("pagesize") pageSize: Int = 20,
        @Query("startdate") startDate: String,
        @Query("enddate") endDate: String? = null
    ): EventsResponse
}