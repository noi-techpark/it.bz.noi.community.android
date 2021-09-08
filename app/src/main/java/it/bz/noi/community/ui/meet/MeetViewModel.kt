package it.bz.noi.community.ui.meet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MeetViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Meet Fragment"
    }
    val text: LiveData<String> = _text
}