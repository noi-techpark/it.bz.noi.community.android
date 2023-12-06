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
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import it.bz.noi.community.MainActivity
import it.bz.noi.community.R
import it.bz.noi.community.databinding.ActivityOnboardingBinding
import it.bz.noi.community.oauth.AuthManager
import it.bz.noi.community.oauth.AuthStateStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

class OnboardingActivity : AppCompatActivity() {

	private val viewModel: OnboardingViewModel by viewModels()

	private lateinit var binding: ActivityOnboardingBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityOnboardingBinding.inflate(layoutInflater)
		setContentView(binding.root)

		setSupportActionBar(binding.toolbar)

		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				AuthManager.userInfo.collect { info ->
					Log.d(TAG, "Fetch user info $info")
				}
			}
		}

		if (savedInstanceState == null) {
			supportFragmentManager.commit {
				replace(R.id.fragment_container_view, OnboardingFragment())
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
					closeAuthorizationErrorFragment()
				}
			}
			else -> {
				super.onActivityResult(requestCode, resultCode, data)
			}
		}
	}

	internal fun goToMainActivity() {
		startActivity(Intent(this, MainActivity::class.java).apply {
			putExtra(MainActivity.EXTRA_SHOW_WELCOME, true)
		})
	}

	internal fun openAuthorizationErrorFragment() {
		supportFragmentManager.commit {
			setReorderingAllowed(true)
			replace(R.id.fragment_container_view, AuthorizationErrorFragment())
			addToBackStack(AUTH_ERROR_TRANSACTION_NAME)
		}
	}

	internal fun closeAuthorizationErrorFragment() {
		supportFragmentManager.popBackStack(
			AUTH_ERROR_TRANSACTION_NAME,
			POP_BACK_STACK_INCLUSIVE
		)
	}

	companion object {
		private const val TAG = "OnboardingActivity"
		private const val AUTH_ERROR_TRANSACTION_NAME = "addAuthorizationErrorFragment"
		const val AUTH_REQUEST = 111
		const val LOGOUT_REQUEST = 112
	}

}
