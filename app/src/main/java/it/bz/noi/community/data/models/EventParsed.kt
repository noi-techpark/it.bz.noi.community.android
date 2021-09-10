package it.bz.noi.community.data.models

import android.text.SpannableStringBuilder

data class EventParsed(
    val eventId: String,
    val name: String,
    val location: String,
    val days: String,
    val month: String,
    val time: String
)