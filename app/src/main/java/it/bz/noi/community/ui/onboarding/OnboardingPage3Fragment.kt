package it.bz.noi.community.ui.onboarding

import android.os.Bundle
import android.view.View
import it.bz.noi.community.R

class OnboardingPage3Fragment : BaseOnboardingFragment() {

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.apply {
			title.text = getString(R.string.onboarding_meetup_title)
			description.text = getString(R.string.onboarding_meetup_text)
			image.setImageResource(R.drawable.welcome_meet_v1)
		}
	}

}
