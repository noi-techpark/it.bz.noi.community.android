// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import it.bz.noi.community.data.repository.AccountsManager
import it.bz.noi.community.databinding.ActivityMainBinding
import it.bz.noi.community.notifications.MessagingService
import it.bz.noi.community.oauth.AuthManager
import it.bz.noi.community.oauth.AuthStateStatus
import it.bz.noi.community.ui.WebViewFragment
import it.bz.noi.community.ui.onboarding.OnboardingActivity
import it.bz.noi.community.ui.onboarding.OnboardingActivity.Companion.LOGOUT_REQUEST
import it.bz.noi.community.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException

class MainActivity : AppCompatActivity() {

	private lateinit var binding: ActivityMainBinding

	private val navController: NavController get() = findNavController(R.id.nav_host_fragment)

	private var showWelcome: Boolean = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		if (intent.hasExtra("deep_link")) {
			val deepLink = intent.getStringExtra("deep_link")
			if (deepLink != null) {
				val uri = Uri.parse(deepLink)
				startActivity(Intent(Intent.ACTION_VIEW).apply { data = uri })
				finish()
				return
			}
		}

		window.navigationBarColor = resources.getColor(R.color.background_color, theme)

		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)

		showWelcome = savedInstanceState?.getBoolean(STATE_SHOW_WELCOME) ?: intent.getBooleanExtra(EXTRA_SHOW_WELCOME, false)

		if (showWelcome) {
			showWelcome = false
			val inflater = navController.navInflater
			val graph = inflater.inflate(R.navigation.mobile_navigation)
			graph.setStartDestination(R.id.welcome)
			navController.graph = graph
		}

		val toolbar = binding.toolbar
		setSupportActionBar(toolbar)

		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		val appBarConfiguration = AppBarConfiguration(
			setOf(
				R.id.navigation_today,
				R.id.navigation_orientate,
				R.id.meet,
				R.id.navigation_eat,
				R.id.navigation_more,
				R.id.welcome,
			)
		)
		setupActionBarWithNavController(navController, appBarConfiguration)
		binding.navView.setupWithNavController(navController)

		navController.addOnDestinationChangedListener { _, destination, arguments ->
			when (destination.id) {
				R.id.navigation_more -> {
					supportActionBar?.hide()
					binding.navView.isVisible = true
				}
				R.id.webViewFragment -> {
					supportActionBar?.show()
					binding.navView.isVisible = true
					toolbar.setTitleTextAppearance(toolbar.context, R.style.TextAppearance_NOI_Toolbar_TitleSecondary)
					arguments?.let {
						supportActionBar?.title = arguments.getString(WebViewFragment.TITLE_ARG)
					}
				}
				R.id.eventsFiltersFragment, R.id.meetFiltersFragment -> {
					supportActionBar?.show()
					binding.navView.isVisible = false
					toolbar.setTitleTextAppearance(toolbar.context, R.style.TextAppearance_NOI_Toolbar_TitleSecondary)
				}
				R.id.eventDetailsFragment, R.id.newsDetails, R.id.profile, R.id.contactDetails -> {
					supportActionBar?.show()
					binding.navView.isVisible = true
					toolbar.setTitleTextAppearance(toolbar.context, R.style.TextAppearance_NOI_Toolbar_TitleSecondary)
				}
				R.id.welcome -> {
					supportActionBar?.show()
					binding.navView.isVisible = false
					toolbar.setTitleTextAppearance(toolbar.context, R.style.TextAppearance_NOI_Toolbar_TitlePrimary)
				}
				else -> {
					supportActionBar?.show()
					binding.navView.isVisible = true
					toolbar.setTitleTextAppearance(toolbar.context, R.style.TextAppearance_NOI_Toolbar_TitlePrimary)
				}
			}
		}

		AuthManager.status.asLiveData(Dispatchers.Main).observe(this) { status ->
			when (status) {
				is AuthStateStatus.Authorized -> {
					AccountsManager.reload()
				}
				is AuthStateStatus.Error,
				AuthStateStatus.Unauthorized.UserAuthRequired,
				AuthStateStatus.Unauthorized.NotValidRole -> {
					goToOnboardingActivity()
				}
				else -> {
					// Nothing to do
				}
			}
		}

		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				AccountsManager.availableCompanies.collect {
					Log.d("MainActivity", "availableCompanies: $it")
				}
			}
		}

		MessagingService.createChannelIfNeeded(this)
		if (BuildConfig.DEBUG) {
			MessagingService.registrationToken()
		}
		subscribeToNewsTopic(Utils.getPreferredNoiNewsTopic())

		checkNotificationPermission()
	}

	/**
	 * Very crude solution to quick add notification permission.
	 */
	private fun checkNotificationPermission() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			if (ActivityCompat.checkSelfPermission(
					this,
					android.Manifest.permission.POST_NOTIFICATIONS
				) != PackageManager.PERMISSION_GRANTED
			) {
				ActivityCompat.requestPermissions(
					this,
					arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
					0
				)
			}
		}
	}

	override fun onRequestPermissionsResult(
		requestCode: Int,
		permissions: Array<out String>,
		grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == 0 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			Log.d(TAG, "Notification permission granted")
		}
	}

	private fun subscribeToNewsTopic(preferredNewsTopic: String) {
		// Per gestire eventuale cambio lingua del dispositivo, faccio prima l'unsubscribe dai topics delle altre lingue
		Utils.allNoiNewsTopics
			.filter { it != preferredNewsTopic }
			.forEach { newsTopic ->
				MessagingService.unsubscribeFromTopic(newsTopic)
			}
		MessagingService.subscribeToTopic(preferredNewsTopic)
	}

	override fun onSupportNavigateUp(): Boolean {
		navController.popBackStack()
		return super.onSupportNavigateUp()
	}

	private fun goToOnboardingActivity() {
		startActivity(Intent(this, OnboardingActivity::class.java))
	}

	@Deprecated("Deprecated in Java")
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		Log.d(TAG, "onActivityResult")
		when (requestCode) {
			LOGOUT_REQUEST -> {
				val exception: AuthorizationException? = data?.let {
					AuthorizationException.fromIntent(it)
				}
				if (exception != null) {
					// TODO
					Toast.makeText(this, "Logout error", Toast.LENGTH_SHORT).show()
				} else {
					AuthManager.onEndSession()
				}
			}
			else -> {
				super.onActivityResult(requestCode, resultCode, data)
			}
		}
	}

	override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
		super.onSaveInstanceState(outState, outPersistentState)
		outState.putBoolean(STATE_SHOW_WELCOME, showWelcome)
	}

	companion object {
		internal const val EXTRA_SHOW_WELCOME: String = "show_welcome"
		private const val STATE_SHOW_WELCOME: String = "show_welcome"
		private const val TAG = "MainActivity"
	}
}
