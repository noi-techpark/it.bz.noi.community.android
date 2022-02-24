package it.bz.noi.community.data.repository

import android.app.Application
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.bz.noi.community.R
import it.bz.noi.community.data.models.MultiLangFilterValue
import java.lang.reflect.Type

interface FilterRepository {

	fun saveFilters(filters: List<MultiLangFilterValue>)
	fun loadFilters(): List<MultiLangFilterValue>

}

class JsonFilterRepository(val app: Application, val path: String) : FilterRepository {
	override fun saveFilters(filters: List<MultiLangFilterValue>) {
		//TODO("Not yet implemented")
	}

	override fun loadFilters(): List<MultiLangFilterValue> {
		//if (File(path).exist()) return parseFilters(path)
		return parseDefaultFilters()
	}

	private fun parseDefaultFilters(): List<MultiLangFilterValue> {
		app.resources.openRawResource(R.raw.filters).bufferedReader().use {
			return try {
				val typeOfT: Type = object : TypeToken<List<MultiLangFilterValue?>?>() {}.type
				Gson().fromJson(it, typeOfT)
			} catch (e: Exception) {
				Log.e(TAG, e.stackTraceToString())
				emptyList()
			}
		}
	}

	companion object {
		private const val TAG = "JsonFilterRepository"
	}
}
