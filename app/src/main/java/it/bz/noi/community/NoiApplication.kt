package it.bz.noi.community

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class NoiApplication : Application() {
	override fun onCreate() {
		super.onCreate()
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
	}
}
