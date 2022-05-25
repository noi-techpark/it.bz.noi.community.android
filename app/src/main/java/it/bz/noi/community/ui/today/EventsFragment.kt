package it.bz.noi.community.ui.today

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import it.bz.noi.community.R
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.EventsResponse
import it.bz.noi.community.data.models.TimeFilter
import it.bz.noi.community.data.models.TimeRange
import it.bz.noi.community.data.repository.JsonFilterRepository
import it.bz.noi.community.databinding.FragmentEventsBinding
import it.bz.noi.community.ui.MainViewModel
import it.bz.noi.community.ui.ViewModelFactory
import it.bz.noi.community.utils.Status

class EventsFragment : Fragment(), EventClickListener, TimeFilterClickListener {

	private lateinit var binding: FragmentEventsBinding

	private val todayViewModel: TodayViewModel by activityViewModels()

	private val viewModel: MainViewModel by activityViewModels(factoryProducer = {
		ViewModelFactory(
			ApiHelper(RetrofitBuilder.apiService),
			JsonFilterRepository(requireActivity().application)
		)
	})

	private lateinit var timeFilters: List<TimeFilter>

	private lateinit var timeFilterAdapter: TimeFilterAdapter

	private val eventsAdapter by lazy {
		EventsAdapter(todayViewModel.events, this)
	}

	private lateinit var layoutManagerFilters: LinearLayoutManager

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		timeFilters = listOf(
			TimeFilter(
				resources.getString(R.string.time_filter_all),
				viewModel.selectedTimeFilter == TimeRange.ALL
			),
			TimeFilter(
				resources.getString(R.string.time_filter_today),
				viewModel.selectedTimeFilter == TimeRange.TODAY
			),
			TimeFilter(
				resources.getString(R.string.time_filter_this_week),
				viewModel.selectedTimeFilter == TimeRange.THIS_WEEK
			),
			TimeFilter(
				resources.getString(R.string.time_filter_this_month),
				viewModel.selectedTimeFilter == TimeRange.THIS_MONTH
			)
		)
		timeFilterAdapter = TimeFilterAdapter(timeFilters, this)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentEventsBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		layoutManagerFilters = LinearLayoutManager(requireContext(), HORIZONTAL, false)

		binding.rvTimeFilters.apply {
			layoutManager = layoutManagerFilters
			adapter = timeFilterAdapter
		}

		binding.rvEvents.apply {
			layoutManager = LinearLayoutManager(requireContext())
			adapter = eventsAdapter
		}

		binding.cdFilterEvents.setOnClickListener {
			findNavController().navigate(
				TodayFragmentDirections.actionNavigationTodayToFiltersFragment()
			)
		}

		binding.swipeRefreshEvents.setOnRefreshListener {
			viewModel.refreshEvents()
		}

		setupObservers()
	}

	private fun setupObservers() {
		viewModel.mediatorEvents.observe(viewLifecycleOwner) {
			it?.let { resource ->
				when (resource.status) {
					Status.SUCCESS -> {
						binding.swipeRefreshEvents.isRefreshing = false
						val events = resource.data
						if (events != null && events.isNotEmpty()) {
							binding.rvEvents.isVisible = true
							binding.clEmptyState.isVisible = false
							retrieveList(events)
						} else {
							binding.clEmptyState.isVisible = true
							binding.rvEvents.isVisible = false
						}
					}
					Status.ERROR -> {
						binding.swipeRefreshEvents.isRefreshing = false
						binding.rvEvents.isVisible = false
						Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
					}
					Status.LOADING -> {
						binding.swipeRefreshEvents.isRefreshing = true
					}
				}
			}
		}

		viewModel.selectedFiltersCount.observe(viewLifecycleOwner) { count ->
			if (count > 0) {
				binding.appliedFiltersCount.visibility = View.VISIBLE
				binding.appliedFiltersCount.text = "($count)"
			} else {
				binding.appliedFiltersCount.visibility = View.GONE
			}
		}
	}

	private fun retrieveList(events: List<EventsResponse.Event>) {
		todayViewModel.events.apply {
			clear()
			addAll(events)
		}
		eventsAdapter.notifyDataSetChanged()
	}

	override fun onEventClick(
		event: EventsResponse.Event
	) {
		findNavController().navigate(
			TodayFragmentDirections.actionNavigationTodayToEventDetailsFragment(
				todayViewModel.events.indexOf(event)
			)
		)
	}

	override fun onTimeFilterClick(position: Int) {
		// used for avoiding reselection of one element
		if (position != viewModel.selectedTimeFilter.ordinal) {
			timeFilters[position].filterSelected = true
			timeFilters[viewModel.selectedTimeFilter.ordinal].filterSelected = false

			// used for avoiding UI partial filter selection
			if (layoutManagerFilters.findLastCompletelyVisibleItemPosition() < position)
				binding.rvTimeFilters.smoothScrollToPosition(timeFilters.lastIndex)
			else if (layoutManagerFilters.findFirstCompletelyVisibleItemPosition() > position)
				binding.rvTimeFilters.smoothScrollToPosition(0)

			when (position) {
				0 -> viewModel.filterTime(TimeRange.ALL)
				1 -> viewModel.filterTime(TimeRange.TODAY)
				2 -> viewModel.filterTime(TimeRange.THIS_WEEK)
				3 -> viewModel.filterTime(TimeRange.THIS_MONTH)
				else -> viewModel.filterTime(TimeRange.ALL)
			}
			timeFilterAdapter.notifyDataSetChanged()
		}
	}
}
