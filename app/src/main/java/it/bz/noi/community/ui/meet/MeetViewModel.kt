package it.bz.noi.community.ui.meet

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.models.Account
import it.bz.noi.community.data.models.Contact
import it.bz.noi.community.data.repository.MainRepository
import it.bz.noi.community.oauth.AuthManager
import it.bz.noi.community.utils.Resource
import it.bz.noi.community.utils.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

class MeetViewModel(
	private val mainRepository: MainRepository
) : ViewModel() {

	companion object {
		private const val TAG = "MeetViewModel"
	}
/*
	private val availableContacts: LiveData<List<Contact>> = getContacts().map { res ->
		when (res.status) {
			Status.SUCCESS -> {
				val contacts = res.data!!
				Log.d(TAG, "Caricati ${contacts.size} contatti")
				contacts
			}
			Status.ERROR -> {
				Log.d(TAG, "Caricamento contatti KO")
				emptyList()
			}
			Status.LOADING -> {
				Log.d(TAG, "Contatti in caricamento...")
				emptyList()
			}
		}
	}

	private fun getContacts() = liveData(Dispatchers.IO) {
		emit(Resource.loading(null))
		try {
			val accessToken = AuthManager.obtainFreshToken()
			val contacts = mainRepository.getContacts("Bearer $accessToken")
			Log.d(TAG, "Caricati ${contacts.size} contatti")
			emit(Resource.success(data = contacts))
		} catch (exception: Exception) {
			emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
		}
	}

	fun getContacts(availableCompanies: Map<String, Account>): LiveData<List<Contact>> = availableContacts.map {
		if (it.isEmpty()) {
			it
		} else {
			it.map { c ->
				if (c.accountId != null) {
					c.copy(companyName = availableCompanies.get(c.accountId)?.name)
				} else {
					c
				}
			}
		}

	}*/

	private val reloadContactsTickerFlow = MutableSharedFlow<Unit>(replay = 1).apply {
		tryEmit(Unit)
	}

	val contactsFlow = reloadContactsTickerFlow.flatMapLatest{
		reloadableContactsFlow()
	}

	private fun reloadableContactsFlow() = flow {
		emit(Resource.loading(null))
		try {
			val accessToken = AuthManager.obtainFreshToken()
			val contacts = mainRepository.getContacts("Bearer $accessToken")
			Log.d(TAG, "Caricati ${contacts.size} contatti")
			emit(Resource.success(data = contacts))
		} catch (exception: Exception) {
			emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
		}
	}

	fun refreshContacts() = reloadContactsTickerFlow.tryEmit(Unit) // TODO

}

class MeetViewModelFactory(
	private val apiHelper: ApiHelper
) : ViewModelProvider.Factory {

	override fun <T : ViewModel?>create(modelClass: Class<T>): T {
		if (modelClass.isAssignableFrom(MeetViewModel::class.java)) {
			return MeetViewModel(MainRepository(apiHelper)) as T
		}
		throw IllegalArgumentException("Unknown class name")
	}
}
