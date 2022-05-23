package it.bz.noi.community.ui.meet

import android.util.Log
import androidx.lifecycle.*
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.repository.MainRepository
import it.bz.noi.community.oauth.AuthManager
import it.bz.noi.community.utils.Resource
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

class MeetViewModel(
	private val mainRepository: MainRepository
) : ViewModel() {

	companion object {
		private const val TAG = "MeetViewModel"
	}

	private val reloadContactsTickerFlow = MutableSharedFlow<Unit>(replay = 1).apply {
		tryEmit(Unit)
	}

	val contactsFlow = reloadContactsTickerFlow.flatMapLatest{
		reloadableContactsFlow()
	}

	private fun reloadableContactsFlow() = flow {
		emit(Resource.loading(data = null))
		try {
			val accessToken = AuthManager.obtainFreshToken()
			val contacts = mainRepository.getContacts("Bearer $accessToken")
			Log.d(TAG, "Caricati ${contacts.size} contatti")
			emit(Resource.success(data = contacts))
		} catch (exception: Exception) {
			emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
		}
	}

	fun refreshContacts() = reloadContactsTickerFlow.tryEmit(Unit)

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
