// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import it.bz.noi.community.NoiApplication
import it.bz.noi.community.data.models.FilterValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import net.openid.appauth.AuthState

private const val VERSION = 1
private const val NAME = "settings"

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = NAME, produceMigrations = { context ->
	listOf(getSharedPreferencesMigration())
})

@Deprecated("Value is now handled locally")
val PRIVACY_ACCEPTED_KEY = booleanPreferencesKey("privacy_accepted")
val WELCOME_UNDERSTOOD_KEY = booleanPreferencesKey("welcome_understood")
val VERSION_KEY = intPreferencesKey("version")
private const val OLD_SKIP_PARAM_KEY = "skip_splash_screen"
val SKIP_PARAM = booleanPreferencesKey("skip_splash_screen")
private const val OLD_AUTH_STATE_KEY = "authState"
val AUTH_STATE_KEY = stringPreferencesKey("auth_state")
private const val OLD_ACCESS_GRANTED_KEY = "accessGrantedState"
val ACCESS_GRANTED_KEY = booleanPreferencesKey("access_granted_state")
val SELECTED_NEWS_FILTERS = stringSetPreferencesKey("selected__news_filters")


//region Welcome understood

suspend fun Context.setWelcomeUnderstood(privacyAccepted: Boolean) {
	this.dataStore.edit { preferences ->
		preferences[WELCOME_UNDERSTOOD_KEY] = privacyAccepted
	}
}

suspend fun Context.getWelcomeUnderstood(): Boolean {
	return this.dataStore.data.map { preferences ->
		preferences[WELCOME_UNDERSTOOD_KEY] ?: false
	}.first()
}

//endregion

//region Skip param

suspend fun Context.getSkipParam(): Boolean {
	return this.dataStore.data.map { preferences ->
		preferences[SKIP_PARAM] ?: false
	}.first()
}

suspend fun Context.setSkipParam(skipParam: Boolean) {
	this.dataStore.edit { preferences ->
		preferences[SKIP_PARAM] = skipParam
	}
}

//endregion

//region Auth state

suspend fun Context.getAuthState(): AuthState? {
	return this.dataStore.data.map { preferences ->
		preferences[AUTH_STATE_KEY]
	}.first()?.let {
		AuthState.jsonDeserialize(it)
	}
}

suspend fun Context.removeAuthState() {
	this.dataStore.edit { preferences ->
		preferences.remove(AUTH_STATE_KEY)
	}
}

suspend fun Context.setAuthState(state: AuthState) {
	this.dataStore.edit { preferences ->
		preferences[AUTH_STATE_KEY] = state.jsonSerializeString()
	}
}

// endregion

// region Access granted

suspend fun Context.getAccessGranted(default: Boolean): Boolean {
	return this.dataStore.data.map { preferences ->
		preferences[ACCESS_GRANTED_KEY] ?: default
	}.first()
}

suspend fun Context.setAccessGranted(accessGranted: Boolean) {
	this.dataStore.edit { preferences ->
		preferences[ACCESS_GRANTED_KEY] = accessGranted
	}
}

suspend fun Context.removeAccessGranted() {
	this.dataStore.edit { preferences ->
		preferences.remove(ACCESS_GRANTED_KEY)
	}
}

// endregion

// region selected news filter

suspend fun Context.getSelectedNewsFilters(): Set<String> {
	return this.dataStore.data.map { preferences ->
		preferences[SELECTED_NEWS_FILTERS] ?: emptySet()
	}.first()
}

suspend fun Context.setSelectedNewsFilters(selectedFilters: List<FilterValue>) {
	val selectedFilterKeys = selectedFilters.map { it.key }.toSet()
	this.dataStore.edit { preferences ->
		preferences[SELECTED_NEWS_FILTERS] = selectedFilterKeys
	}
}

//endregion

/**
 * This migration is needed to migrate from the old SharedPreferences to the new DataStore.
 */
fun getSharedPreferencesMigration() = SharedPreferencesMigration(
	produceSharedPreferences = {
		NoiApplication.currentApplication.getSharedPreferences(NAME, Context.MODE_PRIVATE)
	},
	migrate = { sharedPrefs: SharedPreferencesView, prefs: Preferences ->
		prefs.toMutablePreferences().apply {

			sharedPrefs.getString(OLD_AUTH_STATE_KEY)?.let { value ->
				set(AUTH_STATE_KEY, value)
			}

			if (sharedPrefs.contains(OLD_ACCESS_GRANTED_KEY)) {
				val oldValue = sharedPrefs.getBoolean(OLD_ACCESS_GRANTED_KEY, false)
				set(ACCESS_GRANTED_KEY, oldValue)
			}

			if (sharedPrefs.contains(OLD_SKIP_PARAM_KEY)) {
				val oldValue = sharedPrefs.getBoolean(OLD_SKIP_PARAM_KEY, false)
				set(SKIP_PARAM, oldValue)
			}

			set(VERSION_KEY, VERSION)
		}.toPreferences()
	}
)
