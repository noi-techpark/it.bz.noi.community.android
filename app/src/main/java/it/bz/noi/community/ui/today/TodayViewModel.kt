package it.bz.noi.community.ui.today

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.bz.noi.community.data.models.TimeFilter

class TodayViewModel : ViewModel() {

    val timeFilters = arrayListOf<TimeFilter>(
        TimeFilter("All", true),
        TimeFilter("Today", false),
        TimeFilter("This week", false),
        TimeFilter("This month", false),
    )

    private val _text = MutableLiveData<String>().apply {
        value = "This is Today Screen"
    }
    val text: LiveData<String> = _text
}