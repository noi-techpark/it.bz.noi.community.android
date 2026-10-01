// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.data.api

import com.google.gson.GsonBuilder
import it.bz.noi.community.data.models.Event
import it.bz.noi.community.data.models.EventsResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.util.Date

/**
 * Deserializes a literal JSON payload shaped exactly like the live /v1/Event
 * response (verified against both tourism.api.opendatahub.com and the
 * testingmachine.eu staging environment) - not just the Kotlin constructor
 * directly, to catch @SerializedName keys that silently stop matching the
 * real API field names (Gson sets a missing key's field to null via
 * reflection, bypassing both the Kotlin non-null type and any constructor
 * default value, so a wrong key name never surfaces as a compile error or a
 * constructor-based unit test failure).
 */
class EventGsonDeserializationTest {

	private val gson = GsonBuilder().apply {
		registerTypeAdapter(Date::class.java, NOIDateDeserializer)
	}.create()

	// Shape verified live: single-language Detail/OrganizerInfos, one EventDate
	// entry (as guaranteed by denormalize=true), EventUrls with Type "default".
	private val liveShapedEventJson = """
		{
			"Id": "urn:event:noi:d9301ccf-c0fd-4d79-96bc-c314adab1e5c",
			"DateBegin": "2026-02-23T09:00:00",
			"DateEnd": "2026-02-23T17:00:00",
			"Detail": {
				"en": { "Title": "Smart Kitchen Academy", "BaseText": "Some description" }
			},
			"OrganizerInfos": {
				"en": { "CompanyName": "NOI Techpark" }
			},
			"EventUrls": [
				{ "Url": { "en": "https://noi.bz.it/en/lps/smart-kitchen-academy" }, "Type": "default" }
			],
			"ImageGallery": [
				{ "ImageUrl": "https://cdn.opendatahub.com/api/Image/GetImage?imageurl=test.jpg" }
			],
			"VenueIds": ["urn:venue:noi:6b3f0a14-3c5b-5d09-81f3-3ebe5b7885ea"],
			"EventDate": [
				{ "VenueRoomDetailsIds": ["urn:venueroomid:noi:debedd56-2021-54d1-84fd-c29cfb88b5cd"] }
			]
		}
	""".trimIndent()

	@Test
	fun `deserializes DateBegin and DateEnd into non-null startDate and endDate`() {
		val event = gson.fromJson(liveShapedEventJson, Event::class.java)

		assertNotNull(event.startDate)
		assertNotNull(event.endDate)
		assertEquals("2026-02-23T09:00:00", isoNoZone(event.startDate))
		assertEquals("2026-02-23T17:00:00", isoNoZone(event.endDate))
	}

	@Test
	fun `deserializes nested Detail, OrganizerInfos, EventUrls and EventDate correctly`() {
		val event = gson.fromJson(liveShapedEventJson, Event::class.java)

		assertEquals("Smart Kitchen Academy", event.detail?.get("en")?.title)
		assertEquals("Some description", event.detail?.get("en")?.baseText)
		assertEquals("NOI Techpark", event.organizerInfos?.get("en")?.companyName)
		assertEquals("default", event.eventUrls?.firstOrNull()?.type)
		assertEquals(
			"https://noi.bz.it/en/lps/smart-kitchen-academy",
			event.eventUrls?.firstOrNull()?.url?.get("en")
		)
		assertEquals(
			listOf("urn:venueroomid:noi:debedd56-2021-54d1-84fd-c29cfb88b5cd"),
			event.eventDate?.firstOrNull()?.venueRoomDetailsIds
		)
	}

	@Test
	fun `deserializes the Items-wrapped list response`() {
		val response = gson.fromJson("""{"Items": [$liveShapedEventJson]}""", EventsResponse::class.java)

		assertEquals(1, response.events.size)
		assertNotNull(response.events.first().startDate)
	}

	private fun isoNoZone(date: Date): String {
		val formatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale_EN_US_POSIX)
		formatter.timeZone = java.util.TimeZone.getTimeZone("Europe/Rome")
		return formatter.format(date)
	}

	companion object {
		private val Locale_EN_US_POSIX = java.util.Locale("en_US_POSIX")
	}
}
