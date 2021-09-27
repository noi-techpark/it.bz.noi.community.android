package it.bz.noi.community.data.models

/**
 * The different time ranges for filtering the events
 * ALL -> All the events starting from TODAY --> startDate has today date value
 * TODAY -> The events of today --> startDate and endDate both set to today date
 * THIS_WEEK -> From today to the end of the current week
 * THIS_MONTH -> From today to the end of the current month
 */
enum class TimeRange {
	ALL,
	TODAY,
	THIS_WEEK,
	THIS_MONTH
}
