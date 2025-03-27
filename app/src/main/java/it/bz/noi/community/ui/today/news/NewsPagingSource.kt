// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.today.news

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.crashlytics.FirebaseCrashlytics
import it.bz.noi.community.data.models.FilterValue
import it.bz.noi.community.data.models.News
import it.bz.noi.community.data.models.NewsParams
import it.bz.noi.community.data.repository.MainRepository
import java.util.*

private const val TAG = "NewsPagingSource"

class NewsPagingSource(
	private val pageSize: Int,
	private val selectedFilters: List<FilterValue>,
	private val mainRepository: MainRepository
) :
    PagingSource<Int, News>() {

	private val startDate = Date()

	private var moreHighlight = true

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, News> {

		return try {

			val nextPageNumber = params.key ?: 1 // Start refresh at page 1 if undefined.

			// 1. Obtain the "next" page, optionally highlighted.
			val newsParams = NewsParams(
				nextPageNumber = nextPageNumber,
				pageSize = pageSize,
				from = startDate,
				moreHighlights = moreHighlight,
				selectedFilters = selectedFilters
			)
			val newsResponse = mainRepository.getNews(newsParams)
			val news = newsResponse.news.toMutableList()

			// 2. If there are more pages, load the next page.
			var nextKey = if (newsResponse.nextPage != null) newsResponse.currentPage + 1 else null

			// 3. If there are more highlights but there's no next page, load the next page without
			// highlights.
			if (moreHighlight && nextKey == null) {
				moreHighlight = false

				val notHighlightedNewsParams = NewsParams(
					nextPageNumber = nextPageNumber,
					pageSize = pageSize,
					from = startDate,
					moreHighlights = false,
					selectedFilters = selectedFilters
				)
				val notHighlightedNewsResponse = mainRepository.getNews(notHighlightedNewsParams)
				news += notHighlightedNewsResponse.news
				nextKey = if (notHighlightedNewsResponse.nextPage != null) notHighlightedNewsResponse.currentPage + 1 else null
			}

			LoadResult.Page(
				data = news,
				prevKey = null, // Only paging forward.
				nextKey = nextKey
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
