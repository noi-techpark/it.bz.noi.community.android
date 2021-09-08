package it.bz.noi.community.data.api

class ApiHelper(private val apiService: ApiService) {
    suspend fun getEvents() = apiService.getEvents()
}