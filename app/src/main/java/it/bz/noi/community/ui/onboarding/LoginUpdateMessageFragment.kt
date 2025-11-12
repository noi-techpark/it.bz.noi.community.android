// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import it.bz.noi.community.R
import it.bz.noi.community.databinding.FragmentLoginUpdateMessageBinding
import it.bz.noi.community.oauth.AuthManager
import it.bz.noi.community.ui.common.handleEdgeToEdgeAsFooter

class LoginUpdateMessageFragment : BaseOnboardingFragment() {

	private var _binding: FragmentLoginUpdateMessageBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentLoginUpdateMessageBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onStart() {
		super.onStart()
		(requireActivity() as? AppCompatActivity)?.apply {
			supportActionBar?.apply {
				show()
				title = getString(R.string.login_update_page_title)
			}
		}
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
			// Just intercept and do nothing.
		}

		binding.registerNowBtn.setOnClickListener {
			AuthManager.clearAuthState {
				findNavController().popBackStack()
			}
		}

		binding.footer.handleEdgeToEdgeAsFooter()

	}

	override fun onDestroy() {
		super.onDestroy()
		_binding = null
	}

	companion object {
		private const val TAG = "LoginUpdateInfoFragment"
	}
}
