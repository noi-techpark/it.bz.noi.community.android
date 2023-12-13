// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.meet

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.bearer
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@ExperimentalCoroutinesApi
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

	private val searchParamFlow = MutableStateFlow(savedStateHandle.get(SEARCH_PARAM_STATE) ?: "")

	private val availableFiltersFlow: StateFlow<Map<AccountType, List<FilterValue>>> =
		AccountsManager.availableAccountsFilters
	private val selectedFiltersFlow = MutableStateFlow(emptyMap<AccountType, List<FilterValue>>())

	val appliedFiltersFlow: Flow<Map<AccountType, List<FilterValue>>> =
		availableFiltersFlow.combine(selectedFiltersFlow) { availableFilters, selectedFilters ->
			val appliedFilters = mutableMapOf<AccountType, List<FilterValue>>()
			availableFilters.entries.forEach { availableEntry ->
				appliedFilters[availableEntry.key] = availableEntry.value.map { f ->
					f.copy(checked = selectedFilters[availableEntry.key]?.find { it.key == f.key } != null)
				}
			}
			appliedFilters
		}

	val selectedFiltersCount = selectedFiltersFlow.flatMapLatest { selectedFilters ->
		flowOf(cumulativeCount(selectedFilters.values))
	}.asLiveData(Dispatchers.IO)

	private fun reloadableContactsFlow(): Flow<Resource<List<Contact>>> = flow {
		emit(Resource.loading(data = null))
		try {
			val accessToken = AuthManager.obtainFreshToken()
			val contacts = mainRepository.getContacts(accessToken.bearer())
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
		combine(searchParamFlow, selectedFiltersFlow, contactsFlow) { searchParam: String,
																	  selectedFilters: Map<AccountType, List<FilterValue>>,
																	  allContacts: Resource<List<Contact>> ->
			val selectedFilterCount = cumulativeCount(selectedFilters.values)

			when (allContacts.status) {
				Status.SUCCESS -> {
					if (searchParam.isEmpty() && selectedFilterCount == 0)
						allContacts
					else {
						var filteredContacts = allContacts.data!!
						if (selectedFilterCount > 0) {
							val accountIdsFilters = selectedFilters.values.reduce { acc, list ->
								val newList = acc.toMutableList()
								newList.addAll(list)
								newList
							}.map {
								it.key
							}
							filteredContacts =
								filteredContacts.filterContactsByAccount(accountIdsFilters)
						}
						if (searchParam.isNotEmpty()) {
							filteredContacts =
								filteredContacts.filterContactsByName(searchParam)
						}
						Resource.success(data = filteredContacts)
					}
				}
				else -> {
					allContacts
				}
			}
		}.stateIn(viewModelScope, SharingStarted.Lazily, Resource.loading(data = null))

	fun refreshContacts() = reloadContactsTickerFlow.tryEmit(Unit)

	fun updateSearchParam(searchParam: String) {
		searchParamFlow.tryEmit(searchParam)
		savedStateHandle.set(SEARCH_PARAM_STATE, searchParam)
	}

	fun updateSelectedFilters(filters: Map<AccountType, List<FilterValue>>) {
		selectedFiltersFlow.tryEmit(filters)
	}

	private fun List<Contact>.filterContactsByName(text: String): List<Contact> {
		val matchingText = text.removeAccents()
		return filter { c ->
			c.fullName.removeAccents().contains(matchingText, ignoreCase = true)
		}
	}

	private fun List<Contact>.filterContactsByAccount(accountIds: List<String>): List<Contact> {
		return filter { c ->
			accountIds.contains(c.accountId)
		}
	}

	private fun <T> cumulativeCount(values: Collection<List<T>>): Int = values.sumOf {
		it.size
	}

}

@ExperimentalCoroutinesApi
class MeetViewModelFactory(
	private val apiHelper: ApiHelper,
	owner: Fragment
) : AbstractSavedStateViewModelFactory(owner, owner.arguments) {

	override fun <T : ViewModel> create(
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
