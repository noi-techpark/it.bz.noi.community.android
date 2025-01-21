// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.newsDetails

import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.News
import it.bz.noi.community.data.models.NewsVideo
import it.bz.noi.community.data.models.getLocalizedVideos
import it.bz.noi.community.data.repository.MainRepository
import it.bz.noi.community.utils.Resource
import it.bz.noi.community.utils.Status
import it.bz.noi.community.utils.Utils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.net.MalformedURLException
import java.net.URL

class NewsDetailViewModel(
	private val mainRepository: MainRepository,
	savedStateHandle: SavedStateHandle
) : ViewModel() {

	companion object {
		const val NEWS_ID_ARG = "newsId"
		private const val NEWS_ARG = "news"
	}

	private val news = savedStateHandle.getStateFlow(NEWS_ARG, null as News?)
	private val newsId = savedStateHandle.getStateFlow(NEWS_ID_ARG, null as String?)

	val newsFlow: Flow<Resource<News>> = news.combine(newsId) { news, newsId ->
		Resource.loading(null)
		when {
			news != null -> {
				var fakeNews = news
				if (news.videos == null) {
					fakeNews = news.copy(videos = mapOf(Pair("it",
						listOf(
							NewsVideo(url = "https://player.vimeo.com/external/1043375608.m3u8?s=b7ef4718f09d0f5f23e2b58e3b38eb1f4c3834c7&logging=false"),
							NewsVideo(url = "https://player.vimeo.com/external/1024278566.m3u8?s=cbcbf4d98e7a731751c7361dd2d037ac1e4aa62e&logging=false")
						)
					)))
				}
				Resource.success(
					data = fakeNews // news // FIXME
				)
			}
			newsId != null -> {
				try {
					Resource.success(
						data = mainRepository.getNewsDetails(
							newsId,
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
		val videoId = extractVideoId(videoUrl)
		videoId?.let {
			fetchVimeoAPIResponse(videoId)
		}
	}

	// funzione per ricavare l'id dall'url del video
	private fun extractVideoId(from: String): String? {
		try {
			val url = URL(from)

			// Estrai il percorso dall'URL (la parte dopo il dominio)
			val path = url.path

			val pathComponents = path.split("/")
			if (pathComponents.isNotEmpty()) {
				// L'ID del video è il componente che segue "external"
				var videoId = pathComponents[pathComponents.size - 1]

				// Rimuovi l'estensione ".m3u8" se presente
				val m3u8Index = videoId.indexOf(".m3u8")
				if (m3u8Index >= 0) {
					videoId = videoId.substring(0, m3u8Index)
				}

				Log.d("extractVideoId", "Video Id: $videoId")
				return videoId
			}
		} catch (e: MalformedURLException) {
			// TODO
		}
		return null
	}

	private fun fetchVimeoAPIResponse(videoId: String) {
		// url della chiamata per ricavare il json con il link alla thumbnail
		val vimeoURL = RetrofitBuilder.VIMEO_BASE_URL + videoId

		viewModelScope.launch {
			try {
				val response = mainRepository.getVideoThumbnail(vimeoURL)
				if (response.isSuccessful) {
					// Gestisci i dati ricevuti
					val data = response.body()
					data?.thumbnailUrl
					// TODO
				} else {
					// Gestisci gli errori
					response.message()
				}
			} catch (e: Exception) {
				// Gestisci le eccezioni di rete
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
