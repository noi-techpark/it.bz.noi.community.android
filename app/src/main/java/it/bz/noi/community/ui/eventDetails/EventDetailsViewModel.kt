// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.eventDetails

import androidx.fragment.app.Fragment
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.models.Event
import it.bz.noi.community.data.repository.MainRepository
import it.bz.noi.community.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine

class EventDetailsViewModel(
	private val mainRepository: MainRepository,
	savedStateHandle: SavedStateHandle
) : ViewModel() {

	companion object {
		const val EVENT_ID_ARG = "eventId"
		private const val EVENT_ARG = "event"
	}

	private val event = MutableStateFlow<Event?>(savedStateHandle[EVENT_ARG])
	private val eventId = MutableStateFlow<String?>(savedStateHandle[EVENT_ID_ARG])

	val eventFlow: Flow<Resource<Event>> = event.combine(eventId) { _event, _eventId ->
		Resource.loading(null)
		when {
			_event != null -> {
				Resource.success(
					data = _event
				)
			}
			_eventId != null -> {
				try {
					Resource.success(
						data = mainRepository.getEventDetails(
							_eventId
						)
					)
				} catch (exception: Exception) {
					Resource.error(data = null, message = exception.message ?: "Error Occurred!")
				}
			}
			else -> {
				Resource.error(data = null, message = "Missing arguments $EVENT_ID_ARG and $EVENT_ARG")
			}
		}
	}
}

class EventDetailsViewModelFactory(
	private val apiHelper: ApiHelper,
	owner: Fragment
) : AbstractSavedStateViewModelFactory(owner, owner.arguments) {

	override fun <T : ViewModel> create(
		key: String,
		modelClass: Class<T>,
		handle: SavedStateHandle
	): T {
		if (modelClass.isAssignableFrom(EventDetailsViewModel::class.java)) {
			return EventDetailsViewModel(MainRepository(apiHelper), handle) as T
		}
		throw IllegalArgumentException("Unknown class name")
	}
}
