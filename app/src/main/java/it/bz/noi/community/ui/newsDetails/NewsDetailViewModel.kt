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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

class NewsDetailViewModel(private val mainRepository: MainRepository, savedStateHandle: SavedStateHandle) : ViewModel() {

	companion object {
		const val NEWS_ID_ARG = "newsId"
	}

	private val _newsId = MutableStateFlow<String>(
        savedStateHandle.get(NEWS_ID_ARG)
            ?: throw IllegalStateException("Missing required argument $NEWS_ID_ARG")
    )
	val news: Flow<Resource<News>> = _newsId.flatMapLatest { newsId ->
		loadNewsById(newsId)
	}

	private fun loadNewsById(newsId: String) = flow {
        emit(Resource.loading(null))
        try {
            emit(
                Resource.success(
                    data = mainRepository.getNewsDetails(
                        newsId,
                        Utils.getAppLanguage()
                    )
                )
            )
        } catch (exception: Exception) {
            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
        }
    }
}

class NewsDetailViewModelFactory(
	private val apiHelper: ApiHelper,
	owner: Fragment
) : AbstractSavedStateViewModelFactory(owner, owner.arguments) {

	override fun <T : ViewModel?> create(
		key: String,
		modelClass: Class<T>,
		handle: SavedStateHandle): T {
		if (modelClass.isAssignableFrom(NewsDetailViewModel::class.java)) {
			return NewsDetailViewModel(MainRepository(apiHelper), handle) as T
		}
		throw IllegalArgumentException("Unknown class name")
	}
}
