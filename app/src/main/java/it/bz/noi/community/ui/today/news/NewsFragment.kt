// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.today.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.News
import it.bz.noi.community.data.models.getContactInfo
import it.bz.noi.community.data.models.getDetail
import it.bz.noi.community.data.models.hasImportantFlag
import it.bz.noi.community.data.repository.JsonFilterRepository
import it.bz.noi.community.databinding.FragmentNewsBinding
import it.bz.noi.community.databinding.VhNewsBinding
import it.bz.noi.community.ui.MainViewModel
import it.bz.noi.community.ui.ViewModelFactory
import it.bz.noi.community.ui.today.TodayFragmentDirections
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.DateFormat

@ExperimentalCoroutinesApi
class NewsFragment : Fragment() {

	private var _binding: FragmentNewsBinding? = null
	private val binding get() = _binding!!

	private val viewModel: MainViewModel by activityViewModels(factoryProducer = {
		ViewModelFactory(
			ApiHelper(RetrofitBuilder.opendatahubApiService, RetrofitBuilder.communityApiService),
			JsonFilterRepository(requireActivity().application)
		)
	})

	private val newsAdapter = PagingNewsAdapter(NewsComparator, object : NewsDetailListener {
		override fun openNewsDetail(
			news: News,
			header: ConstraintLayout,
			logo: ImageView,
			publisher: TextView,
			date: TextView,
			title: TextView,
			shortText: TextView
		) {
			val extras = FragmentNavigatorExtras(
				header to "header_${news.id}",
				logo to "logo_${news.id}",
				publisher to "publisher_${news.id}",
				date to "date_${news.id}",
				title to "title_${news.id}",
				shortText to "shortText_${news.id}"
			)
			findNavController().navigate(
				TodayFragmentDirections.actionNavigationTodayToNewsDetails(
					null,
					news
				), extras
			)
		}
	})

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentNewsBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		binding.news.apply {
			addItemDecoration(NewsItemDecoration())
			adapter = newsAdapter
			doOnPreDraw {
				startPostponedEnterTransition()
			}
		}

		viewLifecycleOwner.lifecycleScope.launch {
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
				viewModel.newsFlow.collectLatest { pagingData ->
					newsAdapter.submitData(pagingData)
				}
			}
		}

		viewLifecycleOwner.lifecycleScope.launch {
			newsAdapter.loadStateFlow.collectLatest { loadStates: CombinedLoadStates ->
				when (loadStates.refresh) {
					is LoadState.Loading -> {
						binding.swipeRefreshNews.isRefreshing = true
					}
					is LoadState.Error -> {
						binding.swipeRefreshNews.isRefreshing = false
						Toast.makeText(
							requireContext(),
							(loadStates.refresh as LoadState.Error).error.message,
							Toast.LENGTH_LONG
						).show()
					}
					is LoadState.NotLoading -> {
						binding.swipeRefreshNews.isRefreshing = false
					}
				}

				when (loadStates.append) {
					is LoadState.Loading -> {
						binding.swipeRefreshNews.isRefreshing = true
					}
					is LoadState.Error -> {
						binding.swipeRefreshNews.isRefreshing = false
						Toast.makeText(
							requireContext(),
							(loadStates.append as LoadState.Error).error.message,
							Toast.LENGTH_LONG
						).show()
					}
					is LoadState.NotLoading -> {
						binding.swipeRefreshNews.isRefreshing = false
					}
				}
			}
		}

		binding.swipeRefreshNews.setOnRefreshListener {
			viewModel.refreshNews()
		}
	}

}

interface NewsDetailListener {
	fun openNewsDetail(
		news: News,
		header: ConstraintLayout,
		logo: ImageView,
		publisher: TextView,
		date: TextView,
		title: TextView,
		shortText: TextView
	)
}

class NewsVH(private val binding: VhNewsBinding, detailListener: NewsDetailListener) :
	RecyclerView.ViewHolder(binding.root) {

	private val df = DateFormat.getDateInstance(DateFormat.SHORT)
	private lateinit var news: News

	init {
		binding.root.setOnClickListener {
			detailListener.openNewsDetail(
				news,
				binding.header,
				binding.logo,
				binding.publisher,
				binding.date,
				binding.title,
				binding.shortText
			)
		}
	}

	fun bind(news: News) {
		this.news = news
		binding.date.text = df.format(news.date)
		binding.importantTag.isVisible = news.hasImportantFlag()
		news.getDetail()?.let { detail ->
			binding.title.text = detail.title
			binding.shortText.text = detail.abstract
		}
		news.getContactInfo()?.let { contactInfo ->
			binding.publisher.text = contactInfo.publisher
			Glide
				.with(binding.root.context)
				.load(contactInfo.logo)
				.centerCrop()
				.into(binding.logo)

		}

		setTransitionNames(news.id)
	}

	private fun setTransitionNames(newsId: String) {
		binding.header.transitionName = "header_${newsId}"
		binding.logo.transitionName = "logo_${newsId}"
		binding.publisher.transitionName = "publisher_${newsId}"
		binding.date.transitionName = "date_${newsId}"
		binding.title.transitionName = "title_${newsId}"
		binding.shortText.transitionName = "shortText_${newsId}"
	}

}

class PagingNewsAdapter(
	diffCallback: DiffUtil.ItemCallback<News>,
	private val detailListener: NewsDetailListener
) : PagingDataAdapter<News, NewsVH>(diffCallback) {

	override fun onBindViewHolder(holder: NewsVH, position: Int) {
		getItem(position)?.let {
			holder.bind(it)
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsVH {
		return NewsVH(
			VhNewsBinding.inflate(
				LayoutInflater.from(parent.context),
				parent,
				false
			), detailListener
		)
	}

}

object NewsComparator : DiffUtil.ItemCallback<News>() {
	override fun areItemsTheSame(oldItem: News, newItem: News): Boolean {
		return oldItem.id == newItem.id
	}

	override fun areContentsTheSame(oldItem: News, newItem: News): Boolean {
		return true
	}
}
