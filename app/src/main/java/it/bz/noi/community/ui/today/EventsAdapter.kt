package it.bz.noi.community.ui.today

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import it.bz.noi.community.R
import it.bz.noi.community.data.models.EventsResponse
import it.bz.noi.community.utils.Constants.getLocalDateFormatter
import it.bz.noi.community.utils.Constants.getLocalTimeFormatter
import it.bz.noi.community.utils.Constants.getMonthCode
import it.bz.noi.community.utils.Constants.getServerDatetimeParser
import it.bz.noi.community.utils.Utils.getEventName

/**
 * click listener for handling events click
 * the views are required to create the shared animation
 */
interface EventClickListener {
	fun onEventClick(
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
	)
}

/**
 * isSuggestedEvents is a boolean that is used for telling the adapter that is used in the event details
 * suggested events recyclerview
 * fragment parameter to avoid clicked view to have the fade out animation
 */
class EventsAdapter(
	private val events: List<EventsResponse.Event>,
	private val listener: EventClickListener,
	private val fragment: Fragment,
	private val isSuggestedEvents: Boolean = false
) :
	RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
		val view = if (isSuggestedEvents)
			LayoutInflater.from(parent.context)
				.inflate(R.layout.view_holder_suggested_event, parent, false)
		else
			LayoutInflater.from(parent.context)
				.inflate(R.layout.view_holder_event, parent, false)
		return EventViewHolder(view)
	}

	override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
		holder.bind(events[position])
	}

	override fun getItemCount() = events.size

	inner class EventViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

		private val cardEvent = view.findViewById<MaterialCardView>(R.id.cardViewEvent)
		private val cardDate = view.findViewById<CardView>(R.id.cardViewDate)
		private val constraintLayout = view.findViewById<ConstraintLayout>(R.id.constraintLayout)
		private val eventName = view.findViewById<TextView>(R.id.tvEventName)
		private val eventLocation = view.findViewById<TextView>(R.id.tvEventLocation)
		private val eventDate = view.findViewById<TextView>(R.id.tvEventDate)
		private val eventTime = view.findViewById<TextView>(R.id.tvEventTime)
		private val eventImage = view.findViewById<ImageView>(R.id.ivEventImage)
		private val locationIcon = view.findViewById<ImageView>(R.id.ivLocation)
		private val timeIcon = view.findViewById<ImageView>(R.id.ivTime)

		private lateinit var event: EventsResponse.Event

		init {
			view.rootView.setOnClickListener {
				// it should be used for avoiding clicked view to fade out. But for letting other
				// transactions to work in fragment I cannot use this
				//(fragment.exitTransition as TransitionSet).excludeTarget(view, true)
				listener.onEventClick(
					cardEvent,
					cardDate,
					eventName,
					eventLocation,
					eventTime,
					eventImage,
					constraintLayout,
					locationIcon,
					timeIcon,
					event
				)
			}
		}

		fun bind(event: EventsResponse.Event) {
			this.event = event

			eventName.text = getEventName(event)
			eventLocation.text = event.location

			constraintLayout.transitionName = "constraintLayout_${event.eventId}"
			eventName.transitionName = "eventName_${event.eventId}"
			cardDate.transitionName = "cardDate_${event.eventId}"
			eventLocation.transitionName = "eventLocation_${event.eventId}"
			eventTime.transitionName = "eventTime_${event.eventId}"
			eventImage.transitionName = "eventImage_${event.eventId}"
			locationIcon.transitionName = "locationIcon_${event.eventId}"
			timeIcon.transitionName = "timeIcon_${event.eventId}"

			val startDate =
				getLocalDateFormatter().format(getServerDatetimeParser().parse(event.startDate))
			val endDate =
				getLocalDateFormatter().format(getServerDatetimeParser().parse(event.endDate))
			val month = "${getMonthCode(getServerDatetimeParser().parse(event.startDate).month)}"
			val endMonth = "${getMonthCode(getServerDatetimeParser().parse(event.endDate).month)}"
			val eventDateString = if (startDate == endDate) {
				"${getServerDatetimeParser().parse(event.startDate).date}.$month."
			} else {
				if (month == endMonth)
					"${getServerDatetimeParser().parse(event.startDate).date}. -\n ${
						getServerDatetimeParser().parse(
							event.endDate
						).date
					}.$month.\n"
				else
					"${getServerDatetimeParser().parse(event.startDate).date}.$month. -\n ${
						getServerDatetimeParser().parse(
							event.endDate
						).date
					}.$endMonth.\n"
			}

			eventDate.text = eventDateString

			val startHour =
				getLocalTimeFormatter().format(getServerDatetimeParser().parse(event.startDate))
			val endHour =
				getLocalTimeFormatter().format(getServerDatetimeParser().parse(event.endDate))
			eventTime.text = "$startHour - $endHour"

			val eventImageUrl = event.imageGallery?.firstOrNull { it.imageUrl != null }?.imageUrl
			if (eventImageUrl != null) {
				Glide
					.with(view.context)
					.load(eventImageUrl)
					.centerCrop()
					.into(eventImage)
			} else {
				eventImage.setImageResource(R.drawable.img_event_placeholder)
			}
		}
	}
}
