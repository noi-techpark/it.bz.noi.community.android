package it.bz.noi.community

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import it.bz.noi.community.databinding.ActivitySplashBinding

/**
 * Activity used only for displaying the Splash/Launch Screen
 */
class SplashScreenActivity : AppCompatActivity() {

	private lateinit var binding: ActivitySplashBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivitySplashBinding.inflate(layoutInflater)
		setContentView(binding.root)
		try {

			val video =
				Uri.parse("android.resource://" + packageName + "/" + R.raw.noi_splash_screen_video)
			binding.vwSplash.apply {
				setVideoURI(video)
				setOnCompletionListener {
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
}
