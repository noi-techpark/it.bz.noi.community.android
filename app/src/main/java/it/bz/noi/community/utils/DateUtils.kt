package it.bz.noi.community.utils

import android.icu.text.DateFormat
import android.icu.text.DateIntervalFormat
import android.icu.util.DateInterval
import it.bz.noi.community.utils.NOIDateParser.Companion.LOCALE_EN_US_POSIX
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

	/*
	 * Last day of week depends on Locale, i.e.:
	 * - IT: week goes from MONDAY (id=2) to SUNDAY (id=1)
	 * - US: week goes from SUNDAY (id=1) to SATURDAY (id=7)
	 */
	fun lastDayOfCurrentWeek(): Calendar {
		return Calendar.getInstance().apply {
			// The number of days per week depends on the calendar type
			val numDaysInAWeek = getActualMaximum(Calendar.DAY_OF_WEEK)

			// Day ids are in range [1,7], so we have to map them to range [0,6]
			// (this is why we have the two '-1' in this computation) and then
			// re-map the result to range [1,7] (therefore the last '+1')
			val lastWeekday = ((firstDayOfWeek-1) + (numDaysInAWeek-1) )%numDaysInAWeek +1
			set(Calendar.DAY_OF_WEEK, lastWeekday)
		}
	}

	fun lastDayOfCurrentMonth(): Calendar {
		return Calendar.getInstance().apply {
			set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
		}
	}

	fun Calendar.startOfDay(): Date {
		set(Calendar.HOUR_OF_DAY, 0)
		set(Calendar.MINUTE, 0)
		return time
	}

	fun Calendar.endOfDay(): Date {
		set(Calendar.HOUR_OF_DAY, 23)
		set(Calendar.MINUTE, 59)
		return time
	}

	fun parameterDateFormatter() = SimpleDateFormat("yyyy-MM-dd HH:mm", LOCALE_EN_US_POSIX).apply {
		timeZone = TimeZone.getTimeZone("Europe/Rome")
	}

	fun getDateIntervalString(startDate: Date, endDate: Date): String {
		val dateInterval = DateInterval(startDate.time, endDate.time)

		val diFormatDays = DateIntervalFormat.getInstance(DateFormat.NUM_MONTH_DAY)
		// TODO DateIntervalFormat per gestire intervalli a cavallo d'anno (es. 31/12/2021 - 01/01/2022)
		return diFormatDays.format(dateInterval)
	}

	fun getHoursIntervalString(startDate: Date, endDate: Date): String {
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
