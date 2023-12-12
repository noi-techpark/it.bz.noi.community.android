// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.onboarding

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.bz.noi.community.R
import it.bz.noi.community.databinding.FragmentAuthorizationErrorBinding
import it.bz.noi.community.oauth.AuthManager
import it.bz.noi.community.oauth.AuthStateStatus
import it.bz.noi.community.utils.Status
import it.bz.noi.community.utils.getAppVersion
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AuthorizationErrorFragment : BaseOnboardingFragment() {

	private var _binding: FragmentAuthorizationErrorBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentAuthorizationErrorBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onStart() {
		super.onStart()
		(requireActivity() as? AppCompatActivity)?.apply {
			supportActionBar?.apply {
				show()
				title = getString(R.string.warning_title)
			}
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
			// Just intercept and do nothing.
		}

		binding.logout.setOnClickListener {
			AuthManager.logout(requireActivity(), OnboardingActivity.LOGOUT_REQUEST)
		}

		binding.appVersion.tvAppVersion.text = getAppVersion()

		viewLifecycleOwner.lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				viewModel.status.collectLatest {  status ->
					when (status) {
						is AuthStateStatus.Authorized -> onboardingActivity?.goToMainActivity()
						!is AuthStateStatus.Unauthorized.NotValidRole -> {
							findNavController().popBackStack()
						}
						else -> Unit
					}
				}
			}
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		_binding = null
	}

	companion object {
		private const val TAG = "AuthorizationErrorFragment"
	}
}
