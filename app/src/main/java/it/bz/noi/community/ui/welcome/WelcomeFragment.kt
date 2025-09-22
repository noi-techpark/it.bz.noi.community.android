// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.welcome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import it.bz.noi.community.databinding.FragmentWelcomeBinding
import it.bz.noi.community.storage.setWelcomeUnderstood
import kotlinx.coroutines.launch
import it.bz.noi.community.ui.welcome.WelcomeFragmentDirections
import it.bz.noi.community.utils.getAppVersion

class WelcomeFragment : Fragment() {

	private var _binding: FragmentWelcomeBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentWelcomeBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroy() {
		super.onDestroy()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		binding.appVersion.tvAppVersion.text = getAppVersion()
		binding.understood.setOnClickListener {
			lifecycleScope.launch {
				requireContext().setWelcomeUnderstood(binding.checkbox.isChecked)
				findNavController().navigate(WelcomeFragmentDirections.actionWelcomeToHome())
			}
		}
		ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
			val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
			v.updatePadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
			WindowInsetsCompat.CONSUMED
		}
	}
}
