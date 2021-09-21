package it.bz.noi.community.data.models

/**
 * used for passing parsed data from adapter to shared element transition in event details
 */
data class EventParsed(
    val eventId: String,
    val name: String,
    val location: String,
    val days: String,
    val month: String,
    val time: String,
    val imageUrl: String?
)