package it.bz.noi.community

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import it.bz.noi.community.databinding.ActivitySplashBinding

/**
 * Activity used only for displaying the Splash/Launch Screen
 */
class SplashScreenActivity : AppCompatActivity() {

	private lateinit var sharedPreferences: SharedPreferences
	private lateinit var binding: ActivitySplashBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		sharedPreferences = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

		/**
		 * The splash screen has to be shown only the very first time the user opens the app
		 * (see https://github.com/noi-techpark/it.bz.noi.community.android/issues/74)
		 */
		if (sharedPreferences.getBoolean(SKIP_PARAM, false)) {
			goToMainActivity()
			return
		}

		binding = ActivitySplashBinding.inflate(layoutInflater)
		setContentView(binding.root)
		try {

			val video =
				Uri.parse("android.resource://" + packageName + "/" + R.raw.noi_splash_screen_video)
			binding.vwSplash.apply {
				setVideoURI(video)
				setOnCompletionListener {
					sharedPreferences.edit {
						putBoolean(SKIP_PARAM, true)
					}
					goToMainActivity()
				}
				requestFocus()
				start()
			}
		} catch (ex: Exception) {
			goToMainActivity()
		}


	}

	private fun goToMainActivity() {
		if (isFinishing) return
		startActivity(Intent(this, MainActivity::class.java))
		finish()
	}

	companion object {
		private const val SHARED_PREFS_NAME = "noi_shared_prefs"
		private const val SKIP_PARAM = "skip_splash_screen"
	}
}
