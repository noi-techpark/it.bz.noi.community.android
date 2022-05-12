package it.bz.noi.community.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Format event [Date] to [String] and vice-versa.
 */
interface NOIDateParserI {
	fun parse(string: String): Date
}

class NOIDateParser : NOIDateParserI {

	private val df1 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", LOCALE_EN_US_POSIX).apply {
		timeZone = TimeZone.getTimeZone("Europe/Rome")
	}

	private val df2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", LOCALE_EN_US_POSIX).apply {
		timeZone = TimeZone.getTimeZone("Europe/Rome")
	}

	private val df3 =
		SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSZZZ", LOCALE_EN_US_POSIX).apply {
			timeZone = TimeZone.getTimeZone("Europe/Rome")
		}

	private val dateFormatters = listOf(df1, df2, df3)

	override fun parse(dateStr: String): Date {

		for (formatter in dateFormatters) {
			try {
				return formatter.parse(dateStr)
			} catch (ignore: ParseException) {
			}
		}
		throw Exception("Cannot parse string $dateStr")
	}

	companion object {
		val LOCALE_EN_US_POSIX = Locale("en_US_POSIX")
	}

}
