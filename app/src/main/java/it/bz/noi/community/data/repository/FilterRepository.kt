// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.data.repository

import android.app.Application
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.bz.noi.community.R
import it.bz.noi.community.data.models.EventTag
import it.bz.noi.community.data.models.MultiLangNewsFilterValue
import java.io.BufferedReader
import java.io.File
import java.lang.reflect.Type

interface FilterRepository {

	// Event filters
	fun saveEventFilters(filters: List<EventTag>)
	fun loadEventFilters(): List<EventTag>

	// News filter
	fun saveNewsFilters(filters: List<MultiLangNewsFilterValue>)
	fun loadNewsFilters(): List<MultiLangNewsFilterValue>

}

class JsonFilterRepository(val app: Application) : FilterRepository {

	// EVENTS

	override fun saveEventFilters(filters: List<EventTag>) {
		File(app.filesDir, eventFiltersFileName).bufferedWriter().use {
			try {
				val typeOfT: Type = object : TypeToken<List<EventTag?>?>() {}.type
				it.write(Gson().toJson(filters, typeOfT).toString())
			} catch (e: Exception) {
				Log.e(TAG, e.stackTraceToString())
			}

		}
	}

	override fun loadEventFilters(): List<EventTag> {
		var filters: List<EventTag>? = null
		val filtersFile: Array<File> = File(app.filesDir.path).listFiles { _, name -> name == eventFiltersFileName  }
		if (filtersFile.size == 1) {
			filters = parseEventFilters(filtersFile[0])
		}

		if (!filters.isNullOrEmpty())
			return filters

		return parseDefaultEventFilters()
	}

	private fun parseEventFilters(filtersFile: File): List<EventTag> =
		filtersFile.bufferedReader().parseEventFilters()

	private fun parseDefaultEventFilters(): List<EventTag> =
		app.resources.openRawResource(R.raw.filters).bufferedReader().parseEventFilters()

	private fun BufferedReader.parseEventFilters(): List<EventTag> = use {
		return try {
			val typeOfT: Type = object : TypeToken<List<EventTag?>?>() {}.type
			Gson().fromJson(this, typeOfT)
		} catch (e: Exception) {
			Log.e(TAG, e.stackTraceToString())
			emptyList()
		}
	}

	// NEWS

	override fun saveNewsFilters(filters: List<MultiLangNewsFilterValue>) {
		File(app.filesDir, newsFiltersFileName).bufferedWriter().use {
			try {
				val typeOfT: Type = object : TypeToken<List<MultiLangNewsFilterValue?>?>() {}.type
				it.write(Gson().toJson(filters, typeOfT).toString())
			} catch (e: Exception) {
				Log.e(TAG, e.stackTraceToString())
			}

		}
	}

	override fun loadNewsFilters(): List<MultiLangNewsFilterValue> {
		var filters: List<MultiLangNewsFilterValue>? = null
		val filtersFile: Array<File> = File(app.filesDir.path).listFiles { _, name -> name == newsFiltersFileName  }
		if (filtersFile.size == 1) {
			filters = parseNewsFilters(filtersFile[0])
		}

		if (!filters.isNullOrEmpty())
			return filters

		return parseDefaultNewsFilters()
	}

	private fun parseNewsFilters(filtersFile: File): List<MultiLangNewsFilterValue> =
		filtersFile.bufferedReader().parseNewsFilters()

	private fun parseDefaultNewsFilters(): List<MultiLangNewsFilterValue> =
		app.resources.openRawResource(R.raw.news_filters).bufferedReader().parseNewsFilters()

	private fun BufferedReader.parseNewsFilters(): List<MultiLangNewsFilterValue> = use {
		return try {
			val typeOfT: Type = object : TypeToken<List<MultiLangNewsFilterValue?>?>() {}.type
			Gson().fromJson(this, typeOfT)
		} catch (e: Exception) {
			Log.e(TAG, e.stackTraceToString())
			emptyList()
		}
	}

	companion object {
		private const val eventFiltersFileName: String = "filters.json"
		private const val newsFiltersFileName: String = "news_filters.json"
		private const val TAG = "JsonFilterRepository"
	}
}
