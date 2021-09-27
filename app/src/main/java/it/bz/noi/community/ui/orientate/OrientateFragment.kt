package it.bz.noi.community.ui.orientate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import it.bz.noi.community.R
import it.bz.noi.community.ui.WebViewFragmentDirections

class OrientateFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_orientate, container, false)

        val webView = root.findViewById<WebView>(R.id.orientateWV)
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(resources.getString(R.string.url_map))

        val btn = root.findViewById<Button>(R.id.roomBookingBtn)
        btn.setOnClickListener {
            val action = WebViewFragmentDirections.actionGlobalWebViewFragment().apply {
				title = resources.getString(R.string.room_booking)
				url = resources.getString(R.string.url_room_booking)
			}
            findNavController().navigate(action)
        }

        return root
    }

}
