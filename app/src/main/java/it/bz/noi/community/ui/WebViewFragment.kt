package it.bz.noi.community.ui

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import it.bz.noi.community.R
import it.bz.noi.community.databinding.FragmentWebviewBinding

/**
 *
 */
class WebViewFragment : Fragment() {

    // ARGS: title, url
    private val args: WebViewFragmentArgs by navArgs()
    private val isLoading = MutableLiveData(true)
    private lateinit var binding: FragmentWebviewBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as AppCompatActivity).supportActionBar?.title = args.title
        binding = FragmentWebviewBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val webView = view.findViewById<WebView>(R.id.webView)

        webView.apply {
            settings.javaScriptEnabled = true
            webViewClient = MyWebClient()
            loadUrl(args.url)
        }

        isLoading.observe(viewLifecycleOwner, Observer { isLoading ->
            binding.progressBarLoading.isVisible = isLoading
        })
    }

    companion object {
        const val TITLE = "title"
    }

    inner class MyWebClient : WebViewClient() {

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            isLoading.value = true
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            isLoading.value = false
            super.onPageFinished(view, url)
        }
    }
}
