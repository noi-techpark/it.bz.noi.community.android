package it.bz.noi.community.oauth

import android.app.Activity
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.edit
import com.google.firebase.crashlytics.FirebaseCrashlytics
import it.bz.noi.community.SplashScreenActivity.Companion.SHARED_PREFS_NAME
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import net.openid.appauth.*
import net.openid.appauth.browser.BrowserAllowList
import net.openid.appauth.browser.VersionedBrowserMatcher
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

sealed class AuthStateStatus_copy {
	sealed class Unauthorized : AuthStateStatus_copy() {
		object UserAuthRequired : Unauthorized()
		object PendingToken : Unauthorized()
		object UserAuthProcessing : Unauthorized()
	}

	data class Authorized(val state: AuthState) : AuthStateStatus_copy()
	data class Error(val exception: Exception) : AuthStateStatus_copy()
}

/**
 * OAuth specific exception.
 */
class UnauthorizedException_copy(original: AuthorizationException) : Exception(original)

object AuthManager_copy {

	private fun AuthState.toStatus(userAuthRequired: Boolean): AuthStateStatus_copy {
		return when (userAuthRequired) {
			true -> AuthStateStatus_copy.Unauthorized.UserAuthProcessing
			else -> when {
				authorizationException != null -> AuthStateStatus_copy.Error(UnauthorizedException_copy(authorizationException!!))
				isAuthorized -> AuthStateStatus_copy.Authorized(this)
				lastAuthorizationResponse != null && needsTokenRefresh -> AuthStateStatus_copy.Unauthorized.PendingToken
				else -> AuthStateStatus_copy.Unauthorized.UserAuthRequired
			}
		}
	}

	private const val TAG = "AuthManager"

	// *************** TODO SPOSTARE in BUILD CONFIG?
	private const val REDIRECT_URL: String =
		"noi-community://oauth2redirect/login-callback" // FIXME ricontrollare: Scheme must match appAuthRedirectScheme in the manifest
	private const val AUTH_ENDPOINT: String = "https://auth.opendatahub.testingmachine.eu/auth/realms/noi/protocol/openid-connect"
	private const val CLIENT_ID: String = "it.bz.noi.community"
	// ***************

	//private const val SHARED_PREFS = "auth"
	private const val PREF_AUTH_STATE = "authState"
	private const val PREF_VERSION = "version"
	private const val VERSION = 1
	private const val USE_LOGOUT_CHROME_TABS = false

	lateinit var application: Application

	/**
	 * Se true, stiamo attendendo l'autorizzazione dell'utente.
	 */
	private var userAuthRequired: Boolean = false

	/**
	 * Must be called in Application's onCreate.
	 */
	fun setup(application: Application) {
		this.application = application
	}

	private fun createAuthState() = AuthState(
		AuthorizationServiceConfiguration(
			Uri.parse("$AUTH_ENDPOINT/oauth2/auth"),  // authorization endpoint
			Uri.parse("$AUTH_ENDPOINT/oauth2/token") // token endpoint
		)
	)

	/**
	 * AuthState source of truth.
	 */
	@OptIn(ObsoleteCoroutinesApi::class)
	private val authState: MutableSharedFlow<AuthState> by lazy {
		MutableSharedFlow<AuthState>(1, 0, BufferOverflow.DROP_OLDEST).apply {
			tryEmit(loadAuthState() ?: createAuthState().also {
				saveAuthState(it)
			})
		}
	}

	/**
	 * Flow of [AuthStateStatus_copy]. Basically is a simlified [authState].
	 */
	@OptIn(ObsoleteCoroutinesApi::class)
	val status: Flow<AuthStateStatus_copy> by lazy {
		authState.filterNotNull().map { state ->
			state.toStatus(userAuthRequired)
		}
	}

	private fun AppAuthConfiguration.Builder.setMatchCustomTabs() = setBrowserMatcher(
		BrowserAllowList(
			VersionedBrowserMatcher.FIREFOX_CUSTOM_TAB,
			VersionedBrowserMatcher.CHROME_CUSTOM_TAB,
			VersionedBrowserMatcher.SAMSUNG_CUSTOM_TAB
		)
	)

	private fun configuration() = AppAuthConfiguration.Builder()
		.setMatchCustomTabs()
		.build()

	private val authorizationService: AuthorizationService by lazy {
		AuthorizationService(application, configuration())
	}

	@OptIn(ObsoleteCoroutinesApi::class)
	fun refreshToken() {
		userAuthRequired = false
		runBlocking { authState.first() }.lastAuthorizationResponse?.let { authResponse ->
			authorizationService.performTokenRequest(
				authResponse.createTokenExchangeRequest(),
				ClientSecretBasic(ClientSecretBasic.NAME) // FIXME
			) { response, ex ->
				ex?.let {
					Log.d(TAG, "Token request result", ex)
				}
				onTokenObtained(response, ex)
			}
		}
	}

	@OptIn(ObsoleteCoroutinesApi::class)
	fun refreshState() {
		runBlocking {
			authState.first().let { state ->
				saveAuthState(state)
				authState.emit(state)
			}
		}
	}

	@OptIn(ObsoleteCoroutinesApi::class)
	fun authorize(response: AuthorizationResponse?, exception: AuthorizationException?) {
		userAuthRequired = false
		runBlocking {
			authState.first().let { state ->
				state.update(response, exception)
				saveAuthState(state)
				authState.emit(state)
			}
		}
	}

	@OptIn(ObsoleteCoroutinesApi::class)
	fun authorizeLogout(response: AuthorizationResponse?, exception: AuthorizationException?) {
		runBlocking {
			authState.first().let {
				val freshState = createAuthState()
				saveAuthState(freshState)
				authState.emit(freshState)
			}
		}
	}

	@OptIn(DelicateCoroutinesApi::class)
	fun offlineLogout(context: Activity) {
		GlobalScope.launch {
			clear()
		}
	}

	@OptIn(ObsoleteCoroutinesApi::class)
	private suspend fun clear() {
		delete()
		this@AuthManager_copy.authState.emit(createAuthState())
	}

	@OptIn(ObsoleteCoroutinesApi::class, DelicateCoroutinesApi::class)
	fun onlineLogout(context: Activity, requestCode: Int) {
		runBlocking {
			authState.first()
		}.let { authState ->
			//authState.needsTokenRefresh = true
			authState.performActionWithFreshTokens(
				authorizationService,
				ClientSecretBasic(ClientSecretBasic.NAME) // FIXME
			) { _, idToken, ex ->
				if (ex == null) {
					GlobalScope.launch(Dispatchers.IO) {

						val redirectUrl = URLEncoder.encode(
							"$AUTH_ENDPOINT/logout",
							"UTF-8"
						)
						val state = 17

						// TODO verificare come costruire URL
						val url =
							"$AUTH_ENDPOINT/oidc/logout?id_token_hint=$idToken&post_logout_redirect_uri=$redirectUrl&state=$state"

						if (USE_LOGOUT_CHROME_TABS) {
							CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(url)) //
						} else {
							try {
								val connection = URL(url).openConnection() as HttpURLConnection
								connection.connect()
								when (val responseCode = connection.responseCode) {
									in 300..399,
									in 200..299 -> {
										clear()
									}
									else -> {
										Log.d(TAG, "Logout failed $responseCode")
									}
								}
							} catch (ex: IOException) {
								Log.d(TAG, "Logout failed with exception $ex")
							}
						}
					}
				} else {
					FirebaseCrashlytics.getInstance().recordException(ex)
				}
				saveAuthState(authState)
			}
		}
	}

	@OptIn(ObsoleteCoroutinesApi::class)
	fun login(context: Activity, requestCode: Int) {
		userAuthRequired = true
		val authServiceConfig = runBlocking {
			authState.first().authorizationServiceConfiguration!!
		}

		val authRequest = AuthorizationRequest.Builder(
			authServiceConfig,
			CLIENT_ID,
			ResponseTypeValues.CODE,
			Uri.parse(REDIRECT_URL)
		)
			.setPrompt(AuthorizationRequest.Prompt.LOGIN)
			.setScope(AuthorizationRequest.Scope.OPENID)
			.build()

		try {
			val intent = authorizationService.getAuthorizationRequestIntent(authRequest)
			context.startActivityForResult(intent, requestCode)
		} catch (e: ActivityNotFoundException) {
			AlertDialog.Builder(context)
			AlertDialog.Builder(context).apply {
				// TODO export stringhe
				setTitle("Error")
				//(context.getString(R.string.error))
				setMessage("È necessario aver installato un browser compatibile (Chrome, Firefox o Samsung Browser) per proseguire con l\\'autenticazione. Se lo hai già installato riavvia l\\'app e riprova.")
				//(context.getString(R.string.login_error_msg))
				setPositiveButton("Ok")
				//(context.getString(R.string.btn_ok))
				{ dialogInterFace: DialogInterface, _ ->
					dialogInterFace.dismiss()
				}
				show()
			}
		}
	}

	@OptIn(ObsoleteCoroutinesApi::class)
	suspend fun <T> doAction(action: (token: String) -> T): T {

		val authState = authState.first()

		val token = suspendCoroutine<String> { cont ->
			//authState.needsTokenRefresh = true
			authState.performActionWithFreshTokens(
				authorizationService,
				ClientSecretBasic(ClientSecretBasic.NAME) // FIXME
			) { accessToken, _, ex ->
				if (ex != null) {
					FirebaseCrashlytics.getInstance().recordException(ex)
					if (ex.type != AuthorizationException.TYPE_GENERAL_ERROR) {
						refreshState()
					}
					cont.resumeWithException(ex)
				} else if (accessToken != null) {
					try {
						cont.resume(accessToken) // FIXME
					} catch (ex: Exception) {
						cont.resumeWithException(ex)
					}
				}
				saveAuthState(authState)
			}
		}
		return action(token)
	}

	@OptIn(ObsoleteCoroutinesApi::class)
	private fun onTokenObtained(tokenResponse: TokenResponse?, exception: AuthorizationException?) {
		userAuthRequired = false
		runBlocking {
			authState.first().let { state ->
				state.update(tokenResponse, exception)
				saveAuthState(state)
				authState.emit(state)
			}
		}
	}

	private fun saveAuthState(authState: AuthState) {
		application.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE).edit {
			putString(PREF_AUTH_STATE, authState.jsonSerializeString())
			putInt(PREF_VERSION, VERSION)
		}
	}

	/**
	 * Carichiamo [AuthState] dalle [SharedPreferences] dove è serializzato JSON.
	 * Dobbiamo prima controllare [PREF_VERSION] e se non corrisponde a [VERSION] restituire null
	 * in quanto lo [AuthState] serializzato non sarebbe utilizzabile dall'app.
	 */
	private fun loadAuthState(): AuthState? =
		application.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE).let { prefs ->
			when (prefs.getInt(PREF_VERSION, 0)) {
				VERSION -> prefs.getString(PREF_AUTH_STATE, null)?.let {
					AuthState.jsonDeserialize(it)
				}
				else -> null
			}
		}

	private fun delete() {
		application.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE).let { prefs ->
			prefs.edit {
				remove(PREF_AUTH_STATE)
			}
		}
	}
}
