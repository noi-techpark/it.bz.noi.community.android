package it.bz.noi.community.ui.meet

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.models.AccountType
import it.bz.noi.community.data.models.Contact
import it.bz.noi.community.data.models.FilterValue
import it.bz.noi.community.data.repository.AccountsManager
import it.bz.noi.community.data.repository.MainRepository
import it.bz.noi.community.oauth.AuthManager
import it.bz.noi.community.utils.Resource
import it.bz.noi.community.utils.Status
import it.bz.noi.community.utils.Utils.removeAccents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class MeetViewModel(
	private val mainRepository: MainRepository,
	private val savedStateHandle: SavedStateHandle
) : ViewModel() {

	companion object {
		private const val TAG = "MeetViewModel"
		private const val SEARCH_PARAM_STATE = "search_param_state"
	}

	private val reloadContactsTickerFlow = MutableSharedFlow<Unit>(replay = 1).apply {
		tryEmit(Unit)
	}

	private val contactsFlow = reloadContactsTickerFlow.flatMapLatest {
		reloadableContactsFlow()
	}

	val searchParamFlow = MutableSharedFlow<CharSequence?>(replay = 1).apply {
		tryEmit(savedStateHandle.get(SEARCH_PARAM_STATE))
	}

	private val availableFiltersFlow: StateFlow<Map<AccountType,List<FilterValue>>> = AccountsManager.availableAccountsFilters
	private val selectedFiltersFlow = MutableStateFlow(emptyMap<AccountType,List<FilterValue>>())

	fun updateSelectedFilters(filters: Map<AccountType,List<FilterValue>>) {
		selectedFiltersFlow.tryEmit(filters)
		//contactsParams.selectedFilters = filters // TODO
	}

	val appliedFiltersFlow: Flow<Map<AccountType, List<FilterValue>>> = availableFiltersFlow.combine(selectedFiltersFlow) { availableFilters, selectedFilters ->
		val appliedFilters = mutableMapOf<AccountType, List<FilterValue>>()
		availableFilters.entries.forEach { availableEntry ->
			appliedFilters[availableEntry.key] = availableEntry.value.map { f ->
				f.copy(checked = selectedFilters[availableEntry.key]?.find { it.key == f.key } != null)
			}
		}
		appliedFilters
	}

	// FIXME
	val selectedFiltersCount = selectedFiltersFlow.flatMapLatest { selectedFilters ->
		val count = selectedFilters.values.sumOf {
			it.size
		}
		flowOf(count)
	}.asLiveData(Dispatchers.IO)

	private fun reloadableContactsFlow(): Flow<Resource<List<Contact>>> = flow {
		emit(Resource.loading(data = null))
		try {
			val accessToken = AuthManager.obtainFreshToken()
			val contacts = mainRepository.getContacts("Bearer $accessToken")
			Log.d(TAG, "Caricati ${contacts.size} contatti")
			val availableCompanies = AccountsManager.availableCompanies.value
			val allContacts = contacts.map { c ->
				if (c.accountId != null) {
					c.copy(companyName = availableCompanies.get(c.accountId)?.name)
				} else {
					c
				}
			}
			emit(Resource.success(data = allContacts))
		} catch (exception: Exception) {
			emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
		}
	}

	val filteredContactsFlow: Flow<Resource<List<Contact>>> =
		combine(searchParamFlow, selectedFiltersFlow, contactsFlow) { searchParam: CharSequence?, selectedFilters: Map<AccountType,List<FilterValue>>, allContacts: Resource<List<Contact>> ->
			if (searchParam.isNullOrEmpty() && selectedFilters.isNullOrEmpty())
				allContacts
			else {
				when (allContacts.status) {
					Status.SUCCESS -> {
						var filteredContacts = allContacts.data!!
						if (!selectedFilters.isNullOrEmpty()) {

							// FIXME
							val accountIdsFilters = selectedFilters.values.reduce {acc, list ->
								val newList = acc.toMutableList()
								newList.addAll(list)
								newList
							}.map {
								it.key
							}
							filteredContacts = filteredContacts.filterContactsByAccount(accountIdsFilters)
						}
						if (!searchParam.isNullOrEmpty()) {
							filteredContacts = filteredContacts.filterContactsByName(searchParam)
						}
						Resource.success(data = filteredContacts)
					}
					else -> {
						allContacts
					}
				}
			}
		}

	fun refreshContacts() = reloadContactsTickerFlow.tryEmit(Unit)

	fun updateSearchParam(searchParam: CharSequence?) {
		searchParamFlow.tryEmit(searchParam)
		savedStateHandle.set(SEARCH_PARAM_STATE, searchParam)
	}

	private fun List<Contact>.filterContactsByName(text: CharSequence): List<Contact> {
		val matchingText = text.toString().removeAccents()
		return filter { c ->
			c.fullName.removeAccents().contains(matchingText, ignoreCase = true)
		}
	}

	private fun List<Contact>.filterContactsByAccount(accountIds: List<String>): List<Contact> {
		return filter { c ->
			accountIds.contains(c.accountId)
		}
	}

}

class MeetViewModelFactory(
	private val apiHelper: ApiHelper,
	owner: Fragment
) : AbstractSavedStateViewModelFactory(owner, owner.arguments) {

	override fun <T : ViewModel?> create(
		key: String,
		modelClass: Class<T>,
		handle: SavedStateHandle
	): T {
		if (modelClass.isAssignableFrom(MeetViewModel::class.java)) {
			return MeetViewModel(MainRepository(apiHelper), handle) as T
		}
		throw IllegalArgumentException("Unknown class name")
	}
}
