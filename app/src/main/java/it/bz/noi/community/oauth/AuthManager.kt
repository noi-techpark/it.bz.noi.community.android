package it.bz.noi.community.oauth

import android.app.Activity
import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.DialogInterface
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import it.bz.noi.community.SplashScreenActivity.Companion.SHARED_PREFS_NAME
import net.openid.appauth.*
import net.openid.appauth.browser.BrowserAllowList
import net.openid.appauth.browser.VersionedBrowserMatcher


object AuthManager {

	private const val TAG = "AuthManager"

	// *************** TODO SPOSTARE in BUILD CONFIG?
	private const val REDIRECT_URL: String =
		"noi-community://oauth2redirect/login-callback" // FIXME ricontrollare: Scheme must match appAuthRedirectScheme in the manifest
	private const val AUTH_ENDPOINT: String = "https://auth.opendatahub.testingmachine.eu/auth/realms/noi/protocol/openid-connect"
	private const val CLIENT_ID: String = "it.bz.noi.community"
	private const val CLIENT_SECRET: String = ""
	// ***************

	private const val PREF_AUTH_STATE = "authState"




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

	private val authServiceConfig = AuthorizationServiceConfiguration(
		Uri.parse("${AUTH_ENDPOINT}/auth"),  // authorization endpoint
		Uri.parse("${AUTH_ENDPOINT}/token") // token endpoint
	)

	fun login(context: Activity, requestCode: Int) {

		// val authServiceConfig = runBlocking {
		//			authState.first().authorizationServiceConfiguration!!
		//		}

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

	fun onAuthorization(response: AuthorizationResponse?, exception: AuthorizationException?) {
		val state = createAuthState()
		state.update(response, exception)
		val test = state.isAuthorized
		writeAuthState(state)
	}

	private fun createAuthState() = AuthState(authServiceConfig)

	fun readAuthState(): AuthState {
		return application.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE).getString(PREF_AUTH_STATE, null)?.let {
			AuthState.jsonDeserialize(it)
		} ?: createAuthState()
	}

	fun writeAuthState(state: AuthState) {
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
