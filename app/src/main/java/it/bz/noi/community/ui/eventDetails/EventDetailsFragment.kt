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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.core.text.italic
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
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
import it.bz.noi.community.ui.WebViewFragment
import it.bz.noi.community.ui.today.EventClickListener
import it.bz.noi.community.ui.today.EventsAdapter
import it.bz.noi.community.utils.Constants
import it.bz.noi.community.utils.Constants.getLocalDateFormatter
import it.bz.noi.community.utils.Constants.getLocalTimeFormatter
import it.bz.noi.community.utils.Constants.getMonthCode
import it.bz.noi.community.utils.Status
import java.util.*

class EventDetailsFragment : Fragment(), EventClickListener {
	private lateinit var binding: FragmentEventDetailsBinding

	private val mainViewModel: MainViewModel by activityViewModels(factoryProducer = {
		ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))
	})

	private val suggestedEvents = arrayListOf<EventsResponse.Event>()

	private val eventID by lazy {
		arguments?.getString("eventID")!!
	}

	private val eventName by lazy {
		arguments?.getString("eventName") ?: ""
	}

	private val roomName by lazy {
		arguments?.getString("roomName")
	}

	private val suggestedEventsAdapter by lazy {
		EventsAdapter(
			suggestedEvents,
			this@EventDetailsFragment,
			this@EventDetailsFragment,
			true,
			mainViewModel.locale
		)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		sharedElementEnterTransition =
			TransitionInflater.from(context).inflateTransition(R.transition.change_bounds)
		postponeEnterTransition()

		exitTransition = TransitionInflater.from(context)
			.inflateTransition(R.transition.events_exit_transition)
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
		val eventLocation = arguments?.getString("eventLocation") ?: ""
		val eventStartDate = arguments?.getString("eventStartDate")!!
		val eventEndDate = arguments?.getString("eventEndDate")!!
		val eventImageUrl = arguments?.getString("imageUrl")
		val eventDescription = arguments?.getString("eventDescription") ?: ""
		val technologyFields = arguments?.getStringArrayList("technologyFields") ?: listOf<String>()
		val eventOrganizer = arguments?.getString("eventOrganizer")

		setupTransitions(eventID, eventImageUrl)

		setDate(eventStartDate, eventEndDate)

		(requireActivity() as AppCompatActivity).supportActionBar?.title =
			if (eventName.isNotEmpty())
				eventName
			else
				"No title"

		binding.tvEventName.text = if (eventName.isNotEmpty())
			eventName
		else
			SpannableStringBuilder()
				.italic { append("No title") }
		binding.tvEventLocation.text = eventLocation
		binding.tvEventOrganizer.text = eventOrganizer
		if (eventDescription.isNotEmpty())
			binding.tvEventDescription.text = eventDescription
		else {
			binding.tvAboutLabel.isVisible = false
			binding.tvEventDescription.isVisible = false
		}

		binding.rvSuggestedEvents.apply {
			layoutManager = LinearLayoutManager(requireContext(), HORIZONTAL, false)
			adapter = suggestedEventsAdapter
		}

		mainViewModel.mediatorEvents.observe(viewLifecycleOwner, Observer {
			when (it.status) {
				Status.SUCCESS -> {
					val events = it.data
					if (events != null && events.isNotEmpty()) {
						suggestedEvents.clear()
						for (event in events) {
							if (suggestedEvents.size == 3)
								break
							for (field in technologyFields) {
								if (event.technologyFields?.contains(field) == true) {
									suggestedEvents.add(event)
									break
								}
							}
						}
						suggestedEventsAdapter.notifyItemRangeChanged(0, suggestedEvents.size)
						if (suggestedEvents.isEmpty())
							binding.tvInterestingForYou.isVisible = false
					} else {
						binding.tvInterestingForYou.isVisible = false
					}
				}
			}
		})

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
			mainViewModel.getRoomMapping().observe(viewLifecycleOwner, Observer {
				when (it.status) {
					Status.SUCCESS -> {
						binding.progressBarLoading.isVisible = false
						val mapUrl = it.data?.get(roomName) ?: resources.getString(R.string.url_map)
						findNavController().navigate(
							R.id.action_global_webViewFragment, bundleOf(
								WebViewFragment.TITLE_ARG to if (eventName.isNotEmpty()) eventName else "No title",
								WebViewFragment.URL_ARG to mapUrl
							)
						)
					}
					Status.LOADING -> {
						binding.progressBarLoading.isVisible = true
					}
					Status.ERROR -> {
						binding.progressBarLoading.isVisible = false
						Toast.makeText(requireContext(), it.message, Toast.LENGTH_LONG).show()
					}
				}
			})
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

		Glide
			.with(requireContext())
			.load(eventImageUrl ?: R.drawable.img_event_placeholder)
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
		val endMonth =
			"${getMonthCode(Constants.getServerDatetimeParser().parse(endDatetime).month)}"
		val eventDateString = if (startDate == endDate) {
			"${Constants.getServerDatetimeParser().parse(startDatetime).date}.$month."
		} else {
			if (month == endMonth)
				"${Constants.getServerDatetimeParser().parse(startDatetime).date}. -\n ${
					Constants.getServerDatetimeParser().parse(
						endDatetime
					).date
				}.$month.\n"
			else
				"${Constants.getServerDatetimeParser().parse(startDatetime).date}.$month. -\n ${
					Constants.getServerDatetimeParser().parse(
						endDatetime
					).date
				}.$endMonth.\n"
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
		val eventDescription: String?
		val eventNamed: String?
		when (mainViewModel.locale) {
			"it" -> {
				eventNamed = event.nameIT ?: event.name
				eventDescription = event.descriptionIT ?: event.description
			}
			"de" -> {
				eventNamed = event.nameDE ?: event.name
				eventDescription = event.descriptionDE ?: event.description
			}
			else -> {
				eventNamed = event.nameEN ?: event.name
				eventDescription = event.descriptionEN ?: event.description
			}
		}

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
				"eventName" to eventNamed,
				"eventLocation" to event.location,
				"imageUrl" to event.imageGallery?.firstOrNull { it.imageUrl != null }?.imageUrl,
				"eventStartDate" to event.startDate,
				"eventEndDate" to event.endDate,
				"eventDescription" to eventDescription,
				"technologyFields" to event.technologyFields
			), null, extras
		)
	}
}
