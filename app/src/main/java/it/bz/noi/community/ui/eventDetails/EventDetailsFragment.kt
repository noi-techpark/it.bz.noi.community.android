package it.bz.noi.community.ui.eventDetails

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.SharedElementCallback
import androidx.core.text.bold
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.transition.TransitionInflater
import it.bz.noi.community.R
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.databinding.FragmentEventDetailsBinding
import it.bz.noi.community.ui.MainViewModel
import it.bz.noi.community.ui.ViewModelFactory
import it.bz.noi.community.utils.Status
import java.util.concurrent.TimeUnit

class EventDetailsFragment : Fragment() {
    private lateinit var binding: FragmentEventDetailsBinding

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))

        ).get(MainViewModel::class.java)

        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(R.transition.change_bounds)
        postponeEnterTransition(100, TimeUnit.MILLISECONDS)

        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onSharedElementEnd(
                sharedElementNames: List<String?>?,
                sharedElements: List<View?>?,
                sharedElementSnapshots: List<View?>?
            ) {
                binding.groupEventActions.isVisible = true
                binding.groupEventServerData.isVisible = true
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEventDetailsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val eventID = arguments?.getString("eventID")!!
        val eventName = arguments?.getString("eventName")!!
        val eventLocation = arguments?.getString("eventLocation")!!
        val eventDays = arguments?.getString("eventDays")!!
        val eventMonth = arguments?.getString("eventMonth")!!
        val eventTime = arguments?.getString("eventTime")!!
        ViewCompat.setTransitionName(binding.cardViewDate, "cardDate_${eventID}")
        ViewCompat.setTransitionName(binding.constraintLayout, "constraintLayout_${eventID}")
        ViewCompat.setTransitionName(binding.tvEventName, "eventName_${eventID}")
        ViewCompat.setTransitionName(binding.tvEventLocation, "eventLocation_${eventID}")
        ViewCompat.setTransitionName(binding.tvEventTime, "eventTime_${eventID}")
        ViewCompat.setTransitionName(binding.ivEventImage, "eventImage_${eventID}")
        ViewCompat.setTransitionName(binding.ivLocation, "locationIcon_${eventID}")
        ViewCompat.setTransitionName(binding.ivTime, "timeIcon_${eventID}")
        (requireActivity() as AppCompatActivity).supportActionBar?.title = eventName

        binding.tvEventName.text = eventName
        binding.tvEventLocation.text = eventLocation
        binding.tvEventDate.text = SpannableStringBuilder()
            .append(eventDays)
            .bold { append(eventMonth) }
        binding.tvEventTime.text = eventTime

        viewModel.getEventDetails(eventID).observe(viewLifecycleOwner, Observer {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        binding.tvEventDescription.text = resource.data?.eventDescription
                    }
                }
            }
        })
    }
}