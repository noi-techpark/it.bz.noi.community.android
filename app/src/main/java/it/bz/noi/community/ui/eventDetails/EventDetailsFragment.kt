// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

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
import androidx.core.view.doOnPreDraw
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
import com.bumptech.glide.request.target.Target
import com.google.android.material.card.MaterialCardView
import it.bz.noi.community.R
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.EventsResponse
import it.bz.noi.community.data.repository.JsonFilterRepository
import it.bz.noi.community.databinding.FragmentEventDetailsBinding
import it.bz.noi.community.ui.MainViewModel
import it.bz.noi.community.ui.ViewModelFactory
import it.bz.noi.community.ui.WebViewFragment
import it.bz.noi.community.ui.today.events.EventClickListener
import it.bz.noi.community.ui.today.events.EventsAdapter
import it.bz.noi.community.utils.DateUtils
import it.bz.noi.community.utils.Status
import it.bz.noi.community.utils.Utils
import it.bz.noi.community.utils.Utils.getEventDescription
import it.bz.noi.community.utils.Utils.getEventName
import it.bz.noi.community.utils.Utils.getEventOrganizer
import it.bz.noi.community.utils.Utils.getImageUrl
import java.util.*

class EventDetailsFragment : Fragment(), EventClickListener {
	private lateinit var binding: FragmentEventDetailsBinding

	private val args: EventDetailsFragmentArgs by navArgs()

	private val mainViewModel: MainViewModel by activityViewModels(factoryProducer = {
		ViewModelFactory(
			ApiHelper(RetrofitBuilder.opendatahubApiService, RetrofitBuilder.communityApiService),
			JsonFilterRepository(requireActivity().application)
		)
	})

	private lateinit var allEvents: ArrayList<EventsResponse.Event>

	private lateinit var selectedEvent: EventsResponse.Event

	private val suggestedEvents = arrayListOf<EventsResponse.Event>()

	private val suggestedEventsAdapter by lazy {
		EventsAdapter(
			suggestedEvents,
			this@EventDetailsFragment,
			true
		)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		sharedElementEnterTransition =
			TransitionInflater.from(requireContext()).inflateTransition(R.transition.events_details_enter_transition)
		sharedElementReturnTransition = null

		if (savedInstanceState == null)
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

			doOnPreDraw {
				startPostponedEnterTransition()
			}
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

						setTransitionNames(selectedEvent.eventId!!)
						loadEventImage(getImageUrl(selectedEvent))

						setDate(selectedEvent.startDate, selectedEvent.endDate)

						if (selectedEvent.webAddress != null) {
							binding.addToCalendarOrSignup.text = getString(R.string.btn_sign_up)
							binding.addToCalendarOrSignup.setIconResource(R.drawable.ic_sign_up)
							binding.addToCalendarOrSignup.setOnClickListener {
								val browserIntent =
									Intent(Intent.ACTION_VIEW, Uri.parse(selectedEvent.webAddress))
								startActivity(browserIntent)
							}
						} else {
							binding.addToCalendarOrSignup.text =
								getString(R.string.btn_add_to_calendar)
							binding.addToCalendarOrSignup.setIconResource(R.drawable.ic_add_to_calendar)
							binding.addToCalendarOrSignup.setOnClickListener {
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
									.putExtra(
										Events.DESCRIPTION,
										getEventDescription(selectedEvent)
									)
									.putExtra(Events.EVENT_LOCATION, selectedEvent.location)
									.putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY)

								startActivity(intent)
							}
						}

						binding.findOnMaps.setOnClickListener {
							mainViewModel.getRoomMapping().observe(viewLifecycleOwner, Observer {
								when (it.status) {
									Status.SUCCESS -> {
										binding.progressBarLoading.isVisible = false

										val mapTitle = selectedEvent.location
										val mapUrl = it.data?.get(selectedEvent.roomName)
											?: resources.getString(R.string.url_map)

										findNavController().navigate(
											R.id.action_global_webViewFragment, bundleOf(
												WebViewFragment.TITLE_ARG to mapTitle,
												WebViewFragment.URL_ARG to Utils.addParamsToUrl(
													mapUrl,
													fullview = true,
													hidezoom = true
												)
											)
										)
									}
									Status.LOADING -> {
										binding.progressBarLoading.isVisible = true
									}
									Status.ERROR -> {
										binding.progressBarLoading.isVisible = false
										Toast.makeText(
											requireContext(),
											it.message,
											Toast.LENGTH_LONG
										).show()
									}
								}
							})
						}

						binding.tvEventName.text = getEventName(selectedEvent)
						binding.tvEventLocation.text = selectedEvent.location
						binding.tvEventOrganizer.text = getEventOrganizer(selectedEvent)
						if (getEventDescription(selectedEvent).isNullOrEmpty()) {
							binding.tvAboutLabel.isVisible = false
							binding.tvEventDescription.isVisible = false
						} else
							binding.tvEventDescription.text = getEventDescription(selectedEvent)

						populateSuggestedEvents(events)
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
	}

	/**
	 * populate the UI of the suggested filters
	 */
	private fun populateSuggestedEvents(events: List<EventsResponse.Event>) {
		suggestedEvents.clear()
		for (event in events) {
			if (suggestedEvents.size == 3)
				break
			if (event.eventId != selectedEvent.eventId) {
				for (field in selectedEvent.technologyFields ?: listOf()) {
					if (event.technologyFields?.contains(field) == true) {
						suggestedEvents.add(event)
						break
					}
				}
			}
		}
		suggestedEventsAdapter.notifyItemRangeChanged(0, suggestedEvents.size)
		if (suggestedEvents.isEmpty())
			binding.tvInterestingForYou.isVisible = false
	}

	/**
	 * Setup the transition for having the shared animation effect
	 */
	private fun setTransitionNames(eventId: String) {
		binding.cardViewDate.transitionName = "cardDate_${eventId}"
		binding.constraintLayout.transitionName = "constraintLayout_${eventId}"
		binding.tvEventName.transitionName = "eventName_${eventId}"
		binding.tvEventLocation.transitionName = "eventLocation_${eventId}"
		binding.tvEventTime.transitionName = "eventTime_${eventId}"
		binding.ivEventImage.transitionName = "eventImage_${eventId}"
		binding.ivLocation.transitionName = "locationIcon_${eventId}"
		binding.ivTime.transitionName = "timeIcon_${eventId}"
	}

	private fun loadEventImage(eventImageUrl: String?) {
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
			.placeholder(R.drawable.placeholder_noi_events)
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
		event: EventsResponse.Event,
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
			EventDetailsFragmentDirections.actionEventDetailsFragmentSelf(
				allEvents.indexOf(event)
			),
			extras
		)
	}
}
