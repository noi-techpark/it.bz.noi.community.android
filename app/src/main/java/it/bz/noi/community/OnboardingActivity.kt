package it.bz.noi.community

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.fragment.app.commit
import androidx.lifecycle.asLiveData
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import it.bz.noi.community.databinding.ActivityOnboardingBinding
import it.bz.noi.community.oauth.AuthManager
import it.bz.noi.community.oauth.AuthStateStatus
import it.bz.noi.community.ui.onboarding.AuthorizationErrorFragment
import it.bz.noi.community.ui.onboarding.OnboardingPage1Fragment
import it.bz.noi.community.ui.onboarding.OnboardingPage2Fragment
import it.bz.noi.community.ui.onboarding.OnboardingPage3Fragment
import it.bz.noi.community.utils.Utils.openLinkInExternalBrowser
import kotlinx.coroutines.Dispatchers
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

class OnboardingActivity : AppCompatActivity() {

	private lateinit var binding: ActivityOnboardingBinding
	private lateinit var viewPager: ViewPager2
	private val stateAdapter = OnboardingStateAdapter(this)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		binding = ActivityOnboardingBinding.inflate(layoutInflater)
		setContentView(binding.root)

		AuthManager.status.asLiveData(Dispatchers.Main).observe(this) { status ->
			when (status) {
				is AuthStateStatus.Authorized -> goToMainActivity()
				AuthStateStatus.Unauthorized.NotValidRole -> {
					showWizard(false)
					if (savedInstanceState == null)
						openAuthorizationErrorFragment()
				}
				else -> {
					showWizard(true)
				}
			}
		}

		viewPager = binding.pager
		viewPager.apply {
			adapter = stateAdapter
		}

		TabLayoutMediator(binding.tabLayout, viewPager) { _, _ -> }.attach()

		binding.login.setOnClickListener {
			AuthManager.login(this, AUTH_REQUEST)
		}
		binding.signup.setOnClickListener {
			openLinkInExternalBrowser(SIGNUP_URL)
		}
	}

	private fun showWizard(show: Boolean) {
		binding.login.isVisible = show
		binding.signup.isVisible = show
		binding.pager.isVisible = show
		binding.tabLayout.isVisible = show
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

	private fun goToMainActivity() {
		startActivity(Intent(this, MainActivity::class.java))
	}

	private fun openAuthorizationErrorFragment() {
		supportFragmentManager.commit {
			setReorderingAllowed(true)
			replace(R.id.fragment_container_view, AuthorizationErrorFragment())
			addToBackStack(AUTH_ERROR_TRANSACTION_NAME)
		}
	}

	private fun closeAuthorizationErrorFragment() {
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

		// FIXME SPOSTARE
		private const val SIGNUP_URL = "https://auth.opendatahub.testingmachine.eu/auth/realms/noi/protocol/openid-connect/registrations?client_id=it.bz.noi.community&redirect_uri=https://noi.bz.it&response_type=code&scope=openid"
	}

}

private class OnboardingStateAdapter(activity: OnboardingActivity) :
	FragmentStateAdapter(activity) {
	override fun getItemCount(): Int = 3

	override fun createFragment(position: Int): Fragment {
		return when (position) {
			0 -> OnboardingPage1Fragment()
			1 -> OnboardingPage2Fragment()
			2 -> OnboardingPage3Fragment()
			else -> throw IllegalArgumentException("Onboarding Fragment position not valid: $position")
		}
	}

}
