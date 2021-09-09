package it.bz.noi.community.utils

import java.text.SimpleDateFormat
import java.util.*

object Constants {
    fun getServerDatetimeParser(): SimpleDateFormat {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    }

    fun getServerDateParser(): SimpleDateFormat {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    }
}