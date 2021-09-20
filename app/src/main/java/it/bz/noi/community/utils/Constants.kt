package it.bz.noi.community.utils

import java.text.SimpleDateFormat
import java.util.*

object Constants {

    fun getNoiCalendar() = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.getDefault())

    fun getServerDatetimeParser() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Europe/Rome")
        calendar = getNoiCalendar()
    }
    fun startDateFormatter() = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Europe/Rome")
        calendar = getNoiCalendar()
    }
    fun endDateFormatter() = SimpleDateFormat("yyyy-MM-dd 23:59", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Europe/Rome")
        calendar = getNoiCalendar()
    }
    fun getLocalDateFormatter() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Europe/Rome")
        calendar = getNoiCalendar()
    }
    fun getLocalTimeFormatter() = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Europe/Rome")
        calendar = getNoiCalendar()
    }

    /**
     * Returns the month code based on passed month number (0 -> January, 11 -> December)
     */
    fun getMonthCode(month: Int): Int {
        return when (month) {
            0 -> 1
            1 -> 2
            2 -> 3
            3 -> 4
            4 -> 5
            5 -> 6
            6 -> 7
            7 -> 8
            8 -> 9
            9 -> 10
            10 -> 11
            11 -> 12
            else -> throw IllegalArgumentException("Month does not exist")
        }
    }
}