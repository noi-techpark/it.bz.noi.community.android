package it.bz.noi.community.ui.onboarding

import android.os.Bundle
import android.view.View
import it.bz.noi.community.R

class OnboardingPage1Fragment : BaseOnboardingFragment() {

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.apply {
			title.text = getString(R.string.onboarding_news_title)
			description.text = getString(R.string.onboarding_news_text)
			//image.setImageResource()
		}
	}

}
