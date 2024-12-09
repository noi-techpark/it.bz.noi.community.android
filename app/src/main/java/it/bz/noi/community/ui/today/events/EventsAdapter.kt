// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.today.events

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.card.MaterialCardView
import it.bz.noi.community.R
import it.bz.noi.community.data.models.Event
import it.bz.noi.community.utils.DateUtils.getDateIntervalString
import it.bz.noi.community.utils.DateUtils.getHoursIntervalString
import it.bz.noi.community.utils.Utils
import it.bz.noi.community.utils.Utils.getEventName

/**
 * click listener for handling events click
 * the views are required to create the shared animation
 */
interface EventClickListener {
	fun onEventClick(
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
	)
}

/**
 * isSuggestedEvents is a boolean that is used for telling the adapter that is used in the event details
 * suggested events recyclerview
 * fragment parameter to avoid clicked view to have the fade out animation
 */
class EventsAdapter(
	private val events: List<Event>,
	private val listener: EventClickListener,
	private val isSuggestedEvents: Boolean = false
) :
	RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
		val view = if (isSuggestedEvents)
			LayoutInflater.from(parent.context)
				.inflate(R.layout.vh_suggested_event, parent, false)
		else
			LayoutInflater.from(parent.context)
				.inflate(R.layout.vh_event, parent, false)
		return EventViewHolder(view)
	}

	override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
		holder.bind(events[position])
	}

	override fun getItemCount() = events.size

	inner class EventViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

		private val eventName = view.findViewById<TextView>(R.id.tvEventName)
		private val eventLocation = view.findViewById<TextView>(R.id.tvEventLocation)
		private val eventDate = view.findViewById<TextView>(R.id.tvEventDate)
		private val eventTime = view.findViewById<TextView>(R.id.tvEventTime)
		private val eventImage = view.findViewById<ImageView>(R.id.ivEventImage)
		private val cardEvent = view.findViewById<MaterialCardView>(R.id.cardViewEvent)
		private val cardDate = view.findViewById<CardView>(R.id.cardViewDate)
		private val constraintLayout = view.findViewById<ConstraintLayout>(R.id.constraintLayout)
		private val locationIcon = view.findViewById<ImageView>(R.id.ivLocation)
		private val timeIcon = view.findViewById<ImageView>(R.id.ivTime)

		private lateinit var event: Event

		init {
			view.rootView.setOnClickListener {
				listener.onEventClick(
					event,
					cardEvent,
					cardDate,
					eventName,
					eventLocation,
					eventTime,
					eventImage,
					constraintLayout,
					locationIcon,
					timeIcon,
				)
			}
		}

		fun bind(event: Event) {
			this.event = event

			eventName.text = getEventName(event)
			eventLocation.text = event.location

			eventDate.text = getDateIntervalString(event.startDate, event.endDate)
			eventTime.text = getHoursIntervalString(event.startDate, event.endDate)

			val options: RequestOptions = RequestOptions()
				.placeholder(R.drawable.placeholder_noi_events)
			Glide
				.with(view.context)
				.load(Utils.getImageUrl(event))
				.apply(options)
				.centerCrop()
				.into(eventImage)

			setTransitionNames(event.eventId!!)
		}

		private fun setTransitionNames(eventId: String) {
			constraintLayout.transitionName = "constraintLayout_$eventId"
			eventName.transitionName = "eventName_$eventId"
			cardDate.transitionName = "cardDate_$eventId"
			eventLocation.transitionName = "eventLocation_$eventId"
			eventTime.transitionName = "eventTime_$eventId"
			eventImage.transitionName = "eventImage_$eventId"
			locationIcon.transitionName = "locationIcon_$eventId"
			timeIcon.transitionName = "timeIcon_$eventId"
		}

	}

}
