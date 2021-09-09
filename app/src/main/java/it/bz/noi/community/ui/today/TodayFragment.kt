package it.bz.noi.community.ui.today

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.EventsResponse
import it.bz.noi.community.databinding.FragmentTodayBinding
import it.bz.noi.community.ui.MainViewModel
import it.bz.noi.community.ui.ViewModelFactory
import it.bz.noi.community.utils.Status

class TodayFragment : Fragment(), EventClickListener, TimeFilterClickListener {

    private lateinit var binding: FragmentTodayBinding
    private lateinit var todayViewModel: TodayViewModel

    private lateinit var viewModel: MainViewModel

    private val events = arrayListOf<EventsResponse.Event>()

    private val timeFilterAdapter by lazy {
        TimeFilterAdapter(todayViewModel.timeFilters, this)
    }

    private val eventsAdapter by lazy {
        EventsAdapter(events)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))

        ).get(MainViewModel::class.java)
        todayViewModel =
            ViewModelProvider(this).get(TodayViewModel::class.java)
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

        binding.rvTimeFilters.apply {
            layoutManager = LinearLayoutManager(requireContext(), HORIZONTAL, false)
            adapter = timeFilterAdapter
        }

        binding.rvEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventsAdapter
        }

        setupObservers()
    }

    private fun setupObservers() {
        viewModel.getEvents().observe(viewLifecycleOwner, Observer {
            it?.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        binding.rvEvents.visibility = View.VISIBLE
                        resource.data?.let { events ->
                            retrieveList(events)
                        }
                    }
                    Status.ERROR -> {
                        binding.rvEvents.visibility = View.VISIBLE
                        Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
                    }
                    Status.LOADING -> {
                        binding.rvEvents.visibility = View.GONE
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

    override fun onEventClick(eventId: Long) {
        TODO("Not yet implemented")
    }

    override fun onTimeFilterClick(position: Int) {
        val oldSelected = todayViewModel.timeFilters.first {
            it.filterSelected
        }
        val oldSelectedIndex = todayViewModel.timeFilters.indexOf(oldSelected)
        todayViewModel.timeFilters[oldSelectedIndex].filterSelected = false
        todayViewModel.timeFilters[position].filterSelected = true
        timeFilterAdapter.notifyDataSetChanged()
    }
}