package it.bz.noi.community.ui.today

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.transition.TransitionInflater
import com.google.android.material.card.MaterialCardView
import it.bz.noi.community.R
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.EventsResponse
import it.bz.noi.community.databinding.FragmentTodayBinding
import it.bz.noi.community.ui.MainViewModel
import it.bz.noi.community.ui.TimeRange
import it.bz.noi.community.ui.ViewModelFactory
import it.bz.noi.community.utils.Status

class TodayFragment : Fragment(), EventClickListener, TimeFilterClickListener {

    private lateinit var binding: FragmentTodayBinding
    private lateinit var todayViewModel: TodayViewModel

    private val viewModel: MainViewModel by activityViewModels(factoryProducer = {
        ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))
    })

    private val events = arrayListOf<EventsResponse.Event>()

    private val timeFilterAdapter by lazy {
        TimeFilterAdapter(todayViewModel.timeFilters, this)
    }

    private val eventsAdapter by lazy {
        EventsAdapter(events, this, this)
    }

    private lateinit var layoutManagerFilters: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        todayViewModel =
            ViewModelProvider(this).get(TodayViewModel::class.java)

        exitTransition = TransitionInflater.from(context)
            .inflateTransition(R.transition.events_exit_transition)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTodayBinding.inflate(inflater)
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
            doOnPreDraw {
                startPostponedEnterTransition()
            }
        }

        binding.cdFilterEvents.setOnClickListener {
            findNavController().navigate(TodayFragmentDirections.actionNavigationTodayToFiltersFragment())
        }

        binding.swipeRefreshEvents.setOnRefreshListener {
            viewModel.refresh()
        }

        setupObservers()

        // Avoid a postponeEnterTransition on orientation change, and postpone only of first creation.
        if (savedInstanceState == null) {
            postponeEnterTransition()
        }
    }

    private fun setupObservers() {
        viewModel.mediatorEvents.observe(viewLifecycleOwner, Observer {
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
        })
    }

    private fun retrieveList(events: List<EventsResponse.Event>) {
        this.events.apply {
            clear()
            addAll(events)
        }
        eventsAdapter.notifyDataSetChanged()
    }

    override fun onEventClick(
        cardEvent: MaterialCardView,
        cardDate: CardView,
        eventName: TextView,
        eventLocation: TextView,
        eventTime: TextView,
        eventImage: ImageView,
        constraintLayout: ConstraintLayout,
        locationIcon: ImageView,
        timeIcon: ImageView,
        event: EventsResponse.Event
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
            R.id.action_navigation_today_to_eventDetailsFragment, bundleOf(
                "eventID" to event.eventId,
                "eventName" to event.name,
                "eventLocation" to event.location,
                "imageUrl" to event.imageGallery.firstOrNull()?.imageUrl,
                "eventStartDate" to event.startDate,
                "eventEndDate" to event.endDate,
                "eventDescription" to event.description,
                "technologyFields" to event.technologyFields,
                "roomName" to event.roomName
            ), null, extras
        )
    }

    override fun onTimeFilterClick(position: Int) {
        val oldSelected = todayViewModel.timeFilters.first {
            it.filterSelected
        }
        val oldSelectedIndex = todayViewModel.timeFilters.indexOf(oldSelected)
        todayViewModel.timeFilters[oldSelectedIndex].filterSelected = false
        todayViewModel.timeFilters[position].filterSelected = true
        timeFilterAdapter.notifyDataSetChanged()

        // serve per evitare che venga selezionato un elemento in modo parziale
        if (layoutManagerFilters.findLastCompletelyVisibleItemPosition() < position)
            binding.rvTimeFilters.smoothScrollToPosition(todayViewModel.timeFilters.lastIndex)
        else if (layoutManagerFilters.findFirstCompletelyVisibleItemPosition() > position)
            binding.rvTimeFilters.smoothScrollToPosition(0)

        when (position) {
            0 -> viewModel.filterTime(TimeRange.ALL)
            1 -> viewModel.filterTime(TimeRange.TODAY)
            2 -> viewModel.filterTime(TimeRange.THIS_WEEK)
            3 -> viewModel.filterTime(TimeRange.THIS_MONTH)
            else -> viewModel.filterTime(TimeRange.ALL)
        }
    }
}