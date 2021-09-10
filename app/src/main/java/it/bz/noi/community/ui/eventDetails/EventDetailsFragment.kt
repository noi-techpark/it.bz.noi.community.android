package it.bz.noi.community.ui.eventDetails

import android.graphics.drawable.Drawable
import android.os.Bundle
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
import androidx.core.text.bold
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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
import it.bz.noi.community.data.models.EventParsed
import it.bz.noi.community.data.models.EventsResponse
import it.bz.noi.community.databinding.FragmentEventDetailsBinding
import it.bz.noi.community.ui.MainViewModel
import it.bz.noi.community.ui.ViewModelFactory
import it.bz.noi.community.ui.today.EventClickListener
import it.bz.noi.community.ui.today.EventsAdapter
import it.bz.noi.community.utils.Status
import java.util.concurrent.TimeUnit

class EventDetailsFragment : Fragment(), EventClickListener {
    private lateinit var binding: FragmentEventDetailsBinding

    private lateinit var viewModel: MainViewModel

    val suggestedEvents = arrayListOf<EventsResponse.Event>()

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
        val eventID = arguments?.getString("eventID")!!
        val eventName = arguments?.getString("eventName")!!
        val eventLocation = arguments?.getString("eventLocation")!!
        val eventDays = arguments?.getString("eventDays")!!
        val eventMonth = arguments?.getString("eventMonth")!!
        val eventTime = arguments?.getString("eventTime")!!
        val eventImageUrl = arguments?.getString("imageUrl")

        setupTransitions(eventID, eventImageUrl)

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

        binding.rvSuggestedEvents.apply {
            layoutManager = LinearLayoutManager(requireContext(), HORIZONTAL, false)
            adapter = EventsAdapter(suggestedEvents, this@EventDetailsFragment)
        }
    }

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
        event: EventParsed
    ) {

    }
}