// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.migrations.SharedPreferencesMigration
import androidx.datastore.migrations.SharedPreferencesView
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import it.bz.noi.community.NoiApplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private const val VERSION = 1
private const val NAME = "settings"

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = NAME, produceMigrations = { context ->
	listOf(InitialMigration)
})

val PRIVACY_ACCEPTED_KEY = booleanPreferencesKey("privacy_accepted")
val WELCOME_UNDERSTOOD_KEY = booleanPreferencesKey("welcome_understood")
val VERSION_KEY = intPreferencesKey("version")

fun Context.privacyAcceptedFlow(): Flow<Boolean> = this.dataStore.data
	.map { preferences ->
		preferences[PRIVACY_ACCEPTED_KEY] ?: false
	}
suspend fun Context.updatePrivacyAccepted(privacyAccepted: Boolean) {
	this.dataStore.edit { preferences ->
		preferences[PRIVACY_ACCEPTED_KEY] = privacyAccepted
	}
}

fun Context.welcomeUnderstoodFlow(): Flow<Boolean> = this.dataStore.data
	.map { preferences ->
		preferences[PRIVACY_ACCEPTED_KEY] ?: false
	}
suspend fun Context.updateWelcomeUnderstood(privacyAccepted: Boolean) {
	this.dataStore.edit { preferences ->
		preferences[WELCOME_UNDERSTOOD_KEY] = privacyAccepted
	}
}

suspend fun Context.getWelcomeUnderstood(): Boolean {
	return this.dataStore.data.map { preferences ->
		preferences[WELCOME_UNDERSTOOD_KEY] ?: false
	}.first()
}

fun Context.getWelcomeUnderstoodSync() = runBlocking {
	getWelcomeUnderstood()
}

/**
 * Initial migration from SharedPreferences to DataStore.
 */
object InitialMigration : DataMigration<Preferences> {
	override suspend fun cleanUp() {
		//Delete any data that is no longer needed
	}

	override suspend fun migrate(currentData: Preferences): Preferences {
		return currentData.toMutablePreferences().apply {
			set(VERSION_KEY, VERSION)
		}
	}

	override suspend fun shouldMigrate(currentData: Preferences): Boolean {
		return (currentData.get(VERSION_KEY) ?: -1) != VERSION
	}
}
