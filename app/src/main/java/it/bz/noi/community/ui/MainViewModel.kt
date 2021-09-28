package it.bz.noi.community.ui

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.models.EventsResponse
import it.bz.noi.community.data.models.TimeRange
import it.bz.noi.community.data.models.UrlParams
import it.bz.noi.community.data.repository.MainRepository
import it.bz.noi.community.utils.Constants
import it.bz.noi.community.utils.Constants.getEndOfThisMonth
import it.bz.noi.community.utils.Constants.getNoiCalendar
import it.bz.noi.community.utils.Constants.getEndOfThisWeek
import it.bz.noi.community.utils.Resource
import kotlinx.coroutines.Dispatchers
import java.util.*

/**
 * Factory for creating the MainViewModel
 */
class ViewModelFactory(private val apiHelper: ApiHelper) : ViewModelProvider.Factory {
	override fun <T : ViewModel?> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
			return MainViewModel(MainRepository(apiHelper)) as T
		}
		throw IllegalArgumentException("Unknown class name")
	}
}

/**
 * The ViewModel shared between all the components of the app
 */
class MainViewModel(private val mainRepository: MainRepository) : ViewModel() {

	companion object {
		private const val TAG = "MainViewModel"
	}

	private val today = getNoiCalendar().time
	private val endOfWeek = getEndOfThisWeek().time
	private val endOfMonth = getEndOfThisMonth().time

	/**
	 * persist the time filter selection for having UI consistency
	 */
	var selectedTimeFilter: TimeRange = TimeRange.ALL

	/**
	 * represents the parameters of the URL for filtering the events
	 */
	var urlParams = UrlParams(startDate = Constants.startDateFormatter().format(Date()))

	/**
	 * parameter used for caching the initial filter situation in the Filters fragment
	 */
	private lateinit var cachedParams: UrlParams
	fun cacheFilters() {
		cachedParams = urlParams.copy()
	}

	/**
	 * live data of the events
	 */
	private var events = liveData(Dispatchers.IO) {
		emit(Resource.loading(data = null))
		try {
			emit(Resource.success(data = mainRepository.getEvents(urlParams).events))
		} catch (exception: Exception) {
			emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
		}
	}

	/**
	 * mediator live data that emits the events to the observers
	 */
	val mediatorEvents = MediatorLiveData<Resource<List<EventsResponse.Event>>>()

	init {
		mediatorEvents.addSource(events) {
			mediatorEvents.value = it
		}
	}

	/**
	 * function used to filter the events by time
	 */
	fun filterTime(timeRange: TimeRange) {
		selectedTimeFilter = timeRange
		when (timeRange) {
			TimeRange.ALL -> {
				urlParams.startDate = Constants.startDateFormatter().format(today)
				urlParams.endDate = null
				Log.d(TAG, "ALL filter: from ${urlParams.startDate} to ${urlParams.endDate}")
			}
			TimeRange.TODAY -> {
				urlParams.startDate = Constants.startDateFormatter().format(today)
				urlParams.endDate = Constants.endDateFormatter().format(today)
				Log.d(TAG, "TODAY filter: from ${urlParams.startDate} to ${urlParams.endDate}")
			}
			TimeRange.THIS_WEEK -> {
				urlParams.startDate = Constants.startDateFormatter().format(today)
				urlParams.endDate = Constants.endDateFormatter().format(endOfWeek)
				Log.d(TAG, "THIS WEEK filter: from ${urlParams.startDate} to ${urlParams.endDate}")
			}
			TimeRange.THIS_MONTH -> {
				urlParams.startDate = Constants.startDateFormatter().format(today)
				urlParams.endDate = Constants.endDateFormatter().format(endOfMonth)
				Log.d(TAG, "THIS MONTH filter: from ${urlParams.startDate} to ${urlParams.endDate}")
			}
		}
		refreshData()
	}

	/**
	 * public function for reloading data, it can be used for updating the results
	 */
	fun refresh() {
		refreshData()
	}

	/**
	 * force the mediatorEvents to make another event request
	 */
	private fun refreshData() {
		mediatorEvents.removeSource(events)
		events = liveData(Dispatchers.IO) {
			emit(Resource.loading(data = null))
			try {
				emit(Resource.success(data = mainRepository.getEvents(urlParams).events))
			} catch (exception: Exception) {
				emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
			}
		}
		mediatorEvents.addSource(events) {
			mediatorEvents.value = it
		}
	}

	/**
	 *
	 */
	fun getRoomMapping() = liveData(Dispatchers.IO) {
		emit(Resource.loading(null))
		try {
			emit(Resource.success(data = mainRepository.getRoomMapping()))
		} catch (exception: Exception) {
			emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
		}
	}

	/**
	 * restore the initial situation of filters
	 */
	fun restoreCachedFilters() {
		urlParams = cachedParams.copy()
	}

	/**
	 * Used for check if cached filters are identical to current filters
	 */
	fun isFiltersSameAsCached(): Boolean {
		return urlParams == cachedParams
	}
}
