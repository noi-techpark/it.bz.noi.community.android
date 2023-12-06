// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// At the top level of your kotlin file:
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

val EXAMPLE_COUNTER = booleanPreferencesKey("privacy_accepted")

fun Context.privacyAcceptedFlow(): Flow<Boolean> = this.dataStore.data
	.map { preferences ->
		preferences[EXAMPLE_COUNTER] ?: false
	}
suspend fun Context.updatePrivacyAccepted(privacyAccepted: Boolean) {
	this.dataStore.edit { preferences ->
		preferences[EXAMPLE_COUNTER] = privacyAccepted
	}
}
