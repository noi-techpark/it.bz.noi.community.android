// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import it.bz.noi.community.oauth.AuthManager

class NoiApplication : Application() {
	override fun onCreate() {
		currentApplication = this
		super.onCreate()
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
		AuthManager.setup(this)
	}

	companion object {
		internal lateinit var currentApplication: NoiApplication
		const val SHARED_PREFS_NAME = "noi_shared_prefs"
	}
}
