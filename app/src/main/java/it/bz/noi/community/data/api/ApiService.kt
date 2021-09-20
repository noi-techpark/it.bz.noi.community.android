package it.bz.noi.community.data.api

import it.bz.noi.community.data.models.EventDetailsResponse
import it.bz.noi.community.data.models.EventsResponse
import retrofit2.http.*

/**
 * Interface for calling the different endpoints
 */
interface ApiService {
    @GET("v1/EventShort")
    suspend fun getEvents(
        @Query("onlyactive") onlyActive: Boolean = true,
        @Query("eventlocation") eventLocation: String = "NOI",
        @Query("pagenumber") pageNumber: Int = 1,
        @Query("pagesize") pageSize: Int = 20,
        @Query("startdate") startDate: String,
        @Query("enddate") endDate: String? = null,
        @Query("rawfilter") rawFilter: String?
    ): EventsResponse

    @GET("v1/EventShort/Detail/{id}")
    suspend fun getEventDetails(
        @Path("id") eventID: String
    ): EventDetailsResponse

    @GET("v1/EventShort/RoomMapping")
    suspend fun getRoomMapping(): Map<String, String>
}