// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeClipBounds
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import androidx.media3.exoplayer.ExoPlayer
import it.bz.noi.community.R
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.News
import it.bz.noi.community.data.models.getLocalizedContactInfo
import it.bz.noi.community.data.models.getLocalizedDetail
import it.bz.noi.community.data.models.getLocalizedVideos
import it.bz.noi.community.databinding.FragmentNewsDetailsBinding
import it.bz.noi.community.databinding.VhHorizontalImageBinding
import it.bz.noi.community.databinding.VhVideoBinding
import it.bz.noi.community.utils.Status
import kotlinx.coroutines.Dispatchers
import java.text.DateFormat


class NewsDetailsFragment : Fragment() {

	private var _binding: FragmentNewsDetailsBinding? = null
	private val binding get() = _binding!!

	private val videoAdapter = NewsVideosAdapter()
	private val imageAdapter = NewsImagesAdapter()

	private val viewModel: NewsDetailViewModel by viewModels(factoryProducer = {
		NewsDetailViewModelFactory(
			apiHelper = ApiHelper(
				RetrofitBuilder.opendatahubApiService,
				RetrofitBuilder.communityApiService,
				RetrofitBuilder.vimeoApiService
			),
			this@NewsDetailsFragment
		)
	})

	private val dateFormat = DateFormat.getDateInstance(DateFormat.SHORT)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		sharedElementEnterTransition = ChangeClipBounds().apply {
			duration = 375
		}
		sharedElementReturnTransition = null

		if (savedInstanceState == null) {
			postponeEnterTransition()
		}
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
		setTransitionNames()
		viewModel.newsFlow.asLiveData(Dispatchers.Main).observe(viewLifecycleOwner) {
			when (it.status) {
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

		var startPostponedEnterTransitionInvoked = false

		binding.date.text = dateFormat.format(news.date)

		news.getLocalizedDetail()?.let { detail ->
			(requireActivity() as AppCompatActivity).supportActionBar?.title = detail.title

			binding.title.text = detail.title
			binding.shortText.text = detail.abstract
			binding.shortText.isVisible = detail.abstract?.isNotBlank() == true
			binding.longText.text =
				detail.text?.let { Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY) }
			binding.longText.isVisible = detail.text?.isNotBlank() == true
			binding.longText.movementMethod = LinkMovementMethod.getInstance()
		}

		var isExternalLink = false
		var isEmail = false
		val contactInfo = news.getLocalizedContactInfo()
		if (contactInfo == null) {
			binding.publisher.text = "N/D" //TODO: localize this
		} else {
			binding.publisher.text = contactInfo.publisher

			startPostponedEnterTransitionInvoked = true

			Glide
				.with(binding.root.context)
				.load(contactInfo.logo)
				.listener(object : RequestListener<Drawable> {
					override fun onLoadFailed(
						e: GlideException?,
						model: Any?,
						target: Target<Drawable>,
						isFirstResource: Boolean
					): Boolean {
						startPostponedEnterTransition()
						return false
					}

					override fun onResourceReady(
						resource: Drawable,
						model: Any,
						target: Target<Drawable>?,
						dataSource: DataSource,
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

		if (!news.getLocalizedVideos().isNullOrEmpty() || !news.images.isNullOrEmpty()) {
			binding.images.isVisible = true
			binding.images.layoutManager =
				LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)

			if (!news.images.isNullOrEmpty()) {
				imageAdapter.setImageItems(news.images.mapNotNull { image ->
					image.url?.let {
						GalleryItem.Image(
							url = image.url
						)
					}
				})
			}

			if (!news.getLocalizedVideos().isNullOrEmpty()) {
				videoAdapter.setVideoItems(news.getLocalizedVideos()?.map { video ->
					GalleryItem.Video(
						videoUrl = video.url,
						thumbnailUrl = video.thumbnailUrl
					)
				} ?: emptyList())
			}

			binding.images.adapter = ConcatAdapter(videoAdapter, imageAdapter)
		} else {
			binding.images.isVisible = false
		}

		if (!startPostponedEnterTransitionInvoked) {
			startPostponedEnterTransitionInvoked = true
			startPostponedEnterTransition()
		}
	}

	private fun setTransitionNames() {
		binding.header.transitionName = "header"
		binding.logo.transitionName = "logo"
		binding.publisher.transitionName = "publisher"
		binding.date.transitionName = "date"
		binding.title.transitionName = "title"
		binding.shortText.transitionName = "shortText"
	}

	private fun writeEmail(receiverAddress: String) {
		val intent = Intent(Intent.ACTION_SENDTO).apply {
			data = Uri.parse("mailto:") // only email apps should handle this
			putExtra(Intent.EXTRA_EMAIL, Array(1) { receiverAddress })
		}
		if (intent.resolveActivity(requireContext().packageManager) != null) {
			startActivity(intent)
		}
	}

	private fun openExternalLink(url: String) {
		val intent =
			Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER).apply {
				data = Uri.parse(url)
			}
		if (intent.resolveActivity(requireContext().packageManager) != null) {
			startActivity(intent)
		}
	}

}

// Data classes per rappresentare gli elementi della gallery
sealed class GalleryItem {
	data class Video(
		val thumbnailUrl: String?,
		val videoUrl: String
	) : GalleryItem()

	data class Image(
		val url: String
	) : GalleryItem()
}

// Interface per gestire gli eventi della gallery
interface GalleryClickListener {
	fun onVideoClick(video: GalleryItem.Video)
}

// Base ViewHolder per elementi della gallery
abstract class BaseGalleryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
	abstract fun bind(item: GalleryItem)
}

/**
 * Adapter used to populate the image gallery of news detail
 */
class NewsImagesAdapter() :
	RecyclerView.Adapter<NewsImagesAdapter.NewsImageViewHolder>() {

	private val images: MutableList<GalleryItem.Image> = mutableListOf()

	fun setImageItems(items: List<GalleryItem.Image>) {
		images.addAll(items)
	}


	/**
	 * View holder of a single picture
	 */
	inner class NewsImageViewHolder(private val binding: VhHorizontalImageBinding) :
		BaseGalleryViewHolder(binding.root) {

		override fun bind(item: GalleryItem) {
			item as GalleryItem.Image
			Glide.with(binding.root.context)
				.load(item.url)
				.placeholder(R.drawable.news_placeholder)
				.into(binding.restImage)
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsImageViewHolder {
		return NewsImageViewHolder(
			VhHorizontalImageBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
		)
	}

	override fun onBindViewHolder(holder: NewsImageViewHolder, position: Int) {
		holder.bind(images[position])
	}

	override fun getItemCount(): Int {
		return images.size
	}
}

/**
 * Adapter used to populate the video gallery of news detail
 */
class NewsVideosAdapter() :
	RecyclerView.Adapter<NewsVideosAdapter.NewsVideoViewHolder>() {

	private val videos: MutableList<GalleryItem.Video> = mutableListOf()

	fun setVideoItems(items: List<GalleryItem.Video>) {
		videos.addAll(items)
	}

	/**
	 * View holder of a single video
	 */
	inner class NewsVideoViewHolder(private val binding: VhVideoBinding) :
		BaseGalleryViewHolder(binding.root) {

		private var player: ExoPlayer? = null
		private var playWhenReady = true
		private var currentItem = 0
		private var playbackPosition = 0L
		private var isPlayerView = false

		private val playbackStateListener: Player.Listener = playbackStateListener()

		override fun bind(item: GalleryItem) {
			item as GalleryItem.Video
			Glide
				.with(binding.root.context)
				.load(item.thumbnailUrl)
				.placeholder(R.drawable.news_placeholder)
				.into(binding.thumbnailImageView)

			binding.playButton.setOnClickListener {
				startPlayback(item.videoUrl)
			}
		}

		private fun startPlayback(videoUrl: String) {
			isPlayerView = true
			binding.thumbnailContainer.visibility = View.GONE
			binding.videoPlayer.visibility = View.VISIBLE
			initializePlayer(videoUrl)
		}

		private fun initializePlayer(videoUrl: String) {
			player = ExoPlayer.Builder(binding.root.context)
				.build()
				.also { exoPlayer ->
					binding.videoPlayer.player = exoPlayer
					val mediaItem = MediaItem.fromUri(videoUrl)
					exoPlayer.setMediaItem(mediaItem)
					exoPlayer.playWhenReady = playWhenReady
					exoPlayer.seekTo(currentItem, playbackPosition)
					exoPlayer.addListener(playbackStateListener)
					exoPlayer.prepare()
				}
		}

		private fun playbackStateListener() = object : Player.Listener {
			override fun onPlaybackStateChanged(playbackState: Int) {
				when (playbackState) {
					ExoPlayer.STATE_BUFFERING -> {
						updateLoadingIndicator(true)
					}

					ExoPlayer.STATE_READY -> {
						updateLoadingIndicator(false)
					}

					ExoPlayer.STATE_ENDED -> {
						// Return to thumbnail view when video ends
						isPlayerView = false
						binding.thumbnailContainer.visibility = View.VISIBLE
						binding.videoPlayer.visibility = View.GONE
					}

					ExoPlayer.STATE_IDLE -> {
						updateLoadingIndicator(false)
					}
				}
			}

			override fun onPlayerError(error: PlaybackException) {
				Toast.makeText(
					binding.root.context,
					"An error occurred: ${error.message}",
					Toast.LENGTH_LONG
				).show()
				// Return to thumbnail view on error
				isPlayerView = false
				binding.thumbnailContainer.visibility = View.VISIBLE
				binding.videoPlayer.visibility = View.GONE
			}
		}

		private fun updateLoadingIndicator(show: Boolean) {
			binding.loadingIndicator.visibility = if (show) View.VISIBLE else View.GONE
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsVideoViewHolder {
		return NewsVideoViewHolder(
			VhVideoBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			)
		)
	}

	override fun onBindViewHolder(holder: NewsVideoViewHolder, position: Int) {
		holder.bind(videos[position])
	}

	override fun getItemCount(): Int {
		return videos.size
	}
}


