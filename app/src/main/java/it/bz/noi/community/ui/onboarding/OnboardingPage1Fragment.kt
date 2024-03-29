// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.onboarding

import android.os.Bundle
import android.view.View
import it.bz.noi.community.R

class OnboardingPage1Fragment : BaseOnboardingPageFragment() {

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.apply {
			title.text = getString(R.string.onboarding_news_title)
			description.text = getString(R.string.onboarding_news_text)
			image.setImageResource(R.drawable.welcome_news)
		}
	}

}
