package it.bz.noi.community.data.repository

import it.bz.noi.community.data.api.ApiHelper

class MainRepository(private val apiHelper: ApiHelper) {
    suspend fun getEvents() = apiHelper.getEvents()
}