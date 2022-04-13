package it.bz.noi.community

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import it.bz.noi.community.databinding.ActivityOnboardingBinding
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
			registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
				override fun onPageSelected(position: Int) {
					super.onPageSelected(position)
					binding.dotIndicator.setSelectedItem(position, false)
				}
			})
		}

		binding.dotIndicator.apply {
			numberOfItems = stateAdapter.itemCount
			setSelectedItem(0, false)
		}
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
