package it.bz.noi.community.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import it.bz.noi.community.R

class WebViewFragment : Fragment() {

    // ARGS: title, url
    private val args: WebViewFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_webview, container, false)

        val webView = root.findViewById<WebView>(R.id.webView)
        webView.settings.javaScriptEnabled = true
        args.url?.let {
            webView.loadUrl(it)
        }

        return root
    }

    companion object {
        const val TITLE = "title"
    }
}