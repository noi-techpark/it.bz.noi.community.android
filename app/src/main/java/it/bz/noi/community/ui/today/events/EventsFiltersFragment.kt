// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.today.events

import android.os.Bundle
import android.util.Log
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
import it.bz.noi.community.utils.Status
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class EventsFiltersFragment : Fragment() {

	private lateinit var filterAdapter: EventsFiltersAdapter
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
			mainViewModel.updateSelectedEventFilters(filterAdapter.filters.filter { it.checked })
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		filterAdapter = EventsFiltersAdapter(
			eventTypeHeader = resources.getString(R.string.filter_by_type),
			technlogySectorHeader = resources.getString(R.string.filter_by_sector),
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

		mainViewModel.appliedEventFilters.observe(viewLifecycleOwner) {
			filterAdapter.filters = it
			mainViewModel.refreshEvents()
		}

		mainViewModel.mediatorEvents.observe(viewLifecycleOwner) {
			when (it.status) {
				Status.LOADING -> {
					// Continuiamo a mostrare il valore precedente, per evitare side effects grafici introducendo un loader sul pulsante
					Log.d(TAG, "Loading results with new filters selection in progress...")
				}

				Status.SUCCESS -> {
					updateNumberOfResults(it.data?.size ?: 0)
				}

				Status.ERROR -> {
					// Continuiamo a mostrare il valore precedente
					Log.e(TAG, "Error loading results with new filters selection")
				}
			}
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
		mainViewModel.updateSelectedEventFilters(emptyList())
	}

	companion object {
		private const val TAG = "EventsFiltersFragment"
	}
}
