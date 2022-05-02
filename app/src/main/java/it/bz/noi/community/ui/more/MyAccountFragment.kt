package it.bz.noi.community.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import it.bz.noi.community.MainActivity.Companion.END_SESSION_REQUEST_CODE
import it.bz.noi.community.databinding.FragmentMyAccountBinding
import it.bz.noi.community.oauth.AuthManager

class MyAccountFragment : Fragment() {

	private var _binding: FragmentMyAccountBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentMyAccountBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.name.text = "Nome" // TODO
		binding.email.text = "email" // TODO
		binding.logout.setOnClickListener {
			// FIXME
			AuthManager.logout(requireActivity(), END_SESSION_REQUEST_CODE)
			Toast.makeText(requireContext(), "Logout", Toast.LENGTH_SHORT).show()
		}
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

}
