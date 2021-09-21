package it.bz.noi.community.ui.today

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import it.bz.noi.community.data.models.EventsResponse
import it.bz.noi.community.data.models.TimeFilter
import it.bz.noi.community.utils.ResourcesHelper

class TodayViewModelFactory(private val resourceHelper: ResourcesHelper) :
	ViewModelProvider.Factory {
	override fun <T : ViewModel?> create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(TodayViewModel::class.java)) {
			return TodayViewModel(resourceHelper) as T
		}
		throw IllegalArgumentException("Unknown class name")
	}
}

class TodayViewModel(resourcesHelper: ResourcesHelper) : ViewModel() {
	val timeFilters = arrayListOf(
		TimeFilter(resourcesHelper.allLabel, true),
		TimeFilter(resourcesHelper.todayLabel, false),
		TimeFilter(resourcesHelper.thisWeekLabel, false),
		TimeFilter(resourcesHelper.thisMonthLabel, false)
	)

	val events = arrayListOf<EventsResponse.Event>()
}
