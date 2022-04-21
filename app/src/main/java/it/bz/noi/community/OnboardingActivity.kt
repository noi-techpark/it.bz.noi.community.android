package it.bz.noi.community

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import it.bz.noi.community.databinding.ActivityOnboardingBinding
import it.bz.noi.community.oauth.AuthManager
import it.bz.noi.community.ui.onboarding.OnboardingPage1Fragment
import it.bz.noi.community.ui.onboarding.OnboardingPage2Fragment
import it.bz.noi.community.ui.onboarding.OnboardingPage3Fragment

class OnboardingActivity : AppCompatActivity() {

	private lateinit var binding: ActivityOnboardingBinding
	private lateinit var viewPager: ViewPager2
	private val stateAdapter = OnboardingStateAdapter(this)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
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

	companion object {
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
