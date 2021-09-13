package it.bz.noi.community.ui.eventDetails

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.CalendarContract.Events
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.SharedElementCallback
import androidx.core.os.bundleOf
import androidx.core.text.bold
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.card.MaterialCardView
import it.bz.noi.community.R
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.EventsResponse
import it.bz.noi.community.databinding.FragmentEventDetailsBinding
import it.bz.noi.community.ui.MainViewModel
import it.bz.noi.community.ui.ViewModelFactory
import it.bz.noi.community.ui.today.EventClickListener
import it.bz.noi.community.ui.today.EventsAdapter
import it.bz.noi.community.utils.Constants
import it.bz.noi.community.utils.Constants.getLocalDateFormatter
import it.bz.noi.community.utils.Constants.getLocalTimeFormatter
import it.bz.noi.community.utils.Constants.getMonthCode
import it.bz.noi.community.utils.Status
import java.util.*
import java.util.concurrent.TimeUnit

class EventDetailsFragment : Fragment(), EventClickListener {
    private lateinit var binding: FragmentEventDetailsBinding

    private lateinit var viewModel: MainViewModel

    private val suggestedEvents = arrayListOf<EventsResponse.Event>()

    private val eventID by lazy {
        arguments?.getString("eventID")!!
    }

    private val eventName by lazy {
        arguments?.getString("eventName")!!
    }

    private var eventDescription: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))

        ).get(MainViewModel::class.java)

        createMockEvents()

        sharedElementEnterTransition =
            TransitionInflater.from(context).inflateTransition(R.transition.change_bounds)
        postponeEnterTransition(100, TimeUnit.MILLISECONDS)

        exitTransition = TransitionInflater.from(context)
            .inflateTransition(R.transition.events_exit_transition)

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

    private fun createMockEvents() {
        suggestedEvents.add(
            EventsResponse.Event(
                "zio",
                "Evento test1",
                "NOI Techpark",
                "2021-09-14T08:00:00",
                "2021-09-14T18:00:00",
                listOf()
            )
        )
        suggestedEvents.add(
            EventsResponse.Event(
                "zio",
                "Evento test2",
                "NOI Techpark",
                "2021-09-18T10:00:00",
                "2021-09-20T12:00:00",
                listOf()
            )
        )
        suggestedEvents.add(
            EventsResponse.Event(
                "zio",
                "Evento test3",
                "NOI Techpark",
                "2021-09-29T08:00:00",
                "2021-09-29T18:00:00",
                listOf()
            )
        )
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
        val eventLocation = arguments?.getString("eventLocation")!!
        val eventStartDate = arguments?.getString("eventStartDate")!!
        val eventEndDate = arguments?.getString("eventEndDate")!!
        val eventImageUrl = arguments?.getString("imageUrl")

        setupTransitions(eventID, eventImageUrl)

        setDate(eventStartDate, eventEndDate)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = eventName

        binding.tvEventName.text = eventName
        binding.tvEventLocation.text = eventLocation

        viewModel.getEventDetails(eventID).observe(viewLifecycleOwner, Observer {
            it.let { resource ->
                when (resource.status) {
                    Status.SUCCESS -> {
                        eventDescription = resource.data?.eventDescription
                        binding.tvEventDescription.text = eventDescription
                        binding.groupEventActions.isVisible = true
                        binding.groupEventServerData.isVisible = true
                    }
                }
            }
        })

        binding.rvSuggestedEvents.apply {
            layoutManager = LinearLayoutManager(requireContext(), HORIZONTAL, false)
            adapter = EventsAdapter(suggestedEvents, this@EventDetailsFragment, this@EventDetailsFragment, true)
        }

        binding.btnAddToCalendar.setOnClickListener {
            val beginTime = Constants.getServerDatetimeParser().parse(eventStartDate).time
            val endTime = Constants.getServerDatetimeParser().parse(eventEndDate).time

            val intent: Intent = Intent(Intent.ACTION_INSERT)
                .setData(Events.CONTENT_URI)
                .putExtra(
                    CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                    beginTime
                )
                .putExtra(
                    CalendarContract.EXTRA_EVENT_END_TIME,
                    endTime
                )
                .putExtra(Events.TITLE, eventName)
                .putExtra(Events.DESCRIPTION, eventDescription)
                .putExtra(Events.EVENT_LOCATION, eventLocation)
                .putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY)

            startActivity(intent)
        }

        binding.btnFindOnMaps.setOnClickListener {
            findNavController().navigate(R.id.action_global_webViewFragment, bundleOf(
                "title" to eventName, "url" to "https://maps.noi.bz.it"
            ))
        }
    }

    /**
     * Setup the transition for having the shared animation effect
     */
    private fun setupTransitions(eventID: String, eventImageUrl: String?) {
        ViewCompat.setTransitionName(binding.cardViewDate, "cardDate_${eventID}")
        ViewCompat.setTransitionName(binding.constraintLayout, "constraintLayout_${eventID}")
        ViewCompat.setTransitionName(binding.tvEventName, "eventName_${eventID}")
        ViewCompat.setTransitionName(binding.tvEventLocation, "eventLocation_${eventID}")
        ViewCompat.setTransitionName(binding.tvEventTime, "eventTime_${eventID}")
        ViewCompat.setTransitionName(binding.ivEventImage, "eventImage_${eventID}")
        ViewCompat.setTransitionName(binding.ivLocation, "locationIcon_${eventID}")
        ViewCompat.setTransitionName(binding.ivTime, "timeIcon_${eventID}")

        if (eventImageUrl != null) {
            Glide
                .with(requireContext())
                .load(eventImageUrl)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        startPostponedEnterTransition()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        startPostponedEnterTransition()
                        return false
                    }

                })
                .centerCrop()
                .into(binding.ivEventImage)
        }
    }

    /**
     * Parsing of the date to populate date container and time
     */
    private fun setDate(startDatetime: String, endDatetime: String) {
        val startDate =
            getLocalDateFormatter().format(Constants.getServerDatetimeParser().parse(startDatetime))
        val endDate =
            getLocalDateFormatter().format(Constants.getServerDatetimeParser().parse(endDatetime))
        val month =
            "${getMonthCode(Constants.getServerDatetimeParser().parse(startDatetime).month)}"
        val eventDateString = if (startDate == endDate) {
            SpannableStringBuilder()
                .append("${Constants.getServerDatetimeParser().parse(startDatetime).date}\n")
                .bold {
                    append(month)
                }
        } else {
            SpannableStringBuilder()
                .append(
                    "${Constants.getServerDatetimeParser().parse(startDatetime).date} - ${
                        Constants.getServerDatetimeParser().parse(
                            endDatetime
                        ).date
                    }\n"
                )
                .bold {
                    append(month)
                }
        }
        binding.tvEventDate.text = eventDateString

        val startHour =
            getLocalTimeFormatter().format(Constants.getServerDatetimeParser().parse(startDatetime))
        val endHour =
            getLocalTimeFormatter().format(Constants.getServerDatetimeParser().parse(endDatetime))
        binding.tvEventTime.text = "$startHour - $endHour"
    }

    /**
     * Clicking on suggested event will result in open another EventDetailsFragment instance
     */
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
            R.id.action_eventDetailsFragment_self, bundleOf(
                "eventID" to event.eventId,
                "eventName" to event.name,
                "eventLocation" to event.location,
                "eventStartDate" to event.startDate,
                "eventEndDate" to event.endDate,
                "eventImage" to event.imageGallery.firstOrNull()?.imageUrl
            ), null, extras
        )
    }
}