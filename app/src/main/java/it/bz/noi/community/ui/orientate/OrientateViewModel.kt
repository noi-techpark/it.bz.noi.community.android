package it.bz.noi.community.ui.orientate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class OrientateViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Orientate Screen"
    }
    val text: LiveData<String> = _text
}