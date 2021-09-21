package it.bz.noi.community.utils

import android.content.Context
import it.bz.noi.community.R

class ResourcesHelper(private val context: Context) {
	val allLabel = context.getString(R.string.time_filter_all)
	val todayLabel = context.getString(R.string.time_filter_today)
	val thisWeekLabel = context.getString(R.string.time_filter_this_week)
	val thisMonthLabel = context.getString(R.string.time_filter_this_month)
}
