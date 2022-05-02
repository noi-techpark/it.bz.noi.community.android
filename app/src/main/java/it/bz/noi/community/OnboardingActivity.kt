package it.bz.noi.community

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.asLiveData
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import it.bz.noi.community.databinding.ActivityOnboardingBinding
import it.bz.noi.community.oauth.AuthManager
import it.bz.noi.community.oauth.AuthStateStatus
import it.bz.noi.community.ui.onboarding.OnboardingPage1Fragment
import it.bz.noi.community.ui.onboarding.OnboardingPage2Fragment
import it.bz.noi.community.ui.onboarding.OnboardingPage3Fragment
import kotlinx.coroutines.Dispatchers
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

class OnboardingActivity : AppCompatActivity() {

	private lateinit var binding: ActivityOnboardingBinding
	private lateinit var viewPager: ViewPager2
	private val stateAdapter = OnboardingStateAdapter(this)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		AuthManager.status.asLiveData(Dispatchers.Main).observe(this) { status ->
			when (status) {
				is AuthStateStatus.Authorized -> goToMainActivity()
//				is AuthStateStatus.Error -> TODO()
//				AuthStateStatus.Unauthorized.NotValidRole -> TODO()
//				AuthStateStatus.Unauthorized.PendingToken -> TODO()
//				AuthStateStatus.Unauthorized.UserAuthRequired -> TODO()
			}
		}

		binding = ActivityOnboardingBinding.inflate(layoutInflater)
		setContentView(binding.root)

		viewPager = binding.pager
		viewPager.apply {
			adapter = stateAdapter
		}

		TabLayoutMediator(binding.tabLayout, viewPager) { _, _ -> }.attach()

		binding.login.setOnClickListener {
			AuthManager.login(this, AUTH_REQUEST)
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
			else -> {
				super.onActivityResult(requestCode, resultCode, data)
			}
		}
	}

	private fun goToMainActivity() {
		startActivity(Intent(this, MainActivity::class.java))
	}

	companion object {
		private const val TAG = "OnboardingActivity"
		const val AUTH_REQUEST = 111
		const val REQUEST_LOGOUT = 112
	}

}

private class OnboardingStateAdapter(activity: OnboardingActivity) : FragmentStateAdapter(activity) {
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
