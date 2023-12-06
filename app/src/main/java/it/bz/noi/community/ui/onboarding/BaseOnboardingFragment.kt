// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.onboarding

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels

abstract class BaseOnboardingFragment : Fragment() {

	protected val viewModel: OnboardingViewModel by activityViewModels()

	protected var onboardingActivity: OnboardingActivity? = null
		private set

	override fun onAttach(context: Context) {
		super.onAttach(context)
		onboardingActivity = context as? OnboardingActivity
	}

	override fun onDetach() {
		super.onDetach()
		onboardingActivity = null
	}
}
