package it.bz.noi.community.ui

import androidx.lifecycle.*
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

enum class TimeRange {
    ALL,
    TODAY,
    THIS_WEEK,
    THIS_MONTH
}

class MainViewModel(private val mainRepository: MainRepository) : ViewModel() {

    val urlParams = UrlParams(startDate = Constants.getServerDateParser().format(Date()))

    private var events = liveData(Dispatchers.IO) {
        emit(Resource.loading(data = null))
        try {
            emit(Resource.success(data = mainRepository.getEvents(urlParams).events))
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }

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
                }
                calendar.add(Calendar.DAY_OF_YEAR, 7)
                urlParams.startDate = Constants.getServerDateParser().format(Date())
                urlParams.endDate = Constants.getServerDateParser().format(calendar.time)
            }
            TimeRange.THIS_MONTH -> {
                val calendar = Calendar.getInstance().apply {
                    time = Date()
                }
                calendar.add(Calendar.DAY_OF_YEAR, 30)
                urlParams.startDate = Constants.getServerDateParser().format(Date())
                urlParams.endDate = Constants.getServerDateParser().format(calendar.time)
            }
        }
        refreshData()
    }

    fun refresh() {
        refreshData()
    }

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
}