// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.today.events

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.google.android.material.card.MaterialCardView
import it.bz.noi.community.R
import it.bz.noi.community.data.models.Event
import it.bz.noi.community.data.models.TimeFilter
import it.bz.noi.community.data.models.TimeRange
import it.bz.noi.community.databinding.FragmentEventsBinding
import it.bz.noi.community.ui.MainViewModel
import it.bz.noi.community.ui.ViewModelFactory
import it.bz.noi.community.ui.today.TodayViewModel
import it.bz.noi.community.utils.Status
import it.bz.noi.community.ui.today.TodayFragmentDirections
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
class EventsFragment : Fragment(), EventClickListener, TimeFilterClickListener {

	private var _binding: FragmentEventsBinding? = null
	private val binding get() = _binding!!

	private val todayViewModel: TodayViewModel by activityViewModels()

	private val viewModel: MainViewModel by activityViewModels(factoryProducer = {
		ViewModelFactory.defaultFactory()
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

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentEventsBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		layoutManagerFilters = LinearLayoutManager(requireContext(), HORIZONTAL, false)

		binding.timeFiltersRecyclerView.apply {
			addItemDecoration(TimeFilterItemDecoration())
			layoutManager = layoutManagerFilters
			adapter = timeFilterAdapter
		}

		binding.eventsRecyclerView.apply {
			addItemDecoration(EventsItemDecoration())
			layoutManager = LinearLayoutManager(requireContext())
			adapter = eventsAdapter
			doOnPreDraw {
				startPostponedEnterTransition()
			}
		}

		binding.editFiltersButton.root.setOnClickListener {
			exitTransition = null
			findNavController().navigate(
				TodayFragmentDirections.actionNavigationTodayToEventFiltersFragment()
			)
		}

		binding.eventsSwipeToRefresh.setOnRefreshListener {
			viewModel.refreshEvents()
		}

		setupObservers()
	}

	private fun setupObservers() {
		viewModel.mediatorEvents.observe(viewLifecycleOwner) { resource ->
			if (resource == null) return@observe
			when (resource.status) {
				Status.SUCCESS -> {
					binding.eventsSwipeToRefresh.isRefreshing = false
					val events = resource.data
					if (!events.isNullOrEmpty()) {
						binding.eventsRecyclerView.isVisible = true
						binding.emptyState.root.isVisible = false
						retrieveList(events)
					} else {
						binding.eventsRecyclerView.isVisible = false
						binding.emptyState.apply {
							root.isVisible = true
							subtitle.text = getString(R.string.label_events_empty_state_subtitle)
						}
					}
				}

				Status.ERROR -> {
					binding.eventsSwipeToRefresh.isRefreshing = false
					binding.eventsRecyclerView.isVisible = false
					Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
				}

				Status.LOADING -> {
					binding.eventsSwipeToRefresh.isRefreshing = true
				}
			}
		}

		viewModel.selectedEventFiltersCount.observe(viewLifecycleOwner) { count ->
			binding.editFiltersButton.appliedFiltersCount.apply {
				if (count > 0) {
					isVisible = true
					text = "$count"
				} else {
					isVisible = false
				}
			}
		}
	}

	private fun retrieveList(events: List<Event>) {

		todayViewModel.events.apply {
			clear()
			addAll(events)
		}
		eventsAdapter.notifyDataSetChanged()
	}

	override fun onEventClick(
		event: Event,
		cardEvent: MaterialCardView,
		cardDate: CardView,
		eventName: TextView,
		eventLocation: TextView,
		eventTime: TextView,
		eventImage: ImageView,
		constraintLayout: ConstraintLayout,
		locationIcon: ImageView,
		timeIcon: ImageView,
	) {
		val extras = FragmentNavigatorExtras(
			constraintLayout to "constraintLayout_${event.eventId}",
			eventName to "eventName_${event.eventId}",
			cardDate to "cardDate_${event.eventId}",
			eventLocation to "eventLocation_${event.eventId}",
			eventTime to "eventTime_${event.eventId}",
			eventImage to "eventImage_${event.eventId}",
			locationIcon to "locationIcon_${event.eventId}",
			timeIcon to "timeIcon_${event.eventId}"
		)
		findNavController().navigate(
			TodayFragmentDirections.actionNavigationTodayToEventDetailsFragment(
				null,
				event
			),
			extras
		)
	}

	@SuppressLint("NotifyDataSetChanged")
	override fun onTimeFilterClick(position: Int) {
		// used for avoiding reselection of one element
		if (position != viewModel.selectedTimeFilter.ordinal) {
			timeFilters[position].filterSelected = true
			timeFilters[viewModel.selectedTimeFilter.ordinal].filterSelected = false

			// used for avoiding UI partial filter selection
			if (layoutManagerFilters.findLastCompletelyVisibleItemPosition() < position)
				binding.timeFiltersRecyclerView.smoothScrollToPosition(timeFilters.lastIndex)
			else if (layoutManagerFilters.findFirstCompletelyVisibleItemPosition() > position)
				binding.timeFiltersRecyclerView.smoothScrollToPosition(0)

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
