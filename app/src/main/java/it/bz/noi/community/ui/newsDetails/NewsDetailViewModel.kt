// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.newsDetails

import android.content.res.Resources
import android.util.Log
import android.util.TypedValue
import androidx.fragment.app.Fragment
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.News
import it.bz.noi.community.data.models.extractNewsVideoId
import it.bz.noi.community.data.models.getLocalizedVideos
import it.bz.noi.community.data.repository.MainRepository
import it.bz.noi.community.utils.Resource
import it.bz.noi.community.utils.Status
import it.bz.noi.community.utils.Utils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewsDetailViewModel(
	private val mainRepository: MainRepository,
	savedStateHandle: SavedStateHandle
) : ViewModel() {

	companion object {
		const val NEWS_ID_ARG = "newsId"
		private const val NEWS_ARG = "news"
		private const val TAG = "NewsDetailViewModel"
	}

	private val news = MutableStateFlow<News?>(savedStateHandle[NEWS_ARG])
	private val newsId = MutableStateFlow<String?>(savedStateHandle[NEWS_ID_ARG])

	private val _videoThumbnails = MutableStateFlow<Map<String, String>>(emptyMap())
	val videoThumbnails: StateFlow<Map<String, String>> = _videoThumbnails.asStateFlow()

	// Calcoliamo i pixel dalle dimensioni in dp
	private val metrics = Resources.getSystem().displayMetrics
	private val thumbnailWidthPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 315f, metrics).toInt()
	private val thumbnailHeightPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 210f, metrics).toInt()

	val newsFlow: Flow<Resource<News>> = news.combine(newsId) { _news, _newsId ->
		Resource.loading(null)
		when {
			_news != null -> {
				Resource.success(
					data = _news
				)
			}
			_newsId != null -> {
				try {
					Resource.success(
						data = mainRepository.getNewsDetails(
							_newsId,
							Utils.getAppLanguage()
						)
					)
				} catch (exception: Exception) {
					Resource.error(data = null, message = exception.message ?: "Error Occurred!")
				}
			}
			else -> {
				Resource.error(data = null, message = "Missing arguments $NEWS_ID_ARG and $NEWS_ARG")
			}
		}
	}.onEach { newsRes: Resource<News> ->
		if (newsRes.status == Status.SUCCESS) {
			if (!newsRes.data?.videos.isNullOrEmpty()) {
				newsRes.data?.getLocalizedVideos()?.forEach { video ->
					getThumbnailUrl(videoUrl = video.url)
				}
			}
		}
	}

	private fun getThumbnailUrl(videoUrl: String) {
		val videoId = extractNewsVideoId(videoUrl)
		videoId?.let {
			fetchVimeoAPIResponse(videoId)
		}
	}

	private fun fetchVimeoAPIResponse(videoId: String) {
		// url della chiamata per ricavare il json con il link alla thumbnail
		val vimeoURL = RetrofitBuilder.VIMEO_BASE_URL + videoId

		viewModelScope.launch {
			try {
				val response = mainRepository.getVideoThumbnail(vimeoURL)
				if (response.isSuccessful) {
					response.body()?.thumbnailUrl?.let {
						val pattern = Regex("-d_\\d+x\\d+")
						val thumbnailUrl = it.replace(pattern, "-d_${thumbnailWidthPx}x${thumbnailHeightPx}")

						// Aggiorna la mappa delle thumbnail
						_videoThumbnails.update { currentMap ->
							currentMap + (videoId to thumbnailUrl)
						}
					}
				} else {
					Log.e(TAG, "Error in retrieving the thumbnail URL for the video with id $videoId: ${response.message()}")
				}
			} catch (e: Exception) {
				Log.e(TAG, "Exception in retrieving the thumbnail URL for the video with id $videoId", e)
			}
		}
	}

}

class NewsDetailViewModelFactory(
	private val apiHelper: ApiHelper,
	owner: Fragment
) : AbstractSavedStateViewModelFactory(owner, owner.arguments) {

	override fun <T : ViewModel> create(
		key: String,
		modelClass: Class<T>,
		handle: SavedStateHandle
	): T {
		if (modelClass.isAssignableFrom(NewsDetailViewModel::class.java)) {
			return NewsDetailViewModel(MainRepository(apiHelper), handle) as T
		}
		throw IllegalArgumentException("Unknown class name")
	}
}
