package it.bz.noi.community.ui.newsDetails

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import it.bz.noi.community.R
import it.bz.noi.community.data.models.News
import it.bz.noi.community.data.models.NewsImage
import it.bz.noi.community.data.models.getContactInfo
import it.bz.noi.community.data.models.getDetail
import it.bz.noi.community.databinding.FragmentNewsDetailsBinding
import it.bz.noi.community.databinding.VhVerticalImageNewsBinding
import java.text.DateFormat

class NewsDetailsFragment: Fragment() {

	private var _binding: FragmentNewsDetailsBinding? = null
	private val binding get() = _binding!!

	private val viewModel: NewsDetailViewModel by viewModels()

	private val df = DateFormat.getDateInstance(DateFormat.SHORT)

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentNewsDetailsBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		val news = viewModel.news
		binding.date.text = df.format(news.date)

		news.getDetail()?.let { detail ->
			(requireActivity() as AppCompatActivity).supportActionBar?.title = detail.title

			binding.title.text = detail.title
			binding.shortText.text = detail.abstract
			binding.longText.text = Html.fromHtml(detail.text, Html.FROM_HTML_MODE_LEGACY)
			binding.longText.movementMethod = LinkMovementMethod.getInstance()
		}

		var isExternalLink = false
		var isEmail = false
		news.getContactInfo()?.let { contactInfo ->
			binding.publisher.text = contactInfo.publisher

			binding.logo.isVisible = true
			Glide
				.with(binding.root.context)
				.load(contactInfo.logo)
				.centerCrop()
				.into(binding.logo)

			isExternalLink = !contactInfo.externalLink.isNullOrEmpty()
			if (isExternalLink) {
				binding.externalLink.setOnClickListener {
					openExternalLink(contactInfo.externalLink!!)
				}
			}

			isEmail = !contactInfo.email.isNullOrEmpty()
			if (isEmail) {
				binding.askQuestion.setOnClickListener {
					writeEmail(contactInfo.email!!)
				}
			}
		}
		binding.externalLink.isVisible = isExternalLink
		binding.askQuestion.isVisible = isEmail
		binding.footer.isVisible = isExternalLink || isEmail

		if (news.images != null && news.images.isNotEmpty()) {
			binding.images.isVisible = true
			binding.images.layoutManager = LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
			binding.images.adapter = NewsImagesAdapter(news.images)
		} else {
			binding.images.isVisible = false
		}
	}

	private fun writeEmail(receiverAddress: String) {
		val intent = Intent(Intent.ACTION_SENDTO).apply {
			data = Uri.parse("mailto:") // only email apps should handle this
			putExtra(Intent.EXTRA_EMAIL, Array(1) {receiverAddress})
		}
		if (intent.resolveActivity(requireContext().packageManager) != null) {
			startActivity(intent)
		}
	}

	private fun openExternalLink(url: String) {
		val intent = Intent(Intent.ACTION_VIEW).apply {
			data = Uri.parse(url)
		}
		if (intent.resolveActivity(requireContext().packageManager) != null) {
			startActivity(intent)
		}
	}

}

/**
 * Adapter used to populate the image gallery of news detail
 */
class NewsImagesAdapter(private val images: List<NewsImage>) : RecyclerView.Adapter<NewsImagesAdapter.NewsImageViewHolder>() {

	/**
	 * View holder of a single picture
	 */
	inner class NewsImageViewHolder(private val binding: VhVerticalImageNewsBinding) : RecyclerView.ViewHolder(binding.root) {

		fun bind(image: NewsImage) {
			Glide
				.with(binding.root.context)
				.load(image.url)
				.placeholder(R.drawable.news_placeholder)
				.into(binding.restImage)
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsImageViewHolder {
		return NewsImageViewHolder(VhVerticalImageNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
	}

	override fun onBindViewHolder(holder: NewsImageViewHolder, position: Int) {
		holder.bind(images[position])
	}

	override fun getItemCount(): Int {
		return images.size
	}
}

class NewsDetailViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

	companion object {
		private const val NEWS_ARG = "news"
	}

	val news: News =
		savedStateHandle.get(NEWS_ARG)
			?: throw IllegalStateException("Missing required argument $NEWS_ARG")

}

