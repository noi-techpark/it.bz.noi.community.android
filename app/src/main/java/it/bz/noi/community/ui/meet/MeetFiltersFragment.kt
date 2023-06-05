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
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.R
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.*
import it.bz.noi.community.data.repository.AccountsManager
import it.bz.noi.community.databinding.FragmentFiltersBinding
import it.bz.noi.community.ui.UpdateResultsListener
import it.bz.noi.community.utils.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
class MeetFiltersFragment : Fragment() {

    private lateinit var filterAdapter: MeetFiltersAdapter

    private var _binding: FragmentFiltersBinding? = null
	private val binding get() = _binding!!

	private val viewModel: MeetViewModel by navGraphViewModels(R.id.navigation_meet, factoryProducer = {
		MeetViewModelFactory(apiHelper = ApiHelper(RetrofitBuilder.opendatahubApiService, RetrofitBuilder.communityApiService), this)
	})

	private val updateResultsListener = object : UpdateResultsListener {
		override fun updateResults() {
			// FIXME
			val selectedFilters: Map<AccountType, List<FilterValue>> = filterAdapter.filters.mapValues { entry ->
				entry.value.filter { it.checked }
			}
			viewModel.updateSelectedFilters(selectedFilters)
		}
	}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

		val headers = mapOf(
			AccountType.COMPANY to getString(R.string.filter_by_company),
			AccountType.STARTUP to getString(R.string.filter_by_startup),
			AccountType.RESEARCH_INSTITUTION to getString(R.string.filter_by_research_institution)
		)
		filterAdapter = MeetFiltersAdapter(
			headers = headers,
			updateResultsListener = updateResultsListener
		)
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

		viewModel.appliedFiltersFlow.asLiveData(Dispatchers.Main).observe(requireActivity()) {
			filterAdapter.filters = it
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
            filterstRV.adapter = filterAdapter
			filterstRV.addOnScrollListener(object : RecyclerView.OnScrollListener() {

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

            resetBtn.setOnClickListener {
                resetFilters()
            }

            showBtn.setOnClickListener {
                findNavController().popBackStack()
            }

			searchFieldEditText.addTextChangedListener { text ->
				AccountsManager.updateSearchParam(text?.toString() ?: "")
			}
        }

    }

    private fun updateNumberOfResults(numResults: Int?) {
        binding.showBtn.text = getString(R.string.show_results_btn_format, numResults ?: 0)
    }

    private fun resetFilters() {
		viewModel.updateSelectedFilters(emptyMap())
    }

	companion object {
		private const val TAG = "MeetFiltersFragment"
	}

}
