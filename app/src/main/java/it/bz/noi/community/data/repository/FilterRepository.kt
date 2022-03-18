package it.bz.noi.community.data.repository

import android.app.Application
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.bz.noi.community.R
import it.bz.noi.community.data.models.MultiLangFilterValue
import java.io.BufferedReader
import java.io.File
import java.lang.reflect.Type

interface FilterRepository {

	fun saveFilters(filters: List<MultiLangFilterValue>)
	fun loadFilters(): List<MultiLangFilterValue>

}

class JsonFilterRepository(val app: Application) : FilterRepository {

	override fun saveFilters(filters: List<MultiLangFilterValue>) {
		File(app.filesDir, filtersFileName).bufferedWriter().use {
			try {
				val typeOfT: Type = object : TypeToken<List<MultiLangFilterValue?>?>() {}.type
				it.write(Gson().toJson(filters, typeOfT).toString())
			} catch (e: Exception) {
				Log.e(TAG, e.stackTraceToString())
			}

		}
	}

	override fun loadFilters(): List<MultiLangFilterValue> {
		var filters: List<MultiLangFilterValue>? = null
		val filtersFile: Array<File> = File(app.filesDir.path).listFiles { _, name -> name == filtersFileName  }
		if (filtersFile.size == 1) {
			filters = parseFilters(filtersFile[0])
		}

		if (filters != null && filters.isNotEmpty())
			return filters

		return parseDefaultFilters()
	}

	private fun parseFilters(filtersFile: File): List<MultiLangFilterValue> =
		filtersFile.bufferedReader().parseFilters()

	private fun parseDefaultFilters(): List<MultiLangFilterValue> =
		app.resources.openRawResource(R.raw.filters).bufferedReader().parseFilters()

	private fun BufferedReader.parseFilters(): List<MultiLangFilterValue> = use {
		return try {
			val typeOfT: Type = object : TypeToken<List<MultiLangFilterValue?>?>() {}.type
			Gson().fromJson(this, typeOfT)
		} catch (e: Exception) {
			Log.e(TAG, e.stackTraceToString())
			emptyList()
		}
	}

	companion object {
		private const val filtersFileName: String = "filters.json"
		private const val TAG = "JsonFilterRepository"
	}
}
