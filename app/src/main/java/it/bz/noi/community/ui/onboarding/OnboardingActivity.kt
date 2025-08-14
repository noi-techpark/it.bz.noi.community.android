// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import it.bz.noi.community.MainActivity
import it.bz.noi.community.R
import it.bz.noi.community.databinding.ActivityOnboardingBinding
import it.bz.noi.community.oauth.AuthManager
import it.bz.noi.community.storage.getWelcomeUnderstood
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

class OnboardingActivity : AppCompatActivity() {

	private val navController: NavController get() = findNavController(R.id.nav_host_fragment)

	private lateinit var binding: ActivityOnboardingBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityOnboardingBinding.inflate(layoutInflater)
		setContentView(binding.root)

		val toolbar = binding.toolbar
		setSupportActionBar(toolbar)

		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		val appBarConfiguration = AppBarConfiguration(
			setOf(
				R.id.login,
				R.id.error,
			)
		)

		setupActionBarWithNavController(navController, appBarConfiguration)

		navController.addOnDestinationChangedListener { controller, destination, arguments ->
			when (destination.id) {
				R.id.login -> {
					supportActionBar?.hide()
				}
				else -> {
					toolbar.setTitleTextAppearance(toolbar.context, R.style.TextAppearance_NOI_Toolbar_TitlePrimary)
					supportActionBar?.show()
				}
			}
		}

		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				AuthManager.userInfo.collect { info ->
					Log.d(TAG, "Fetch user info $info")
				}
			}
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		Log.d(TAG, "onActivityResult")
		when (requestCode) {
			AUTH_REQUEST -> {
				val response: AuthorizationResponse? = data?.let {
					AuthorizationResponse.fromIntent(it)
				}
				val exception: AuthorizationException? = data?.let {
					AuthorizationException.fromIntent(it)
				}
				AuthManager.onAuthorization(response, exception)
			}
			LOGOUT_REQUEST -> {
				val exception: AuthorizationException? = data?.let {
					AuthorizationException.fromIntent(it)
				}
				if (exception != null) {
					// TODO
					Toast.makeText(this, "Logout error", Toast.LENGTH_SHORT).show()
				} else {
					AuthManager.onEndSession()
					navController.popBackStack(R.id.login, false)
				}
			}
			else -> {
				super.onActivityResult(requestCode, resultCode, data)
			}
		}
	}

	internal suspend fun goToMainActivity() {
		val showWelcome = !getWelcomeUnderstood()
		startActivity(Intent(this, MainActivity::class.java).apply {
			putExtra(MainActivity.EXTRA_SHOW_WELCOME, showWelcome)
		})
	}

	companion object {
		private const val TAG = "OnboardingActivity"
		private const val AUTH_ERROR_TRANSACTION_NAME = "addAuthorizationErrorFragment"
		const val AUTH_REQUEST = 111
		const val LOGOUT_REQUEST = 112
	}

}
