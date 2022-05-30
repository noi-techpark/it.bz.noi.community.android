package it.bz.noi.community.ui.newsDetails

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.ChangeClipBounds
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import it.bz.noi.community.R
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.News
import it.bz.noi.community.data.models.NewsImage
import it.bz.noi.community.data.models.getContactInfo
import it.bz.noi.community.data.models.getDetail
import it.bz.noi.community.databinding.FragmentNewsDetailsBinding
import it.bz.noi.community.databinding.VhVerticalImageNewsBinding
import it.bz.noi.community.utils.Status
import kotlinx.coroutines.Dispatchers
import java.text.DateFormat


class NewsDetailsFragment: Fragment() {

	private var _binding: FragmentNewsDetailsBinding? = null
	private val binding get() = _binding!!

	private val viewModel: NewsDetailViewModel by viewModels(factoryProducer = {
		NewsDetailViewModelFactory(apiHelper = ApiHelper(RetrofitBuilder.apiService), this@NewsDetailsFragment)
	})

	private val df = DateFormat.getDateInstance(DateFormat.SHORT)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		sharedElementEnterTransition = ChangeClipBounds().apply {
			duration = 375
		}
		sharedElementReturnTransition = null

		if (savedInstanceState == null)
			postponeEnterTransition()
	}

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

		viewModel.newsFlow.asLiveData(Dispatchers.Main).observe(viewLifecycleOwner) {
			when(it.status) {
				Status.SUCCESS -> {
					binding.newsLoader.isVisible = false
					val news = it.data!!
					loadNewsData(news)
				}
				Status.ERROR -> {
					binding.newsLoader.isVisible = false
					Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
				}
				Status.LOADING -> {
					binding.newsLoader.isVisible = true
				}
			}
		}

	}

	private fun loadNewsData(news: News) {
		setTransitionNames(news.id)

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
				.listener(object : RequestListener<Drawable> {
					override fun onLoadFailed(
						e: GlideException?,
						model: Any?,
						target: Target<Drawable>?,
						isFirstResource: Boolean
					): Boolean {
						startPostponedEnterTransition()
						return false
					}

					override fun onResourceReady(
						resource: Drawable?,
						model: Any?,
						target: Target<Drawable>?,
						dataSource: DataSource?,
						isFirstResource: Boolean
					): Boolean {
						startPostponedEnterTransition()
						return false
					}
				})

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

	private fun setTransitionNames(newsId: String) {
		binding.header.transitionName = "header_${newsId}"
		binding.logo.transitionName = "logo_${newsId}"
		binding.publisher.transitionName = "publisher_${newsId}"
		binding.date.transitionName = "date_${newsId}"
		binding.title.transitionName = "title_${newsId}"
		binding.shortText.transitionName = "shortText_${newsId}"
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


