package it.bz.noi.community.ui.today

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.crashlytics.FirebaseCrashlytics
import it.bz.noi.community.data.models.News
import it.bz.noi.community.data.models.NewsParams
import it.bz.noi.community.data.repository.MainRepository
import it.bz.noi.community.utils.DateUtils
import it.bz.noi.community.utils.Utils
import java.util.*

class NewsPagingSource(private val mainRepository: MainRepository) :
    PagingSource<Int, News>() {

    companion object {
        private const val TAG = "NewsPagingSource"
		const val PAGE_ITEMS = 10
    }

	private val startDate = Date()

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, News> {
        // Start refresh at page 1 if undefined.
        val nextPageNumber = params.key ?: 1

		val newsParams = NewsParams(
			startDate = DateUtils.parameterDateFormatter().format(startDate),
			pageSize = PAGE_ITEMS,
			pageNumber = nextPageNumber,
			language = Utils.getAppLanguage()
		)

		return try {
			val newsResponse = mainRepository.getNews(newsParams)

			LoadResult.Page(
				data = newsResponse.news,
				prevKey = null, // Only paging forward.
				nextKey = if (newsResponse.nextPage != null) newsResponse.currentPage + 1 else null
			)
		} catch (ex: Exception) {
			Log.e(TAG, "Error loading news", ex)
			FirebaseCrashlytics.getInstance().recordException(ex)
			LoadResult.Error(ex)
		}

    }

    override fun getRefreshKey(state: PagingState<Int, News>): Int? {
        // Try to find the page key of the closest page to anchorPosition, from
        // either the prevKey or the nextKey, but you need to handle nullability
        // here:
        //  * prevKey == null -> anchorPage is the first page.
        //  * nextKey == null -> anchorPage is the last page.
        //  * both prevKey and nextKey null -> anchorPage is the initial page, so
        //    just return null.
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
