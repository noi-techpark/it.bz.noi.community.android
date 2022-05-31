package it.bz.noi.community

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import it.bz.noi.community.NoiApplication.Companion.SHARED_PREFS_NAME
import it.bz.noi.community.databinding.ActivitySplashBinding
import it.bz.noi.community.oauth.AuthManager
import it.bz.noi.community.oauth.AuthStateStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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

			lifecycleScope.launch {
				repeatOnLifecycle(Lifecycle.State.STARTED) {
					AuthManager.userInfo.collect {
						Log.d(TAG, "Fetch user info")
					}
				}
			}

			AuthManager.status.asLiveData(Dispatchers.Main).observe(this) { status ->
				when (status) {
					is AuthStateStatus.Authorized -> goToMainActivity()
					else -> {  // Error or Unauthorized
						goToOnboardingActivity()
					}
				}
			}
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
					goToOnboardingActivity()
				}
				requestFocus()
				start()
			}
		} catch (ex: Exception) {
			goToOnboardingActivity()
		}

	}

	private fun goToMainActivity() {
		if (isFinishing) return
		startActivity(Intent(this, MainActivity::class.java))
		finish()
	}

	private fun goToOnboardingActivity() {
		if (isFinishing) return
		startActivity(Intent(this, OnboardingActivity::class.java))
		finish()
	}

	companion object {
		private const val TAG = "SplashScreenActivity"
		private const val SKIP_PARAM = "skip_splash_screen"
	}
}
