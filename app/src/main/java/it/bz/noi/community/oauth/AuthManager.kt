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
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.edit
import com.google.firebase.crashlytics.FirebaseCrashlytics
import it.bz.noi.community.MainActivity.Companion.END_SESSION_REQUEST_CODE
import it.bz.noi.community.SplashScreenActivity.Companion.SHARED_PREFS_NAME
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import net.openid.appauth.*
import net.openid.appauth.AuthorizationServiceConfiguration.RetrieveConfigurationCallback
import net.openid.appauth.browser.BrowserAllowList
import net.openid.appauth.browser.VersionedBrowserMatcher
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

sealed class AuthStateStatus {
	sealed class Unauthorized : AuthStateStatus() {
		object UserAuthRequired : Unauthorized()
		object PendingToken : Unauthorized()
		object NotValidRole : Unauthorized()
	}

	data class Authorized(val state: AuthState) : AuthStateStatus()
	data class Error(val exception: Exception) : AuthStateStatus()
}

/**
 * OAuth specific exception.
 */
class UnauthorizedException(original: AuthorizationException) : Exception(original)

object AuthManager {

	private fun AuthState.toStatus(): AuthStateStatus {
		return when {
			authorizationException != null -> AuthStateStatus.Error(UnauthorizedException(authorizationException!!))
			isAuthorized -> AuthStateStatus.Authorized(this)
			lastAuthorizationResponse != null && needsTokenRefresh -> AuthStateStatus.Unauthorized.PendingToken
			else -> AuthStateStatus.Unauthorized.UserAuthRequired
		}
	}

	private const val TAG = "AuthManager"

	// *************** TODO SPOSTARE in BUILD CONFIG?
	private const val REDIRECT_URL: String =
		"noi-community://oauth2redirect/login-callback"
	private const val END_SESSION_URL = "noi-community://oauth2redirect/end_session-callback"
	private const val ISSUER_URL: String = "https://auth.opendatahub.testingmachine.eu/auth/realms/noi/"
	private const val CLIENT_ID: String = "it.bz.noi.community"
	// ***************

	private const val PREF_AUTH_STATE = "authState"
	private const val ACCESS_GRANTED_ROLE = "ACCESS_GRANTED"

	lateinit var application: Application

	/**
	 * Must be called in Application's onCreate.
	 */
	fun setup(application: Application) {
		this.application = application
	}

	@OptIn(ObsoleteCoroutinesApi::class)
	private val authState: MutableSharedFlow<AuthState> by lazy {
		MutableSharedFlow<AuthState>(1, 0, BufferOverflow.DROP_OLDEST).apply {
			tryEmit(readAuthState())
		}
	}

	@OptIn(ObsoleteCoroutinesApi::class)
	fun refreshState() {
		runBlocking {
			authState.first().let { state ->
				writeAuthState(state)
				authState.emit(state)
			}
		}
	}

	@OptIn(ObsoleteCoroutinesApi::class)
	val status: Flow<AuthStateStatus> by lazy {
		authState.filterNotNull().map { state -> state.toStatus() }.distinctUntilChanged()
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
			Uri.parse(ISSUER_URL),
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
			authState.first().let { state ->
				state.update(response, exception)
				writeAuthState(state)
				if (state.lastAuthorizationResponse != null) {
					obtainToken(state.lastAuthorizationResponse!!)
				}
			}
		}
	}

	private fun obtainToken(authResponse: AuthorizationResponse) {
		authorizationService.performTokenRequest(
			authResponse.createTokenExchangeRequest()
		) { response, ex ->
			if (ex != null) {
				Log.d(TAG, "Token request result", ex)
				onTokenObtained(response, ex)
			} else {
				response!!.accessToken?.let { jwtToken ->
					decode(jwtToken)?.let { decodedToken ->
						if (verifyToken(decodedToken)) {
							onTokenObtained(response, ex)
						} else {
							// FIXME
							Log.d(TAG, "Not authorized")
						}
					}
				}
			}
		}
	}

	private fun verifyToken(token: NOIJwtAccessToken) = token.checkResourceAccessRoles(CLIENT_ID, listOf(ACCESS_GRANTED_ROLE))

	private fun onTokenObtained(tokenResponse: TokenResponse?, exception: AuthorizationException?) {
		runBlocking {
			authState.first().let { state ->
				state.update(tokenResponse, exception)
				writeAuthState(state)
				authState.emit(state)
			}
		}
	}

	// FIXME
	fun logout(context: Activity, requestCode: Int) {
		AuthorizationServiceConfiguration.fetchFromIssuer(
			Uri.parse(ISSUER_URL),
			RetrieveConfigurationCallback { serviceConfiguration, ex ->
				if (ex != null) {
					Log.e(TAG, "failed to fetch configuration")
					return@RetrieveConfigurationCallback
				}

				logout(serviceConfiguration!!, context, requestCode)
			})
	}

	private fun logout(authServiceConfig: AuthorizationServiceConfiguration, context: Activity, requestCode: Int) {
		// TODO

		runBlocking {
			authState.first().let { currentState ->
				if (authServiceConfig.endSessionEndpoint != null) {
					val endSessionIntent: Intent = authorizationService.getEndSessionRequestIntent(
						EndSessionRequest.Builder(authServiceConfig)
							.setIdTokenHint(currentState.idToken)
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
		deleteAuthState()
		runBlocking { authState.emit(AuthState()) }
	}

	private fun readAuthState(): AuthState {
		return application.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE).getString(PREF_AUTH_STATE, null)?.let {
			AuthState.jsonDeserialize(it)
		} ?: AuthState()
	}

	private fun writeAuthState(state: AuthState) {
		application.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE).edit {
			putString(PREF_AUTH_STATE, state.jsonSerializeString())
		}
	}

	private fun deleteAuthState() {
		application.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE).edit {
			remove(PREF_AUTH_STATE)
		}
	}

}
