package it.bz.noi.community.utils

import java.text.SimpleDateFormat
import java.util.*

object Constants {
    fun getServerDatetimeParser() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    fun getServerDateParser() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    fun getLocalDateFormatter() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    fun getLocalTimeFormatter() = SimpleDateFormat("HH:mm", Locale.getDefault())

    /**
     * Returns the month code based on passed month number (0 -> January, 11 -> December)
     */
    fun getMonthCode(month: Int): String {
        return when (month) {
            0 -> "JAN"
            1 -> "FEB"
            2 -> "MAR"
            3 -> "APR"
            4 -> "MAY"
            5 -> "JUN"
            6 -> "JUL"
            7 -> "AUG"
            8 -> "SEP"
            9 -> "OCT"
            10 -> "NOV"
            11 -> "DEC"
            else -> throw IllegalArgumentException("Month does not exist")
        }
    }
}