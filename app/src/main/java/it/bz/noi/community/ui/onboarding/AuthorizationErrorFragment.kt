// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.onboarding

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.bz.noi.community.OnboardingActivity
import it.bz.noi.community.R
import it.bz.noi.community.databinding.FragmentAuthorizationErrorBinding
import it.bz.noi.community.oauth.AuthManager
import it.bz.noi.community.utils.Status
import kotlinx.coroutines.launch

class AuthorizationErrorFragment : Fragment() {

	private var _binding: FragmentAuthorizationErrorBinding? = null
	private val binding get() = _binding!!

	private var reloadUserInfo = true

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentAuthorizationErrorBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		viewLifecycleOwner.lifecycleScope.launch {
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
				AuthManager.userInfo.collect {
					it?.let { userInfoRes ->
						when (userInfoRes.status) {
							Status.SUCCESS -> {
								val userInfo = userInfoRes.data!!
								binding.message.text = getString(R.string.access_not_granted_format, userInfo.fullname, userInfo.email)
							}
							Status.ERROR -> {
								if (reloadUserInfo) {
									AuthManager.relaodUserInfo()
								} else {
									MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_NOI_MaterialAlertDialog)
										.setTitle(R.string.error_title)
										.setMessage(R.string.user_info_error_msg)
										.setPositiveButton(R.string.ok_button) { _, _ -> }
										.show()
								}
								reloadUserInfo = !reloadUserInfo
							}
							Status.LOADING -> {
								Log.d(TAG, "Loading user info...")
							}
						}
					}
				}
			}
		}

		binding.logout.setOnClickListener {
			AuthManager.logout(requireActivity(), OnboardingActivity.LOGOUT_REQUEST)
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
