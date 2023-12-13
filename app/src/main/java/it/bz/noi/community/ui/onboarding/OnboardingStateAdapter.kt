// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.onboarding

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

internal class OnboardingStateAdapter(host: Fragment) :
	FragmentStateAdapter(host) {
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
