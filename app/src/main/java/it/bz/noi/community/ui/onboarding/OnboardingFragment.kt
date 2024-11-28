// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.onboarding

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.buildSpannedString
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.tabs.TabLayoutMediator
import it.bz.noi.community.BuildConfig
import it.bz.noi.community.R
import it.bz.noi.community.databinding.FragmentOnboardingBinding
import it.bz.noi.community.oauth.AuthManager
import it.bz.noi.community.oauth.AuthStateStatus
import it.bz.noi.community.ui.onboarding.OnboardingFragmentDirections
import it.bz.noi.community.utils.Utils.openLinkInExternalBrowser
import it.bz.noi.community.utils.addLinkSpan
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OnboardingFragment : BaseOnboardingFragment() {

	private var stateAdapter: OnboardingStateAdapter? = null

	private var _binding: FragmentOnboardingBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentOnboardingBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		stateAdapter = OnboardingStateAdapter(this@OnboardingFragment)
		binding.pager.apply {
			adapter = stateAdapter
		}

		TabLayoutMediator(binding.tabLayout, binding.pager) { _, _ -> }.attach()

		binding.login.setOnClickListener {
			AuthManager.login(requireActivity(), OnboardingActivity.AUTH_REQUEST)
		}
		binding.signup.setOnClickListener {
			requireActivity().openLinkInExternalBrowser(BuildConfig.SIGNUP_URL)
		}
		binding.checkboxText.apply {
			movementMethod = LinkMovementMethod.getInstance()
			text = buildSpannedString {
				append(getString(R.string.app_privacy_policy_label))
				addLinkSpan(getString(R.string.app_privacy_policy_label_link_part), getString(R.string.url_app_privacy))
			}
		}
		binding.checkbox.setOnCheckedChangeListener { _, checked ->
			viewModel.setPrivacyAccepted(checked)
		}
		viewLifecycleOwner.lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				viewModel.isPrivacyAcceptedFlow.collect { accepted ->
					binding.checkbox.isChecked = accepted
					binding.signup.isEnabled = accepted
					binding.login.isEnabled = accepted
				}
			}
		}
		viewLifecycleOwner.lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				viewModel.status.collectLatest {  status ->
					when (status) {
						is AuthStateStatus.Authorized -> {
							showLoginInterface(false)
							onboardingActivity?.goToMainActivity()
						}
						is AuthStateStatus.Unauthorized.NotValidRole -> {
							showLoginInterface(false)
							findNavController().navigate(OnboardingFragmentDirections.loginToError(status.emailAddress))
						}
						else -> {
							showLoginInterface(true)
						}
					}
				}
			}
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onStart() {
		super.onStart()
		(requireActivity() as? AppCompatActivity)?.apply {
			supportActionBar?.apply {
				hide()
			}
		}
	}

	private fun showLoginInterface(show: Boolean) {
		binding.login.isVisible = show
		binding.signup.isVisible = show
	}
}
