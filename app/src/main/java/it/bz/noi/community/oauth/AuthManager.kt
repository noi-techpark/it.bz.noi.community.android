package it.bz.noi.community.oauth

import android.app.Activity
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.edit
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.GsonBuilder
import it.bz.noi.community.BuildConfig
import it.bz.noi.community.NoiApplication.Companion.SHARED_PREFS_NAME
import it.bz.noi.community.R
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

@OptIn(ExperimentalCoroutinesApi::class, ObsoleteCoroutinesApi::class)
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

	private val userState: MutableSharedFlow<UserState> by lazy {
		MutableSharedFlow<UserState>(1, 0, BufferOverflow.DROP_OLDEST).apply {
			tryEmit(readAuthState())
		}
	}

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
					reloadableUserInfoFlow()
				}
				else -> flowOf(null)
			}
		}.stateIn(mainCoroutineScope, SharingStarted.Lazily, null)
	}

	fun relaodUserInfo() {
		reloadTickerFlow.tryEmit(Unit)
	}

	private fun reloadableUserInfoFlow() =
		reloadTickerFlow.flatMapLatest { getUserInfo() }

	private suspend fun blockingNetworkRequest(request: Request) = withContext(Dispatchers.IO) {
		client.newCall(request).execute()
	}

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

	suspend fun obtainFreshToken(): String {
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

	private fun refreshState() {
		mainCoroutineScope.launch {
			userState.first().let { state ->
				writeAuthState(state)
				userState.emit(state)
			}
		}
	}

	private suspend fun getUserInfo() = flow {
		emit(Resource.loading(null))
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
			return if (response.isSuccessful) {
				val responseBody = response.body?.string()
				val userInfo = parseUserInfoResponse(responseBody!!)
				Resource.success(userInfo)
			} else {
				Log.d(
					TAG,
					"User info failed with response code ${response.code}: ${response.message}"
				)
				Resource.error(
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
			MaterialAlertDialogBuilder(context).apply {
				setTitle(context.getString(R.string.error_title))
				setMessage(context.getString(R.string.login_browser_error_msg))
				setPositiveButton(context.getString(R.string.ok_button)) { _, _ -> }
				show()
			}
		}
	}

	fun onAuthorization(response: AuthorizationResponse?, exception: AuthorizationException?) {
		mainCoroutineScope.launch {
			val state = userState.first()
			state.authState.update(response, exception)
			writeAuthState(state)
			val authResponse = state.authState.lastAuthorizationResponse
			if (authResponse != null) {
				try {
					val tokenResponse = obtainToken(authResponse)
					tokenResponse.accessToken?.let { jwtToken ->
						decode(jwtToken)?.let { decodedToken ->
							if (verifyToken(decodedToken)) {
								Log.d(TAG, "Access granted role check: true")
								onTokenObtained(tokenResponse, null, true)
							} else {
								Log.d(TAG, "Access granted role check: false")
								onTokenObtained(tokenResponse, null, false)
							}
						}
					}
				} catch (ex: AuthorizationException) {
					onTokenObtained(null, ex, state.validRole)
				}

			}
		}
	}

	private suspend fun obtainToken(authResponse: AuthorizationResponse): TokenResponse =
		suspendCoroutine { cont ->
			authorizationService.performTokenRequest(
				authResponse.createTokenExchangeRequest()
			) { response, ex ->
				if (ex != null) {
					FirebaseCrashlytics.getInstance().recordException(ex)
					cont.resumeWithException(ex)
				} else if (response != null) {
					try {
						cont.resume(response)
					} catch (ex: Exception) {
						cont.resumeWithException(ex)
					}
				}
			}
		}

	private fun verifyToken(token: NOIJwtAccessToken) =
		token.checkResourceAccessRoles(CLIENT_ID, listOf(ACCESS_GRANTED_ROLE))

	private suspend fun onTokenObtained(
		tokenResponse: TokenResponse?,
		exception: AuthorizationException?,
		validRole: Boolean
	) {
		val newState = UserState(userState.first().authState, validRole)
		newState.authState.update(tokenResponse, exception)
		writeAuthState(newState)
		userState.emit(newState)
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
		val currentUserState = userState.first()
		val endSessionIntent: Intent = authorizationService.getEndSessionRequestIntent(
			EndSessionRequest.Builder(authServiceConfig)
				.setIdTokenHint(currentUserState.authState.idToken)
				.setPostLogoutRedirectUri(Uri.parse(END_SESSION_URL))
				.build()
		)
		try {
			context.startActivityForResult(endSessionIntent, requestCode)
		} catch (ex: ActivityNotFoundException) {
			Log.e(TAG, "End session error: $ex")
			MaterialAlertDialogBuilder(context).apply {
				setTitle(context.getString(R.string.error_title))
				setMessage(context.getString(R.string.logout_error_msg))
				setPositiveButton(context.getString(R.string.ok_button)) { _, _ -> }
				show()
			}
		}

	}

	fun onEndSession() {
		deleteUserState()
		mainCoroutineScope.launch { userState.emit(UserState(AuthState(), true)) }
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
