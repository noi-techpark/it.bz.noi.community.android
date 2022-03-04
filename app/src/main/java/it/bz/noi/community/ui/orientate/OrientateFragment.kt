package it.bz.noi.community.ui.orientate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import it.bz.noi.community.R
import it.bz.noi.community.databinding.FragmentOrientateBinding
import it.bz.noi.community.ui.WebViewFragmentDirections
import it.bz.noi.community.utils.Utils

class OrientateFragment : Fragment() {

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {

		val binding = FragmentOrientateBinding.inflate(inflater, container, false)

		binding.orientateWV.apply {
			settings.javaScriptEnabled = true
			loadUrl(
				Utils.addParamsToUrl(
					resources.getString(R.string.url_map),
					fullview = true,
					hidezoom = true
				)
			)
		}

		binding.bookARoom.setOnClickListener {
			val action = WebViewFragmentDirections.actionGlobalWebViewFragment().apply {
				title = resources.getString(R.string.room_booking)
				url = resources.getString(R.string.url_room_booking)
			}
			findNavController().navigate(action)
		}

		return binding.root
	}

}
