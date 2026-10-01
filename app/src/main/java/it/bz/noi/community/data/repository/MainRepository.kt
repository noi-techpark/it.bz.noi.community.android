// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.data.repository

import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.models.*

class MainRepository(
	/**
	 * Remote data source.
	 */
	private val apiHelper: ApiHelper,
	/**
	 * If enabled, [Contact]s with `appOptOut` set to `true` will be filtered out.
	 */
	private val isOptOutEnabled: Boolean = false
) {
	// EVENTS
	suspend fun getEvents(eventsParams: EventsParams): List<Event> =
		resolveVenues(apiHelper.getEvents(eventsParams).events)

	suspend fun getEventDetails(eventID: String): Event =
		resolveVenues(listOf(apiHelper.getEventDetails(eventID))).first()

	suspend fun getEventFilterValues() = apiHelper.getEventFilterValues()

	/**
	 * VenueIds on an event only identifies the building; resolving the specific
	 * room name/map link requires a batched /v1/Venue call for all distinct
	 * VenueIds in the page, done once here rather than once per event.
	 */
	private suspend fun resolveVenues(events: List<Event>): List<Event> {
		val venueIds = events.flatMap { it.venueIds.orEmpty() }.distinct()
		if (venueIds.isEmpty())
			return events

		val venuesById = apiHelper.getVenues(venueIds.joinToString(",")).venues.associateBy { it.id }
		return events.map { it.resolve(venuesById) }
	}

	// NEWS
	suspend fun getNews(newsParams: NewsParams) = apiHelper.getNews(newsParams)
	suspend fun getNewsDetails(newsId: String, language: String?) =
		apiHelper.getNewsDetails(newsId = newsId, language = language)
	suspend fun getVideoThumbnail(url: String) = apiHelper.getVideoThumbnail(url = url)
	suspend fun getNewsFilterValues() = apiHelper.getNewsFilterValues()
	suspend fun getNewsCount(newsParams: NewsParams) = apiHelper.getNewsCount(newsParams)

	// CONTACTS
	suspend fun getAccounts(accessToken: String): List<Account> = apiHelper.getAccounts(accessToken).accounts
	suspend fun getContacts(accessToken: String): List<Contact> = apiHelper.getContacts(accessToken).contacts.let {
		if (isOptOutEnabled) it.filter { !it.appOptOut } else it
	}
}
