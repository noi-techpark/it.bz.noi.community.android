package it.bz.noi.community.data.repository

import android.util.Log
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.*
import it.bz.noi.community.oauth.AuthManager
import it.bz.noi.community.utils.Resource
import it.bz.noi.community.utils.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*

object AccountsManager {

	private const val TAG = "AccountsManager"

	private val mainRepository = MainRepository(
		ApiHelper(
			RetrofitBuilder.opendatahubApiService,
			RetrofitBuilder.communityApiService
		)
	)
	private val mainCoroutineScope = CoroutineScope(Dispatchers.Main + Job())

	private val reloadTickerFlow = MutableSharedFlow<Unit>(replay = 1)

	private val reloadableAccountsFlow: Flow<Map<String, Account>> = reloadTickerFlow.flatMapLatest {
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

	val availableCompanies: StateFlow<Map<String, Account>> = reloadableAccountsFlow.stateIn(mainCoroutineScope, SharingStarted.Lazily, emptyMap())

	private fun getAccounts() = flow {
		emit(Resource.loading(null))
		try {
			val accessToken = AuthManager.obtainFreshToken()
			val accounts = mainRepository.getAccounts("Bearer $accessToken")
			emit(Resource.success(data = accounts))
		} catch (exception: Exception) {
			emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
		}
	}

	fun relaod() = reloadTickerFlow.tryEmit(Unit)

	val availableAccountsFilters: StateFlow<Map<Int, List<FilterValue>>> = availableCompanies.flatMapLatest {
		val accountGroups: Map<AccountType, List<Account>> = it.values.groupBy { a ->
			a.getAccountType()
		}

		flowOf(mapAccountsToFilters(accountGroups))
	}.stateIn(mainCoroutineScope, SharingStarted.Lazily, emptyMap())

	private fun mapAccountsToFilters(accountGroups: Map<AccountType, List<Account>>): Map<Int, List<FilterValue>> {
		return accountGroups.filterKeys { type ->
			type != AccountType.DEFAULT
		}.mapKeys {
			it.key.filterCode!!
		}.mapValues {
			it.value.map { account ->
				account.toFilterValue()
			}
		}
	}

}
