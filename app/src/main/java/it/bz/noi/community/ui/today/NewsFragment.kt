package it.bz.noi.community.ui.today

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
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.News
import it.bz.noi.community.data.models.getAbstract
import it.bz.noi.community.data.models.getPublisher
import it.bz.noi.community.data.models.getTitle
import it.bz.noi.community.data.repository.JsonFilterRepository
import it.bz.noi.community.databinding.FragmentNewsBinding
import it.bz.noi.community.databinding.ViewHolderNewsBinding
import it.bz.noi.community.ui.MainViewModel
import it.bz.noi.community.ui.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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

	fun bind(news: News) {
		binding.title.text = news.getTitle()
		binding.shortText.text = news.getAbstract()
		//binding.logo.setImageDrawable() // TODO
		binding.publisher.text = news.getPublisher()
		//binding.date.text = news.date.toString() // FIXME
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
