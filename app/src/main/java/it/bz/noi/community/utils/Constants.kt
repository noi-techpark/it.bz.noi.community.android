package it.bz.noi.community.utils

import android.icu.text.DateFormat
import android.icu.text.DateIntervalFormat
import android.icu.util.DateInterval
import java.text.SimpleDateFormat
import java.util.*

object Constants {

	/**
	 * various formatter/parser used
	 */
    fun getNoiCalendar(): Calendar = Calendar.getInstance(TimeZone.getTimeZone("Europe/Rome"), Locale.getDefault())

	fun getEndOfThisWeek(): Calendar {
		val calendar = getNoiCalendar()
		val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
		val daysToAdd = if (dayOfWeek == Calendar.SUNDAY) 0 else (8-dayOfWeek)
		calendar.add(Calendar.DATE, daysToAdd)
		return calendar
	}

	fun getEndOfThisMonth(): Calendar {
		val calendar = getNoiCalendar()
		calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE))
		return calendar
	}

    fun getServerDatetimeParser() = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Europe/Rome")
        calendar = getNoiCalendar()
    }
    fun startDateFormatter() = SimpleDateFormat("yyyy-MM-dd 00:00", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Europe/Rome")
        calendar = getNoiCalendar()
    }
    fun endDateFormatter() = SimpleDateFormat("yyyy-MM-dd 23:59", Locale.getDefault()).apply {
        timeZone = TimeZone.getTimeZone("Europe/Rome")
        calendar = getNoiCalendar()
    }

	fun getDateIntervalString(eventStartDate: String, eventEndDate: String): String {
		val startDate = getServerDatetimeParser().parse(eventStartDate)
		val endDate = getServerDatetimeParser().parse(eventEndDate)
		val dateInterval = DateInterval(startDate.time, endDate.time)

		val diFormatDays = DateIntervalFormat.getInstance(DateFormat.NUM_MONTH_DAY)
		// TODO DateIntervalFormat per gestire intervalli a cavallo d'anno (es. 31/12/2021 - 01/01/2022)
		return diFormatDays.format(dateInterval)
	}

	fun getHoursIntervalString(eventStartDate: String, eventEndDate: String): String {
		val startDate = getServerDatetimeParser().parse(eventStartDate)
		val endDate = getServerDatetimeParser().parse(eventEndDate)

		val endCal = Calendar.getInstance().apply {
			time = endDate
		}
		val endHour = Calendar.getInstance().apply {
			time = startDate
			set(Calendar.HOUR_OF_DAY, endCal.get(Calendar.HOUR_OF_DAY))
			set(Calendar.MINUTE, endCal.get(Calendar.MINUTE))
		}.time
		val hoursInterval = DateInterval(startDate.time, endHour.time)

		val diFormatHours = DateIntervalFormat.getInstance(DateFormat.HOUR24_MINUTE)
		return diFormatHours.format(hoursInterval)
	}
}
