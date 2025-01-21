// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.data.repository

import android.util.Log
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.api.bearer
import it.bz.noi.community.data.models.*
import it.bz.noi.community.oauth.AuthManager
import it.bz.noi.community.utils.Resource
import it.bz.noi.community.utils.Status
import it.bz.noi.community.utils.Utils.removeAccents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
object AccountsManager {

	private const val TAG = "AccountsManager"

	private val mainRepository = MainRepository(
		ApiHelper(
			RetrofitBuilder.opendatahubApiService,
			RetrofitBuilder.communityApiService,
			RetrofitBuilder.vimeoApiService
		)
	)
	private val mainCoroutineScope = CoroutineScope(Dispatchers.Main + Job())

	private val reloadTickerFlow = MutableSharedFlow<Unit>(replay = 1)

	private val reloadableAccountsFlow: Flow<Map<String, Account>> =
		reloadTickerFlow.flatMapLatest {
			getAccounts().flatMapLatest { res ->
				when (res.status) {
					Status.SUCCESS -> {
						val accounts = res.data!!
						Log.d(TAG, "Caricati ${accounts.size} accounts")
						flowOf(accounts.associateBy { it.id })
					}

					Status.ERROR -> {
						Log.d(TAG, "Caricamento accounts KO")
						flowOf(emptyMap())
					}

					Status.LOADING -> {
						Log.d(TAG, "Accounts in caricamento...")
						flowOf(emptyMap())
					}
				}
			}
		}

	val availableCompanies: StateFlow<Map<String, Account>> =
		reloadableAccountsFlow.stateIn(mainCoroutineScope, SharingStarted.Lazily, emptyMap())

	private fun getAccounts() = flow {
		emit(Resource.loading(null))
		try {
			val accessToken = AuthManager.obtainFreshToken()
			val accounts = mainRepository.getAccounts(accessToken.bearer())
			emit(Resource.success(data = accounts))
		} catch (exception: Exception) {
			emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
		}
	}

	fun reload() = reloadTickerFlow.tryEmit(Unit)

	private val searchParamFlow = MutableStateFlow("")

	val availableAccountsFilters: StateFlow<Map<AccountType, List<FilterValue>>> =
		availableCompanies.combine(searchParamFlow) { accountsMap: Map<String, Account>, searchParam: String ->
			if (searchParam.isEmpty())
				mapAccountsToFilterValues(accountsMap.values)
			else {
				val filteredAccounts = accountsMap.values.filterAccountsByName(searchParam)
				mapAccountsToFilterValues(filteredAccounts)
			}
		}.stateIn(mainCoroutineScope, SharingStarted.Lazily, emptyMap())

	private fun mapAccountsToFilterValues(accounts: Collection<Account>): Map<AccountType, List<FilterValue>> {
		return accounts.groupBy { a ->
			a.getAccountType()
		}.filterKeys { type ->
			type != AccountType.DEFAULT
		}.mapValues {
			it.value.map { account ->
				account.toFilterValue()
			}
		}
	}

	fun updateSearchParam(searchParam: String) {
		searchParamFlow.tryEmit(searchParam)
	}

	private fun Collection<Account>.filterAccountsByName(text: String): Collection<Account> {
		val matchingText = text.removeAccents()
		return filter { a ->
			a.name.removeAccents().contains(matchingText, ignoreCase = true)
		}
	}

}
