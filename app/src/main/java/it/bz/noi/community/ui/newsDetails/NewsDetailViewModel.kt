// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.newsDetails

import androidx.fragment.app.Fragment
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.models.News
import it.bz.noi.community.data.repository.MainRepository
import it.bz.noi.community.utils.Resource
import it.bz.noi.community.utils.Utils
import kotlinx.coroutines.flow.*

class NewsDetailViewModel(
	private val mainRepository: MainRepository,
	savedStateHandle: SavedStateHandle
) : ViewModel() {

	companion object {
		const val NEWS_ID_ARG = "newsId"
		private const val NEWS_ARG = "news"
	}

	private val news = MutableStateFlow<News?>(savedStateHandle[NEWS_ARG])
	private val newsId = MutableStateFlow<String?>(savedStateHandle[NEWS_ID_ARG])

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
