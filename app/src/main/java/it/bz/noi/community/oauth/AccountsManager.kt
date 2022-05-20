package it.bz.noi.community.oauth

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.Account
import it.bz.noi.community.data.repository.MainRepository
import it.bz.noi.community.utils.Resource
import it.bz.noi.community.utils.Status
import kotlinx.coroutines.Dispatchers

object AccountsManager {

	private const val TAG = "AccountsManager"

	private val mainRepository = MainRepository(ApiHelper(RetrofitBuilder.opendatahubApiService, RetrofitBuilder.communityApiService))

	val availableCompanies: LiveData<Map<String, Account>> = getAcccounts().map { res ->
		when (res.status) {
			Status.SUCCESS -> {
				val accounts = res.data!!
				Log.d(TAG, "Caricati ${accounts.size} accounts")
				accounts.associateBy { it.id }
			}
			Status.ERROR -> {
				Log.d(TAG, "Caricamento accounts KO")
				emptyMap<String, Account>()
			}
			Status.LOADING -> {
				Log.d(TAG, "Accounts in caricamento...")
				emptyMap<String, Account>()
			}
		}
	}

	private fun getAcccounts() = liveData(Dispatchers.IO) {
		emit(Resource.loading(null))
		try {
			val accessToken = AuthManager.obtainFreshToken()
			val accounts = mainRepository.getAccounts("Bearer $accessToken")
			emit(Resource.success(data = accounts))
		} catch (exception: Exception) {
			emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
		}
	}

}
