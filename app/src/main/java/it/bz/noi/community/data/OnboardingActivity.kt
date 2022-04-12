package it.bz.noi.community.data

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import it.bz.noi.community.databinding.ActivityOnboardingBinding

class OnboardingActivity : AppCompatActivity() {

	private lateinit var binding: ActivityOnboardingBinding
	private lateinit var viewPager: ViewPager2

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = ActivityOnboardingBinding.inflate(layoutInflater)
		setContentView(binding.root)

		viewPager = binding.pager.apply {
			// viewPager.adapter = TODO

		}

		binding.dotIndicator.apply {
			numberOfItems = 3
			setSelectedItem(0, false)
		}
	}


}
