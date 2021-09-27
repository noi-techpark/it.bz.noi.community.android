package it.bz.noi.community.ui.today

import androidx.lifecycle.ViewModel
import it.bz.noi.community.data.models.EventsResponse
import it.bz.noi.community.data.models.TimeFilter

class TodayViewModel : ViewModel() {
	val events = arrayListOf<EventsResponse.Event>()
}
