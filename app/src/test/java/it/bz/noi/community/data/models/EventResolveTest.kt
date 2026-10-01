// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.data.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Date

private val NOW = Date()

private fun event(
	venueIds: List<String>? = null,
	eventDate: List<Event.EventDateItem>? = null,
	eventUrls: List<Event.EventUrl>? = null,
) = Event(
	eventId = "urn:event:noi:test",
	startDate = NOW,
	endDate = NOW,
	venueIds = venueIds,
	eventDate = eventDate,
	eventUrls = eventUrls,
)

class EventResolveTest {

	@Test
	fun `resolves the specific room name and map url when a room match is found`() {
		val venue = Venue(
			id = "urn:venue:noi:building",
			detail = mapOf("en" to Venue.VenueDetail(title = "NOI Techpark")),
			roomDetails = listOf(
				Venue.RoomDetail(
					id = "urn:venueroomid:noi:room1",
					shortname = "NOISE",
					mapping = Venue.RoomDetail.Mapping(Venue.RoomDetail.Mapping.Maps(roomMapping = "https://maps.noi.bz.it/en/?shared=NOISE"))
				)
			)
		)
		val resolved = event(
			venueIds = listOf("urn:venue:noi:building"),
			eventDate = listOf(Event.EventDateItem(venueRoomDetailsIds = listOf("urn:venueroomid:noi:room1"))),
		).resolve(mapOf(venue.id to venue))

		assertEquals("NOISE", resolved.resolvedLocationName)
		assertEquals("https://maps.noi.bz.it/en/?shared=NOISE", resolved.resolvedMapUrl)
	}

	@Test
	fun `falls back to the venue title when no room match is found`() {
		val venue = Venue(
			id = "urn:venue:noi:building",
			detail = mapOf("en" to Venue.VenueDetail(title = "NOI Techpark")),
			roomDetails = listOf(
				Venue.RoomDetail(id = "urn:venueroomid:noi:other", shortname = "Auditorium")
			)
		)
		val resolved = event(
			venueIds = listOf("urn:venue:noi:building"),
			eventDate = listOf(Event.EventDateItem(venueRoomDetailsIds = listOf("urn:venueroomid:noi:unmatched"))),
		).resolve(mapOf(venue.id to venue))

		assertEquals("NOI Techpark", resolved.resolvedLocationName)
		assertNull(resolved.resolvedMapUrl)
	}

	@Test
	fun `both fields are null when there is no venue at all`() {
		val resolved = event(venueIds = null).resolve(emptyMap())

		assertNull(resolved.resolvedLocationName)
		assertNull(resolved.resolvedMapUrl)
	}

	@Test
	fun `signupUrl matches the default EventUrl and ignores others`() {
		val withSignup = event(
			eventUrls = listOf(
				Event.EventUrl(url = mapOf("en" to "https://noi.bz.it/signup"), type = "default")
			)
		)
		assertEquals("https://noi.bz.it/signup", withSignup.signupUrl)
	}

	@Test
	fun `signupUrl is null when no EventUrl has type default (eg the misspelled 'deafult' seen live)`() {
		val misspelled = event(
			eventUrls = listOf(Event.EventUrl(url = mapOf("en" to "https://noi.bz.it/signup"), type = "deafult"))
		)
		assertNull(misspelled.signupUrl)

		val none = event(eventUrls = null)
		assertNull(none.signupUrl)
	}
}
