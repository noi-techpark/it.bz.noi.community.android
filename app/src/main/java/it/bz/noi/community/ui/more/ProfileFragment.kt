// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.more

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.bz.noi.community.OnboardingActivity.Companion.LOGOUT_REQUEST
import it.bz.noi.community.R
import it.bz.noi.community.databinding.FragmentProfileBinding
import it.bz.noi.community.oauth.AuthManager
import it.bz.noi.community.utils.Status
import it.bz.noi.community.utils.Utils.writeEmail
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

	private var _binding: FragmentProfileBinding? = null
	private val binding get() = _binding!!

	private var reloadUserInfo = true

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentProfileBinding.inflate(inflater, container, false)
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
								binding.name.text = "${userInfo.firstName}\n${userInfo.lastName}"
								binding.icon.text =
									"${userInfo.firstName[0]}${userInfo.lastName[0]}"
								binding.email.text = userInfo.email
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
			AuthManager.logout(requireActivity(), LOGOUT_REQUEST)
		}

		binding.deleteAccount.setOnClickListener {
			requireContext().writeEmail(
				receiverAddress = DELETE_ACCOUNT_EMAIL,
				subject = getString(R.string.delete_profile_compose_subject),
				text = getString(R.string.delete_profile_compose_body)
			)
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	companion object {
		private const val TAG = "ProfileFragment"
		private const val DELETE_ACCOUNT_EMAIL = "community@noi.bz.it"
	}

}
