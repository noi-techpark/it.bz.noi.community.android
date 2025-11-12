// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.onboarding

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.crashlytics.FirebaseCrashlytics
import it.bz.noi.community.R
import it.bz.noi.community.databinding.FragmentAuthorizationErrorBinding
import it.bz.noi.community.oauth.AuthManager
import it.bz.noi.community.oauth.AuthStateStatus
import it.bz.noi.community.ui.common.handleEdgeToEdgeAsFooter
import it.bz.noi.community.utils.addClickableSpan
import it.bz.noi.community.utils.addLinkSpan
import it.bz.noi.community.utils.getAppVersion
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AuthorizationErrorFragment : BaseOnboardingFragment() {

	private var _binding: FragmentAuthorizationErrorBinding? = null
	private val binding get() = _binding!!

	private val args by navArgs<AuthorizationErrorFragmentArgs>()

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

		binding.footer.handleEdgeToEdgeAsFooter()

		binding.appVersion.tvAppVersion.text = getAppVersion()

		binding.message.apply {
			movementMethod = LinkMovementMethod.getInstance()
			text = buildSpannedString {
				append(
					getString(R.string.outsider_user_body_format, args.emailAddress)
					.replace("() ", "") // Remove blank email address.
				)
				val jobsSnippet = getString(R.string.outsider_user_body_link_1_part)
				addClickableSpan(jobsSnippet) {
					try {
						startActivity(Intent(Intent.ACTION_VIEW).apply {
							data = Uri.parse(getString(R.string.url_jobs_noi_techpark))
						})
					} catch (ex: Exception) {
						FirebaseCrashlytics.getInstance().recordException(ex)
					}
				}
				addLinkSpan(jobsSnippet, ContextCompat.getColor(requireContext(), R.color.secondary_color))

				val emailSnippet = getString(R.string.community_email_address_link)
				addClickableSpan(emailSnippet) {
					try {
						startActivity(Intent(Intent.ACTION_SENDTO).apply {
							data = Uri.parse(getString(R.string.community_email_address))
						})
					} catch (ex: Exception) {
						FirebaseCrashlytics.getInstance().recordException(ex)
					}
				}
				addLinkSpan(emailSnippet, ContextCompat.getColor(requireContext(), R.color.secondary_color))
			}
		}

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
