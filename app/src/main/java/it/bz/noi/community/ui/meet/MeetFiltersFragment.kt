package it.bz.noi.community.ui.meet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import it.bz.noi.community.R
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.*
import it.bz.noi.community.data.repository.AccountsManager
import it.bz.noi.community.databinding.FragmentFiltersBinding
import it.bz.noi.community.ui.UpdateResultsListener
import it.bz.noi.community.ui.meet.MeetFiltersAdapter.Companion.COMPANY_FILTER
import it.bz.noi.community.ui.meet.MeetFiltersAdapter.Companion.RESEARCH_INSTITUTION_FILTER
import it.bz.noi.community.ui.meet.MeetFiltersAdapter.Companion.STARTUP_FILTER
import kotlinx.coroutines.launch

class MeetFiltersFragment : Fragment() {

    private lateinit var filterAdapter: MeetFiltersAdapter

    private var _binding: FragmentFiltersBinding? = null
	private val binding get() = _binding!!

	private val viewModel: MeetViewModel by navGraphViewModels(R.id.navigation_meet, factoryProducer = {
		MeetViewModelFactory(apiHelper = ApiHelper(RetrofitBuilder.opendatahubApiService, RetrofitBuilder.communityApiService), this)
	})

	private val updateResultsListener = object : UpdateResultsListener {
		override fun updateResults() {
			// TODO
		//	viewModel.updateSelectedFilters(filterAdapter.filters.filter { it.checked })
		}
	}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

		val headers = mapOf(
			COMPANY_FILTER to getString(R.string.filter_by_company),
			STARTUP_FILTER to getString(R.string.filter_by_startup),
			RESEARCH_INSTITUTION_FILTER to getString(R.string.filter_by_research_institution)
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
	}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

		viewLifecycleOwner.lifecycleScope.launch {
			viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
				AccountsManager.availableAccountsFilters.collect { filters ->
					filterAdapter.filters = filters
				}
			}
		}




		// TODO
/*		viewModel.appliedFilters.observe(requireActivity()) {
			filterAdapter.filters = it
			mainViewModel.refreshEvents()
		}

        viewModel.mediatorEvents.observe(viewLifecycleOwner) {
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
		}*/

        binding.apply {
            filterstRV.adapter = filterAdapter

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
    	// TODO
		//viewModel.updateSelectedFilters(emptyList())
    }

	companion object {
		private const val TAG = "FiltersFragment"
	}

}
