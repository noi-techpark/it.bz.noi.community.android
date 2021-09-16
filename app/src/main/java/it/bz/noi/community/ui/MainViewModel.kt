package it.bz.noi.community.ui

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.models.EventsResponse
import it.bz.noi.community.data.models.UrlParams
import it.bz.noi.community.data.repository.MainRepository
import it.bz.noi.community.utils.Constants
import it.bz.noi.community.utils.Resource
import kotlinx.coroutines.Dispatchers
import java.util.*

class ViewModelFactory(private val apiHelper: ApiHelper) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(MainRepository(apiHelper)) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}

/**
 * The different time ranges for filtering the events
 * ALL -> All the events starting from TODAY --> startDate has today date value
 * TODAY -> The events of today --> startDate and endDate both set to today date
 * THIS_WEEK -> From today to the end of the current week
 * THIS_MONTH -> From today to the end of the current month
 */
enum class TimeRange {
    ALL,
    TODAY,
    THIS_WEEK,
    THIS_MONTH
}

class MainViewModel(private val mainRepository: MainRepository) : ViewModel() {

    /**
     * parameters of the url for filter the events
     */
    val urlParams = UrlParams(startDate = Constants.getServerDateParser().format(Date()))

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

    fun filterTime(timeRange: TimeRange) {
        when (timeRange) {
            TimeRange.ALL -> {
                urlParams.startDate = Constants.getServerDateParser().format(Date())
                urlParams.endDate = null
            }
            TimeRange.TODAY -> {
                urlParams.startDate = Constants.getServerDateParser().format(Date())
                urlParams.endDate = Constants.getServerDateParser().format(Date())
            }
            TimeRange.THIS_WEEK -> {
                val calendar = Calendar.getInstance().apply {
                    time = Date()
                    firstDayOfWeek = Calendar.MONDAY
                }
                urlParams.startDate = Constants.getServerDateParser().format(calendar.time)
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
                urlParams.endDate = Constants.getServerDateParser().format(calendar.time)
            }
            TimeRange.THIS_MONTH -> {
                val calendar = Calendar.getInstance().apply {
                    time = Date()
                }
                urlParams.startDate = Constants.getServerDateParser().format(calendar.time)
                calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE))
                urlParams.endDate = Constants.getServerDateParser().format(calendar.time)
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
     * force the mediatorEvents to trigger other data
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
     * function for getting the details of event
     */
    fun getEventDetails(eventId: String) = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.getEventDetails(eventId)))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
}