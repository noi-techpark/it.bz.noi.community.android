// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.meet

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.paging.Config
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.R
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.*
import it.bz.noi.community.data.repository.AccountsManager
import it.bz.noi.community.databinding.FragmentFiltersBinding
import it.bz.noi.community.ui.UpdateResultsListener
import it.bz.noi.community.ui.today.events.TimeFilterAdapter
import it.bz.noi.community.ui.today.events.TimeFilterClickListener
import it.bz.noi.community.utils.Status
import it.bz.noi.community.utils.groupedByInitial
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Contextual

@ExperimentalCoroutinesApi
class MeetFiltersFragment : Fragment() {

	private var _binding: FragmentFiltersBinding? = null
	private val binding get() = _binding!!

	private lateinit var adapters: Map<Char, MeetFiltersSectionAdapter>
	private lateinit var adapter: ConcatAdapter

	private val categoriesListener by lazy {
		object : TimeFilterClickListener {
			override fun onTimeFilterClick(position: Int) {
				Log.d(TAG, "Categories filter clicked at position $position")
				viewModel.updateCategoriesFilter(
					CategoryFilter.entries[position].types
				)
			}
		}
	}

	private val categoriesAdapter: TimeFilterAdapter = TimeFilterAdapter(emptyList(), categoriesListener)

	private val viewModel: MeetViewModel by navGraphViewModels(
		R.id.navigation_meet,
		factoryProducer = {
			MeetViewModelFactory(
				apiHelper = ApiHelper(
					RetrofitBuilder.opendatahubApiService,
					RetrofitBuilder.communityApiService
				), this
			)
		})

	private val updateResultsListener = object : UpdateResultsListener {
		override fun updateResults(filter: FilterValue) {
			viewModel.updateSelectedFilters(filter)
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		adapters = (('A'..'Z').associateWith { c -> MeetFiltersSectionAdapter(c, updateResultsListener) }) + ('#' to MeetFiltersSectionAdapter('#', updateResultsListener))
		adapter = ConcatAdapter(
			ConcatAdapter.Config.Builder()
				.setIsolateViewTypes(false)
				.setStableIdMode(ConcatAdapter.Config.StableIdMode.SHARED_STABLE_IDS)
				.build(),
			adapters.entries.sortedBy { it.key }.map { it.value }
		)

		lifecycleScope.launch {
			repeatOnLifecycle(Lifecycle.State.STARTED) {
				viewModel.categoriesFilter.collectLatest { filters ->
					categoriesAdapter.timeFilters = CategoryFilter.entries.map { cat ->

						fun CategoryFilter.toDescription(): String = with(requireContext()) {
							when (this@toDescription) {
								CategoryFilter.ALL -> getString(R.string.filter_by_none)
								CategoryFilter.COMPANY -> getString(R.string.filter_by_company)
								CategoryFilter.STARTUP -> getString(R.string.filter_by_startup)
								CategoryFilter.RESEARCH_INSTITUTION -> getString(R.string.filter_by_research_institution)
							}
						}

						/**
						 * Se la categoria è selezionata in base ai filtri di categoria attivi.
						 */
						fun CategoryFilter.matches(types: List<AccountType>): Boolean = when (this) {
							CategoryFilter.ALL -> types.isEmpty()
							else -> types.containsAll(this.types)
						}

						TimeFilter(
							cat.toDescription(),
							cat.matches(filters),
						)
					}
				}
			}
		}
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentFiltersBinding.inflate(inflater)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
		AccountsManager.updateSearchParam("")
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		viewModel.filtersFlow.asLiveData(Dispatchers.Main).observe(requireActivity()) { filtersByType ->
			filtersByType.values.flatten().groupedByInitial {
				it.desc.first().uppercaseChar()
			}.forEach { (initial, filters) ->
				adapters[initial]?.filters = filters
			}
		}

		viewLifecycleOwner.lifecycleScope.launch {
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
				viewModel.filteredContactsFlow.collectLatest { res ->
					when (res.status) {
						Status.SUCCESS -> {
							updateNumberOfResults(res.data?.size ?: 0)
						}

						Status.ERROR -> {
							// Continuiamo a mostrare il valore precedente
							Log.e(TAG, "Error loading results with new filters selection")
						}

						Status.LOADING -> {
							// Continuiamo a mostrare il valore precedente, per evitare side effects grafici introducendo un loader sul pulsante
							Log.d(TAG, "Loading results with new filters selection in progress...")
						}
					}
				}
			}
		}

		binding.apply {
			filterstRecyclerView.apply {
				adapter = this@MeetFiltersFragment.adapter
				addOnScrollListener(object : RecyclerView.OnScrollListener() {

					// Nasconde la tastiera, quando l'utente inizia a scrollare la lista
					override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
						super.onScrollStateChanged(recyclerView, newState)
						if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
							val imm: InputMethodManager =
								recyclerView.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
							imm.hideSoftInputFromWindow(recyclerView.windowToken, 0)
						}
					}

				})
			}

			resetBtn.setOnClickListener {
				resetFilters()
			}

			showBtn.setOnClickListener {
				findNavController().popBackStack()
			}

			searchFieldEditText.addTextChangedListener { text ->
				AccountsManager.updateSearchParam(text?.toString() ?: "")
			}

			categoriesRecyclerView.apply {
				layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
				adapter = categoriesAdapter
			}
		}
	}

	private fun updateNumberOfResults(numResults: Int?) {
		binding.showBtn.text = getString(R.string.show_results_btn_format, numResults ?: 0)
	}

	private fun resetFilters() {
		viewModel.clearSelctedFilters()
	}

	companion object {
		private const val TAG = "MeetFiltersFragment"
	}

}
