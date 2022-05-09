package it.bz.noi.community.oauth

import android.app.Activity
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.GsonBuilder
import it.bz.noi.community.BuildConfig
import it.bz.noi.community.NoiApplication.Companion.SHARED_PREFS_NAME
import it.bz.noi.community.utils.Resource
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import net.openid.appauth.*
import net.openid.appauth.browser.BrowserAllowList
import net.openid.appauth.browser.VersionedBrowserMatcher
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONException
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

sealed class AuthStateStatus {
	sealed class Unauthorized : AuthStateStatus() {
		object UserAuthRequired : Unauthorized()
		object PendingToken : Unauthorized()
		object NotValidRole : Unauthorized()
	}

	data class Authorized(val state: AuthState) : AuthStateStatus()
	data class Error(val exception: Exception) : AuthStateStatus()
}

data class UserState(val authState: AuthState, val validRole: Boolean)

/**
 * OAuth specific exception.
 */
class UnauthorizedException(original: AuthorizationException) : Exception(original)

object AuthManager {

	private fun UserState.toStatus(): AuthStateStatus {
		return when {
			authState.authorizationException != null -> AuthStateStatus.Error(
				UnauthorizedException(
					authState.authorizationException!!
				)
			)
			!validRole -> AuthStateStatus.Unauthorized.NotValidRole
			authState.isAuthorized -> AuthStateStatus.Authorized(authState)
			authState.lastAuthorizationResponse != null && authState.needsTokenRefresh -> AuthStateStatus.Unauthorized.PendingToken
			else -> AuthStateStatus.Unauthorized.UserAuthRequired
		}
	}

	private const val TAG = "AuthManager"

	private const val REDIRECT_URL: String =
		"noi-community://oauth2redirect/login-callback"
	private const val END_SESSION_URL = "noi-community://oauth2redirect/end_session-callback"
	private const val CLIENT_ID: String = "it.bz.noi.community"

	private const val PREF_AUTH_STATE = "authState"
	private const val PREF_ACCESS_GRANTED_STATE = "accessGrantedState"
	private const val ACCESS_GRANTED_ROLE = "ACCESS_GRANTED"

	private val mainCoroutineScope = CoroutineScope(Dispatchers.Main + Job())

	private val interceptor = HttpLoggingInterceptor().apply {
		setLevel(HttpLoggingInterceptor.Level.BODY)
	}
	private val client = OkHttpClient.Builder().addInterceptor(interceptor)
		.build()

	lateinit var application: Application

	/**
	 * Must be called in Application's onCreate.
	 */
	fun setup(application: Application) {
		this.application = application
	}

	private val authorizationService: AuthorizationService by lazy {
		AuthorizationService(application, configuration())
	}

	private fun configuration() = AppAuthConfiguration.Builder()
		.setBrowserMatcher(
			BrowserAllowList(
				VersionedBrowserMatcher.FIREFOX_CUSTOM_TAB,
				VersionedBrowserMatcher.CHROME_CUSTOM_TAB,
				VersionedBrowserMatcher.SAMSUNG_CUSTOM_TAB
			)
		)
		.build()

	@OptIn(ObsoleteCoroutinesApi::class)
	private val userState: MutableSharedFlow<UserState> by lazy {
		MutableSharedFlow<UserState>(1, 0, BufferOverflow.DROP_OLDEST).apply {
			tryEmit(readAuthState())
		}
	}

	@OptIn(ObsoleteCoroutinesApi::class)
	val status: Flow<AuthStateStatus> by lazy {
		userState.filterNotNull().map { state ->
			state.toStatus()
		}.distinctUntilChanged()
	}

	private val reloadTickerFlow = MutableSharedFlow<Unit>(replay = 1).apply {
		tryEmit(Unit)
	}

	val userInfo: StateFlow<Resource<UserInfo>?> by lazy {
		status.flatMapLatest {
			when (it) {
				is AuthStateStatus.Authorized,
				is AuthStateStatus.Unauthorized.NotValidRole -> {
					//getUserInfo()
					 reloadableUserInfoFlow()
				}
				else -> flowOf(null)
			}
		}.stateIn(mainCoroutineScope, SharingStarted.Lazily,null)
	}

	fun relaodUserInfo() {
		reloadTickerFlow.tryEmit(Unit)
	}

	private fun reloadableUserInfoFlow() =
		reloadTickerFlow.flatMapLatest { getUserInfo() }

	private suspend fun blockingNetworkRequest(request: Request) = withContext(Dispatchers.IO) {
		client.newCall(request).execute()
	}

	@OptIn(ObsoleteCoroutinesApi::class)
	private suspend fun obtainAuthServiceConfig(): AuthorizationServiceConfiguration =
		suspendCoroutine { cont ->
			AuthorizationServiceConfiguration.fetchFromIssuer(
				Uri.parse(BuildConfig.ISSUER_URL)
			) { serviceConfiguration, ex ->
				if (ex != null) {
					FirebaseCrashlytics.getInstance().recordException(ex)
					cont.resumeWithException(ex)
				} else if (serviceConfiguration != null) {
					try {
						cont.resume(serviceConfiguration)
					} catch (ex: Exception) {
						cont.resumeWithException(ex)
					}
				}
			}
		}

	@OptIn(ObsoleteCoroutinesApi::class)
	private suspend fun obtainFreshToken(): String {
		val userState = userState.first()

		return suspendCoroutine { cont ->
			userState.authState.performActionWithFreshTokens(
				authorizationService
			) { accessToken, _, ex ->
				if (ex != null) {
					FirebaseCrashlytics.getInstance().recordException(ex)
					if (ex.type != AuthorizationException.TYPE_GENERAL_ERROR) {
						refreshState()
					}
					cont.resumeWithException(ex)
				} else if (accessToken != null) {
					try {
						cont.resume(accessToken)
					} catch (ex: Exception) {
						cont.resumeWithException(ex)
					}
				}
			}
		}
	}

	private suspend fun getUserInfo() = flow {
		val authServiceConfig = obtainAuthServiceConfig()
		val userInfo = getUserInfo(authServiceConfig)
		emit(userInfo)
	}

	private suspend fun getUserInfo(authServiceConfig: AuthorizationServiceConfiguration): Resource<UserInfo> {
		val userinfoEndpoint = authServiceConfig.discoveryDoc?.userinfoEndpoint
		return if (userinfoEndpoint != null) {
			try {
				val accessToken = obtainFreshToken()
				fetchUserInfo(accessToken, userinfoEndpoint)
			} catch (ex: AuthorizationException) {
				Resource.error(data = null, message = "AuthorizationException: ${ex.error}")
			}
		} else {
			Resource.error(data = null, message = "Missing discoveryDoc userinfoEndpoint")
		}
	}

	private suspend fun fetchUserInfo(
		accessToken: String,
		userInfoEndpoint: Uri
	): Resource<UserInfo> {
		try {
			val request: Request = Request.Builder()
				.url(userInfoEndpoint.toString())
				.addHeader("Authorization", "Bearer $accessToken")
				.get()
				.build()

			val response = blockingNetworkRequest(request)
			if (response.isSuccessful) {
				val responseBody = response.body?.string()
				val userInfo = parseUserInfoResponse(responseBody!!)
				return Resource.success(userInfo)
			} else {
				Log.d(
					TAG,
					"User info failed with response code ${response.code}: ${response.message}"
				)
				return Resource.error(
					data = null,
					message = "User info failed with response code ${response.code}"
				)
			}
		} catch (ioEx: IOException) {
			Log.e(TAG, "Network error when querying userinfo endpoint", ioEx)
			return Resource.error(
				data = null,
				message = "Network error when querying userinfo endpoint"
			)
		} catch (jsonEx: JSONException) {
			Log.e(TAG, "Failed to parse userinfo response")
			return Resource.error(data = null, message = "Failed to parse userinfo response")
		}
	}

	private suspend fun parseUserInfoResponse(userInfoResponse: String): UserInfo =
		withContext(Dispatchers.Default) {
			val gson = GsonBuilder().create()
			gson.fromJson(userInfoResponse, UserInfo::class.java)
		}

	fun login(context: Activity, requestCode: Int) = mainCoroutineScope.launch {
		val authServiceConfig = obtainAuthServiceConfig()
		login(authServiceConfig, context, requestCode)
	}

	private fun login(
		authServiceConfig: AuthorizationServiceConfiguration,
		context: Activity,
		requestCode: Int
	) {
		val authRequest = AuthorizationRequest.Builder(
			authServiceConfig,
			CLIENT_ID,
			ResponseTypeValues.CODE,
			Uri.parse(REDIRECT_URL)
		)
			.setPrompt(AuthorizationRequest.Prompt.LOGIN)
			.setScopes(AuthorizationRequest.Scope.OPENID, AuthorizationRequest.Scope.PROFILE)
			.build()

		try {
			val intent = authorizationService.getAuthorizationRequestIntent(authRequest)
			context.startActivityForResult(intent, requestCode)
		} catch (e: ActivityNotFoundException) {
			AlertDialog.Builder(context).apply {
				// TODO export stringhe
				setTitle("Error")
				setMessage("È necessario aver installato un browser compatibile (Chrome, Firefox o Samsung Browser) per proseguire con l\\'autenticazione. Se lo hai già installato riavvia l\\'app e riprova.")
				setPositiveButton("Ok")
				{ dialogInterFace: DialogInterface, _ ->
					dialogInterFace.dismiss()
				}
				show()
			}
		}
	}

	fun onAuthorization(response: AuthorizationResponse?, exception: AuthorizationException?) {
		runBlocking {
			userState.first().let { state ->
				state.authState.update(response, exception)
				writeAuthState(state)
				if (state.authState.lastAuthorizationResponse != null) {
					obtainToken(state.authState.lastAuthorizationResponse!!, state.validRole)
				}
			}
		}
	}

	private fun obtainToken(authResponse: AuthorizationResponse, currentValidRole: Boolean) {
		authorizationService.performTokenRequest(
			authResponse.createTokenExchangeRequest()
		) { response, ex ->
			if (ex != null) {
				Log.d(TAG, "Token request result", ex)
				onTokenObtained(response, ex, currentValidRole)
			} else {
				response!!.accessToken?.let { jwtToken ->
					decode(jwtToken)?.let { decodedToken ->
						if (verifyToken(decodedToken)) {
							onTokenObtained(response, ex, true)
						} else {
							// FIXME
							Log.d(TAG, "Not authorized")
							onTokenObtained(response, ex, false)
						}
					}
				}
			}
		}
	}

	private fun verifyToken(token: NOIJwtAccessToken) =
		token.checkResourceAccessRoles(CLIENT_ID, listOf(ACCESS_GRANTED_ROLE))

	private fun onTokenObtained(
		tokenResponse: TokenResponse?,
		exception: AuthorizationException?,
		validRole: Boolean
	) {
		runBlocking {
			userState.first().let { currentState ->
				val newState = UserState(currentState.authState, validRole)
				newState.authState.update(tokenResponse, exception)
				writeAuthState(newState)
				userState.emit(newState)
			}
		}
	}

	fun logout(context: Activity, requestCode: Int) = mainCoroutineScope.launch {
		val authServiceConfig = obtainAuthServiceConfig()
		logout(authServiceConfig, context, requestCode)
	}

	private suspend fun logout(
		authServiceConfig: AuthorizationServiceConfiguration,
		context: Activity,
		requestCode: Int
	) {
		userState.first().let { currentUserState ->
			if (authServiceConfig.endSessionEndpoint != null) {
				val endSessionIntent: Intent = authorizationService.getEndSessionRequestIntent(
					EndSessionRequest.Builder(authServiceConfig)
						.setIdTokenHint(currentUserState.authState.idToken)
						.setPostLogoutRedirectUri(Uri.parse(END_SESSION_URL))
						.build()
				)
				try {
					context.startActivityForResult(endSessionIntent, requestCode)
				} catch (ex: ActivityNotFoundException) {
					Log.e(TAG, "End session error: " + ex.toString())
				}

			}
		}
	}

	fun onEndSession() {
		deleteUserState()
		runBlocking { userState.emit(UserState(AuthState(), true)) }
	}

	@OptIn(ObsoleteCoroutinesApi::class)
	private fun refreshState() {
		runBlocking {
			userState.first().let { state ->
				writeAuthState(state)
				userState.emit(state)
			}
		}
	}

	private fun readAuthState(): UserState {
		application.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE).apply {
			val authState = getString(PREF_AUTH_STATE, null)?.let {
				AuthState.jsonDeserialize(it)
			} ?: AuthState()

			val isValidRole = getBoolean(PREF_ACCESS_GRANTED_STATE, true)

			return UserState(authState, isValidRole)
		}

	}

	private fun writeAuthState(userState: UserState) {
		application.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE).edit {
			putString(PREF_AUTH_STATE, userState.authState.jsonSerializeString())
			putBoolean(PREF_ACCESS_GRANTED_STATE, userState.validRole)
		}
	}

	private fun deleteUserState() {
		application.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE).edit {
			remove(PREF_AUTH_STATE)
			remove(PREF_ACCESS_GRANTED_STATE)
		}
	}

}
