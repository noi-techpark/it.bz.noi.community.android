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
	suspend fun getEvents(eventsParams: EventsParams) = apiHelper.getEvents(eventsParams)
	suspend fun getEventDetails(eventID: String) = apiHelper.getEventDetails(eventID)
	suspend fun getEventFilterValues() = apiHelper.getEventFilterValues()

	// ROOMS
	suspend fun getRoomMapping(language: String?) = apiHelper.getRoomMapping(language)

	// NEWS
	suspend fun getNews(newsParams: NewsParams) = apiHelper.getNews(newsParams)
	suspend fun getNewsDetails(newsId: String, language: String?) =
		apiHelper.getNewsDetails(newsId = newsId, language = language)
	suspend fun getVideoThumbnail(url: String) = apiHelper.getVideoThumbnail(url = url)
	suspend fun getNewsFilterValues() = apiHelper.getNewsFilterValues()

	// CONTACTS
	suspend fun getAccounts(accessToken: String): List<Account> = apiHelper.getAccounts(accessToken).accounts
	suspend fun getContacts(accessToken: String): List<Contact> = apiHelper.getContacts(accessToken).contacts.let {
		if (isOptOutEnabled) it.filter { !it.appOptOut } else it
	}
}
