package it.bz.noi.community.ui.today

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.News
import it.bz.noi.community.data.models.getContactInfo
import it.bz.noi.community.data.models.getDetail
import it.bz.noi.community.data.repository.JsonFilterRepository
import it.bz.noi.community.databinding.FragmentNewsBinding
import it.bz.noi.community.databinding.ViewHolderNewsBinding
import it.bz.noi.community.ui.MainViewModel
import it.bz.noi.community.ui.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor
import java.util.*

class NewsFragment  : Fragment() {

	private var _binding: FragmentNewsBinding? = null
	private val binding get() = _binding!!

	private val viewModel: MainViewModel by activityViewModels(factoryProducer = {
		ViewModelFactory(
			ApiHelper(RetrofitBuilder.apiService),
			JsonFilterRepository(requireActivity().application)
		)
	})

	private val newsAdapter = PagingNewsAdapter(NewsComparator)

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
			adapter = newsAdapter
		}

		viewLifecycleOwner.lifecycleScope.launch {
			viewModel.newsFlow.collectLatest { pagingData ->
				newsAdapter.submitData(pagingData)
			}
		}
	}

}

class NewsVH(private val binding: ViewHolderNewsBinding) : RecyclerView.ViewHolder(binding.root) {

	private val df = DateFormat.getDateInstance(DateFormat.SHORT) // FIXME chiedere tipo di formattazione

	fun bind(news: News) {
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
		binding.date.text = df.format(news.date)
	}

}

class PagingNewsAdapter(diffCallback: DiffUtil.ItemCallback<News>) : PagingDataAdapter<News, NewsVH>(diffCallback) {
	override fun onBindViewHolder(holder: NewsVH, position: Int) {
		getItem(position)?.let {
			holder.bind(it)
		}
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsVH {
		return NewsVH(ViewHolderNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false))
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
