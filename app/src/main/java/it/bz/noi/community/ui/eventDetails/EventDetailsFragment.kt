package it.bz.noi.community.ui.eventDetails

import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.CalendarContract.Events
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
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
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
import it.bz.noi.community.utils.DateUtils
import it.bz.noi.community.utils.Status
import it.bz.noi.community.utils.Utils.getEventDescription
import it.bz.noi.community.utils.Utils.getEventName
import it.bz.noi.community.utils.Utils.getEventOrganizer
import it.bz.noi.community.utils.Utils.getImageUrl
import java.util.*

class EventDetailsFragment : Fragment(), EventClickListener {
	private lateinit var binding: FragmentEventDetailsBinding

	private val args: EventDetailsFragmentArgs by navArgs()

	private val mainViewModel: MainViewModel by activityViewModels(factoryProducer = {
		ViewModelFactory(ApiHelper(RetrofitBuilder.apiService))
	})

	private lateinit var allEvents: ArrayList<EventsResponse.Event>

	private lateinit var selectedEvent: EventsResponse.Event

	private val suggestedEvents = arrayListOf<EventsResponse.Event>()

	private val suggestedEventsAdapter by lazy {
		EventsAdapter(
			suggestedEvents,
			this@EventDetailsFragment,
			this@EventDetailsFragment,
			true
		)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		sharedElementEnterTransition =
			TransitionInflater.from(context).inflateTransition(R.transition.change_bounds)
		postponeEnterTransition()
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

		binding.rvSuggestedEvents.apply {
			layoutManager = LinearLayoutManager(requireContext(), HORIZONTAL, false)
			adapter = suggestedEventsAdapter
		}

		mainViewModel.mediatorEvents.observe(viewLifecycleOwner, Observer {
			when (it.status) {
				Status.SUCCESS -> {
					val events = it.data
					if (events != null && events.isNotEmpty()) {
						allEvents = events as ArrayList<EventsResponse.Event>
						selectedEvent = events[args.eventID]

						(requireActivity() as AppCompatActivity).supportActionBar?.title =
							getEventName(selectedEvent)

						setupTransitions(selectedEvent.eventId!!, getImageUrl(selectedEvent))

						setDate(selectedEvent.startDate, selectedEvent.endDate)

						binding.tvEventName.text = getEventName(selectedEvent)
						binding.tvEventLocation.text = selectedEvent.location
						binding.tvEventOrganizer.text = getEventOrganizer(selectedEvent)
						if (getEventDescription(selectedEvent).isNullOrEmpty()) {
							binding.tvAboutLabel.isVisible = false
							binding.tvEventDescription.isVisible = false
						} else
							binding.tvEventDescription.text = getEventDescription(selectedEvent)

						suggestedEvents.clear()
						for (event in events) {
							if (suggestedEvents.size == 3)
								break
							for (field in selectedEvent.technologyFields ?: listOf()) {
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
				Status.ERROR -> {
				}
				Status.LOADING -> {
				}
			}
		})

		binding.btnAddToCalendar.setOnClickListener {
			val beginTime = selectedEvent.startDate.time
			val endTime = selectedEvent.endDate.time

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
				.putExtra(Events.TITLE, getEventName(selectedEvent))
				.putExtra(Events.DESCRIPTION, getEventDescription(selectedEvent))
				.putExtra(Events.EVENT_LOCATION, selectedEvent.location)
				.putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY)

			startActivity(intent)
		}

		binding.btnFindOnMaps.setOnClickListener {
			mainViewModel.getRoomMapping().observe(viewLifecycleOwner, Observer {
				when (it.status) {
					Status.SUCCESS -> {
						binding.progressBarLoading.isVisible = false

						val mapTitle = if (it.data?.get(selectedEvent.roomName) != null)
							selectedEvent.roomName!!
						else
							getString(R.string.title_generic_noi_techpark_map)
						var mapUrl = it.data?.get(selectedEvent.roomName)
							?: resources.getString(R.string.url_map)

						val originalUri = Uri.parse(mapUrl)

						val scheme = originalUri.scheme
						val authority = originalUri.authority
						val path = originalUri.path
						val query = originalUri.query

						val newUri = Uri.Builder()
							.scheme(scheme)
							.authority(authority)
							.path(path)
							.query(query)
							.appendQueryParameter("fullview", "1")
							.appendQueryParameter("hidezoom", "1")
							.build()

						mapUrl = newUri.toString()

						findNavController().navigate(
							R.id.action_global_webViewFragment, bundleOf(
								WebViewFragment.TITLE_ARG to mapTitle,
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

		val options: RequestOptions = RequestOptions()
			.placeholder(R.drawable.img_event_placeholder)

		Glide
			.with(requireContext())
			.load(eventImageUrl)
			.apply(options)
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
	private fun setDate(startDatetime: Date, endDatetime: Date) {
		binding.tvEventDate.text = DateUtils.getDateIntervalString(startDatetime, endDatetime)
		binding.tvEventTime.text = DateUtils.getHoursIntervalString(startDatetime, endDatetime)
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
			EventDetailsFragmentDirections.actionEventDetailsFragmentSelf(
				allEvents.indexOf(event)
			),
			extras
		)
	}
}
