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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.GsonBuilder
import it.bz.noi.community.BuildConfig
import it.bz.noi.community.SplashScreenActivity.Companion.SHARED_PREFS_NAME
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import net.openid.appauth.*
import net.openid.appauth.AuthorizationServiceConfiguration.RetrieveConfigurationCallback
import net.openid.appauth.browser.BrowserAllowList
import net.openid.appauth.browser.VersionedBrowserMatcher
import okio.buffer
import okio.source
import org.json.JSONException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

sealed class AuthStateStatus {
	object Initial: AuthStateStatus()

	sealed class Unauthorized : AuthStateStatus() {
		object UserAuthRequired : Unauthorized()
		object PendingToken : Unauthorized()
		object NotValidRole : Unauthorized()
	}

	data class Authorized(val state: AuthState) : AuthStateStatus()
	data class Error(val exception: Exception) : AuthStateStatus()
}

data class UserState(val authState: AuthState, val validRole: Boolean, val initial: Boolean)

/**
 * OAuth specific exception.
 */
class UnauthorizedException(original: AuthorizationException) : Exception(original)

object AuthManager {

	private fun UserState.toStatus(): AuthStateStatus {
		return when {
			authState.authorizationException != null -> AuthStateStatus.Error(UnauthorizedException(authState.authorizationException!!))
			initial -> AuthStateStatus.Initial
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

	lateinit var application: Application

	private var _userInfo = MutableLiveData<UserInfo?>(null)
	val userInfo: LiveData<UserInfo?>
		get() = _userInfo

	/**
	 * Must be called in Application's onCreate.
	 */
	fun setup(application: Application) {
		this.application = application
	}

	@OptIn(ObsoleteCoroutinesApi::class)
	private val userState: MutableSharedFlow<UserState> by lazy {
		MutableSharedFlow<UserState>(1, 0, BufferOverflow.DROP_OLDEST).apply {
			tryEmit(readAuthState())
		}
	}

	@OptIn(ObsoleteCoroutinesApi::class)
	val status: Flow<AuthStateStatus> by lazy {
		userState.filterNotNull().map { state -> state.toStatus() }.distinctUntilChanged()
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

	fun login(context: Activity, requestCode: Int) {
		AuthorizationServiceConfiguration.fetchFromIssuer(
			Uri.parse(BuildConfig.ISSUER_URL),
			RetrieveConfigurationCallback { serviceConfiguration, ex ->
				if (ex != null) {
					Log.e(TAG, "failed to fetch configuration")
					return@RetrieveConfigurationCallback
				}

				login(serviceConfiguration!!, context, requestCode)
			})
	}

	private fun login(authServiceConfig: AuthorizationServiceConfiguration, context: Activity, requestCode: Int) {
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
				writeAuthState(state.copy(initial = false))
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

						fetchUserInfo()
					}
				}
			}
		}
	}

	private fun verifyToken(token: NOIJwtAccessToken) = token.checkResourceAccessRoles(CLIENT_ID, listOf(ACCESS_GRANTED_ROLE))

	private fun onTokenObtained(tokenResponse: TokenResponse?, exception: AuthorizationException?, validRole: Boolean) {
		runBlocking {
			userState.first().let { currentState ->
				val newState = UserState(currentState.authState, validRole, false)
				newState.authState.update(tokenResponse, exception)
				writeAuthState(newState)
				userState.emit(newState)
			}
		}
	}

	// FIXME
	fun logout(context: Activity, requestCode: Int) {
		AuthorizationServiceConfiguration.fetchFromIssuer(
			Uri.parse(BuildConfig.ISSUER_URL),
			RetrieveConfigurationCallback { serviceConfiguration, ex ->
				if (ex != null) {
					Log.e(TAG, "failed to fetch configuration")
					return@RetrieveConfigurationCallback
				}

				logout(serviceConfiguration!!, context, requestCode)
			})
	}

	private fun logout(authServiceConfig: AuthorizationServiceConfiguration, context: Activity, requestCode: Int) {
		runBlocking {
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
	}

	fun onEndSession() {
		deleteUserState()
		runBlocking { userState.emit(UserState(AuthState(), true, false)) }
	}

	@OptIn(ObsoleteCoroutinesApi::class)
	suspend fun <T> doAuthenticatedAction(action: (token: String) -> T): T {
		val userState = userState.first()

		val token = suspendCoroutine<String> { cont ->
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
				writeAuthState(userState)
			}
		}
		return action(token)
	}

	@OptIn(ObsoleteCoroutinesApi::class)
	private fun refreshState() {
		runBlocking {
			userState.first().let { state ->
				writeAuthState(state)
				userState.emit(state.copy(initial = false))
			}
		}
	}

	// FIXME
	fun fetchUserInfo()  {
			AuthorizationServiceConfiguration.fetchFromIssuer(
				Uri.parse(BuildConfig.ISSUER_URL),
				RetrieveConfigurationCallback { serviceConfiguration, ex ->
					if (ex != null) {
						Log.e(TAG, "failed to fetch configuration")
						return@RetrieveConfigurationCallback
					}
					serviceConfiguration!!.discoveryDoc?.userinfoEndpoint?.let {

							fetchUserInfo(it)

					} ?: throw Exception("missing userinfoEndpoint")

				})
	}

	private fun fetchUserInfo(userInfoEndpoint: Uri)  {
		runBlocking {
			userState.first().authState
		}.let { authState ->
			authState.performActionWithFreshTokens(
				authorizationService,
			) { accessToken, _, ex ->
				if (ex == null) {

					GlobalScope.launch(Dispatchers.IO) {
						try {

							val conn = URL(userInfoEndpoint.toString()).openConnection() as HttpURLConnection
							conn.setRequestProperty("Authorization", "Bearer $accessToken")
							conn.instanceFollowRedirects = false
							conn.connect()
							when (val responseCode = conn.responseCode) {
								in 300..399,
								in 200..299 -> {
									val response = conn.inputStream.source().buffer()
										.readString(Charset.forName("UTF-8"))
									val gson = GsonBuilder().create()
									_userInfo.postValue(gson.fromJson(response, UserInfo::class.java))
								}
								else -> {
									Log.d(TAG, "User info failed with response code $responseCode: ${conn.responseMessage}")
								}
							}

						} catch (ioEx: IOException) {
							Log.e(
								TAG,
								"Network error when querying userinfo endpoint",
								ioEx
							)
						} catch (jsonEx: JSONException) {
							Log.e(TAG, "Failed to parse userinfo response")
						}
						refreshState()
					}
				}
				else {
					refreshState()
				}
			}
		}
	}

	private fun readAuthState(): UserState {
		application.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE).apply {
			val authState = getString(PREF_AUTH_STATE, null)?.let {
				AuthState.jsonDeserialize(it)
			} ?: AuthState()

			val isValidRole = getBoolean(PREF_ACCESS_GRANTED_STATE, true)

			return UserState(authState, isValidRole, true)
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
		_userInfo.value = null
	}

	fun retrieveUserInfo() {
		if (userInfo.value == null) {
			fetchUserInfo()
		}
	}

}
