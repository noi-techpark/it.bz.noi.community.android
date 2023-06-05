// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.data.api

import com.google.gson.*
import it.bz.noi.community.BuildConfig
import it.bz.noi.community.utils.NOIDateParser
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import java.lang.reflect.Type
import java.util.*

object RetrofitBuilder {

	private const val OPENDATAHUB_API_BASE_URL = "https://tourism.opendatahub.bz.it/"

	private fun getRetrofit(baseUrl: String): Retrofit {
		val interceptor = HttpLoggingInterceptor()
		interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
		val client: OkHttpClient = OkHttpClient.Builder().addInterceptor(interceptor).build()

		val gsonBuilder = GsonBuilder()
		gsonBuilder.registerTypeAdapter(Date::class.java, NOIDateDeserializer)

		return Retrofit.Builder()
			.baseUrl(baseUrl)
			.client(client)
			.addConverterFactory(GsonConverterFactory.create(gsonBuilder.create()))
			.build() //Doesn't require the adapter
	}

	val opendatahubApiService: OpendatahubApiService = getRetrofit(OPENDATAHUB_API_BASE_URL).create(OpendatahubApiService::class.java)
	val communityApiService: CommunityApiService = getRetrofit(BuildConfig.COMMUNITY_API_URL).create(CommunityApiService::class.java)
}

object NOIDateDeserializer : JsonDeserializer<Date> {

	private val dateParser = NOIDateParser()

	override fun deserialize(
		json: JsonElement?,
		typeOfT: Type?,
		context: JsonDeserializationContext?
	): Date {
		if (json == null)
			throw JsonParseException("DateParseException: null jsonElement")

		if (json.isJsonNull) {
			throw JsonParseException("The date should not be null")
		}
		if (!json.isJsonPrimitive || !json.asJsonPrimitive.isString) {
			throw JsonParseException("The date should be a string value")
		}

		return try {
			dateParser.parse(json.asString)
		} catch (e: Exception) {
			throw JsonParseException("DateParseException: $json.asString", e)
		}

	}

}
