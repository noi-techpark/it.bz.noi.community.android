package it.bz.noi.community.models

data class Event(
    val eventId: Long,
    val name: String,
    val location: String,
    val startDate: String,
    val endDate: String
)