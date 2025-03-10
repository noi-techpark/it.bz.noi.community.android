// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.today.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import it.bz.noi.community.R
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.FilterValue
import it.bz.noi.community.data.repository.JsonFilterRepository
import it.bz.noi.community.databinding.FragmentFiltersBinding
import it.bz.noi.community.ui.MainViewModel
import it.bz.noi.community.ui.UpdateResultsListener
import it.bz.noi.community.ui.ViewModelFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class NewsFiltersFragment : Fragment() {

	private lateinit var filterAdapter: NewsFilterAdapter
	private var _binding: FragmentFiltersBinding? = null
	private val binding get() = _binding!!

	private val mainViewModel: MainViewModel by activityViewModels(factoryProducer = {
		ViewModelFactory(
			ApiHelper(RetrofitBuilder.opendatahubApiService, RetrofitBuilder.communityApiService, RetrofitBuilder.vimeoApiService),
			JsonFilterRepository(requireActivity().application)
		)
	})

	private val updateResultsListener = object : UpdateResultsListener {
		override fun updateResults(filter: FilterValue) {
			mainViewModel.updateSelectedNewsFilters(filterAdapter.filters.filter { it.checked })
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		filterAdapter = NewsFilterAdapter(
			updateResultsListener = updateResultsListener
		)
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentFiltersBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		mainViewModel.appliedNewsFilters.observe(requireActivity()) {
			filterAdapter.filters = it
			mainViewModel.refreshNews()
		}

		binding.apply {
			categoriesGroup.isVisible = false

			filterstRecyclerView.adapter = filterAdapter

			resetBtn.setOnClickListener {
				resetFilters()
			}

			showBtn.setOnClickListener {
				findNavController().popBackStack()
			}
		}

	}

	private fun updateNumberOfResults(numResults: Int?) {
		binding.showBtn.text = getString(R.string.show_results_btn_format, numResults ?: 0)
	}

	private fun resetFilters() {
		mainViewModel.updateSelectedNewsFilters(emptyList())
	}

	companion object {
		private const val TAG = "NewsFiltersFragment"
	}
}
