// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import it.bz.noi.community.databinding.ActivitySplashBinding
import it.bz.noi.community.oauth.AuthManager
import it.bz.noi.community.oauth.AuthStateStatus
import it.bz.noi.community.storage.getSkipParam
import it.bz.noi.community.storage.getWelcomeUnderstood
import it.bz.noi.community.storage.setSkipParam
import it.bz.noi.community.ui.onboarding.OnboardingActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileOutputStream

/**
 * Activity used only for displaying the Splash/Launch Screen
 */
class SplashScreenActivity : AppCompatActivity() {

	private lateinit var binding: ActivitySplashBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		/**
		 * The splash screen has to be shown only the very first time the user opens the app
		 * (see https://github.com/noi-techpark/it.bz.noi.community.android/issues/74)
		 */
		if (runBlocking { getSkipParam() }) {

			lifecycleScope.launch {
				repeatOnLifecycle(Lifecycle.State.STARTED) {
					AuthManager.userInfo.collect { info ->
						Log.d(TAG, "Fetched user info $info")
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

		if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
			goToOnboardingActivity()
			return
		}

		binding = ActivitySplashBinding.inflate(layoutInflater)
		setContentView(binding.root)
		try {
			binding.vwSplash.apply {
				setVideoURI(getVideoUri())
				setOnCompletionListener {
					lifecycleScope.launch {
						setSkipParam(true)
					}
					goToOnboardingActivity()
				}
				setOnErrorListener { _, what, extra ->
					Log.e("TAG", "Error playing splash video: what=$what, extra=$extra")
					goToOnboardingActivity()
					true // Indicate that we handled the error
				}
				requestFocus()
				setOnPreparedListener { mediaPlayer ->
					start()
				}
				setOnInfoListener { _, what, extra ->
					Log.d(TAG, "Splash video is ready to play $what, extra=$extra")
					true // Indicate that we handled the info
				}
			}
		} catch (ex: Exception) {
			goToOnboardingActivity()
		}
	}

	private fun getVideoUri(): Uri {
		val file = File(cacheDir, "splash.mp4")
		if (!file.exists()) {
			resources.openRawResource(R.raw.noi_splash_screen_video).use { input ->
				FileOutputStream(file).use { output ->
					input.copyTo(output)
				}
			}
		}
		return Uri.fromFile(file)
	}

	private fun goToMainActivity() {
		if (isFinishing) return
		if (intent.hasExtra("deep_link")) {
			val deepLink = Uri.parse(intent.getStringExtra("deep_link"))
			startActivity(Intent(this, MainActivity::class.java).apply {
				setData(deepLink)
				flags = Intent.FLAG_ACTIVITY_NEW_TASK
				putExtra(MainActivity.EXTRA_SHOW_WELCOME, false)
			})
			finish()
		} else {
			startActivity(Intent(this, MainActivity::class.java).apply {
				putExtra(MainActivity.EXTRA_SHOW_WELCOME, runBlocking { !getWelcomeUnderstood() })
			})
			finish()
		}
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
