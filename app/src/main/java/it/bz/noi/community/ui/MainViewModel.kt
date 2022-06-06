package it.bz.noi.community.ui

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.models.*
import it.bz.noi.community.data.repository.FilterRepository
import it.bz.noi.community.data.repository.MainRepository
import it.bz.noi.community.ui.today.news.NewsPagingSource
import it.bz.noi.community.utils.DateUtils.endOfDay
import it.bz.noi.community.utils.DateUtils.lastDayOfCurrentMonth
import it.bz.noi.community.utils.DateUtils.lastDayOfCurrentWeek
import it.bz.noi.community.utils.DateUtils.parameterDateTimeFormatter
import it.bz.noi.community.utils.DateUtils.startOfDay
import it.bz.noi.community.utils.Resource
import it.bz.noi.community.utils.Status
import it.bz.noi.community.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.util.*

/**
 * Factory for creating the MainViewModel
 */
class ViewModelFactory(private val apiHelper: ApiHelper, private val filterRepo: FilterRepository) : ViewModelProvider.Factory {
	override fun <T : ViewModel?> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
			return MainViewModel(MainRepository(apiHelper), filterRepo) as T
		}
		throw IllegalArgumentException("Unknown class name")
	}
}

/**
 * The ViewModel shared between all the components of the app
 */
@ExperimentalCoroutinesApi
class MainViewModel(private val mainRepository: MainRepository, private val filterRepo: FilterRepository) : ViewModel() {

	companion object {
		private const val TAG = "MainViewModel"
	}

	private val startDate = Calendar.getInstance().startOfDay()

	/**
	 * persist the time filter selection for having UI consistency
	 */
	var selectedTimeFilter: TimeRange = TimeRange.ALL

	/**
	 * represents the parameters of the URL for filtering the events
	 */
	var eventsParams = EventsParams(startDate = parameterDateTimeFormatter().format(startDate))

	/**
	 * live data of the events
	 */
	private var events = liveData(Dispatchers.IO) {
		emit(Resource.loading(data = null))
		try {
			emit(Resource.success(data = mainRepository.getEvents(eventsParams).events))
		} catch (exception: Exception) {
			emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
		}
	}

	/**
	 * live data of the event filters
	 */
	private val availableFilters: LiveData<List<FilterValue>> = getEventFilterValues().map {
		when (it.status) {
			Status.SUCCESS -> {
				val language = Utils.getAppLanguage() ?: Utils.FALLBACK_LANGUAGE
				it.data!!.map {
					it.toFilterValue(language)
				}
			}
			Status.ERROR -> {
				Log.d(TAG, "Caricamento filtri KO")
				emptyList()
			}
			Status.LOADING -> {
				Log.d(TAG, "Filtri in caricamento")
				emptyList()
			}
		}
	}

	private val selectedFilters = MutableLiveData(emptyList<FilterValue>())
	fun updateSelectedFilters(filters: List<FilterValue>) {
		selectedFilters.postValue(filters)
		eventsParams.selectedFilters = filters
	}

	val appliedFilters = MediatorLiveData<List<FilterValue>>()

	val selectedFiltersCount = selectedFilters.asFlow().flatMapLatest {
		flowOf(it.size)
	}.asLiveData(Dispatchers.IO)

	/**
	 * mediator live data that emits the events to the observers
	 */
	val mediatorEvents = MediatorLiveData<Resource<List<EventsResponse.Event>>>()

	init {
		mediatorEvents.addSource(events) {
			mediatorEvents.value = it
		}

		appliedFilters.addSource(availableFilters) { newAvailableFilters ->
			appliedFilters.value = loadAppliedFilters()
		}
		appliedFilters.addSource(selectedFilters) { newSelectedFilters ->
			appliedFilters.value = loadAppliedFilters()
		}
	}

	private fun loadAppliedFilters(): List<FilterValue> {
		return availableFilters.value?.map {f ->
			f.copy(checked = selectedFilters.value?.find { it.key == f.key } != null)
		} ?: emptyList()
	}


	/**
	 * function used to filter the events by time
	 */
	fun filterTime(timeRange: TimeRange) {
		selectedTimeFilter = timeRange
		when (timeRange) {
			TimeRange.ALL -> {
				eventsParams.startDate = parameterDateTimeFormatter().format(startDate)
				eventsParams.endDate = null
				Log.d(TAG, "ALL filter: from ${eventsParams.startDate} to ${eventsParams.endDate}")
			}
			TimeRange.TODAY -> {
				eventsParams.startDate = parameterDateTimeFormatter().format(startDate)
				eventsParams.endDate = parameterDateTimeFormatter().format(Calendar.getInstance().endOfDay())
				Log.d(TAG, "TODAY filter: from ${eventsParams.startDate} to ${eventsParams.endDate}")
			}
			TimeRange.THIS_WEEK -> {
				eventsParams.startDate = parameterDateTimeFormatter().format(startDate)
				eventsParams.endDate = parameterDateTimeFormatter().format(lastDayOfCurrentWeek().endOfDay())
				Log.d(TAG, "THIS WEEK filter: from ${eventsParams.startDate} to ${eventsParams.endDate}")
			}
			TimeRange.THIS_MONTH -> {
				eventsParams.startDate = parameterDateTimeFormatter().format(startDate)
				eventsParams.endDate = parameterDateTimeFormatter().format(lastDayOfCurrentMonth().endOfDay())
				Log.d(TAG, "THIS MONTH filter: from ${eventsParams.startDate} to ${eventsParams.endDate}")
			}
		}
		refreshEventsData()
	}

	/**
	 * public function for reloading data, it can be used for updating the results
	 */
	fun refreshEvents() {
		refreshEventsData()
	}

	/**
	 * force the mediatorEvents to make another event request
	 */
	private fun refreshEventsData() {
		mediatorEvents.removeSource(events)
		events = liveData(Dispatchers.IO) {
			emit(Resource.loading(data = null))
			try {
				emit(Resource.success(data = mainRepository.getEvents(eventsParams).events))
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
			emit(Resource.success(data = mainRepository.getRoomMapping(Utils.getAppLanguage())))
		} catch (exception: Exception) {
			emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
		}
	}

	/**
	 * Loads filters to show in filter screen
	 */
	private fun getEventFilterValues() = liveData(Dispatchers.IO) {
		emit(Resource.loading(null))
		var filters: List<MultiLangEventsFilterValue>
		try {
			filters = mainRepository.getEventFilterValues()
			if (filters.isEmpty())
				filters= filterRepo.loadFilters()
			else
				filterRepo.saveFilters(filters)
		} catch (exception: Exception) {
			filters= filterRepo.loadFilters()
		}

		if (filters != null && filters.isNotEmpty())
			emit(Resource.success(data = filters))
		else
			emit(Resource.error(data = null, message = "Filter loading: error occurred!"))
	}

	private val reloadNewsTickerFlow = MutableSharedFlow<Unit>(replay = 1).apply {
		tryEmit(Unit)
	}

	val newsFlow: StateFlow<PagingData<News>> = reloadNewsTickerFlow.flatMapLatest {
		loadNews()
	}.stateIn(viewModelScope, SharingStarted.Lazily, PagingData.empty())

	private fun loadNews(): Flow<PagingData<News>> = Pager(PagingConfig(pageSize = NewsPagingSource.PAGE_ITEMS)) {
		NewsPagingSource(mainRepository)
	}.flow.cachedIn(viewModelScope)

	fun refreshNews() {
		reloadNewsTickerFlow.tryEmit(Unit)
	}

}

object NewsTickerFlow {
	val ticker = MutableSharedFlow<Unit>(replay = 1).apply {
		tryEmit(Unit)
	}
	fun tick() = ticker.tryEmit(Unit)
}

