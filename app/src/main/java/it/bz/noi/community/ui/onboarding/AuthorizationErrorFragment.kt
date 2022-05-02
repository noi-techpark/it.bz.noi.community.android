package it.bz.noi.community.ui.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import it.bz.noi.community.OnboardingActivity
import it.bz.noi.community.databinding.FragmentAuthorizationErrorBinding
import it.bz.noi.community.oauth.AuthManager

class AuthorizationErrorFragment : Fragment() {

	private var _binding: FragmentAuthorizationErrorBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding = FragmentAuthorizationErrorBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.logout.setOnClickListener {
			AuthManager.logout(requireActivity(), OnboardingActivity.LOGOUT_REQUEST)
		}
	}

	override fun onDestroy() {
		super.onDestroy()
		_binding = null
	}

}
