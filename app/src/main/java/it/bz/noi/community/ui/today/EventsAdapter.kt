package it.bz.noi.community.ui.today

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import it.bz.noi.community.R
import it.bz.noi.community.data.models.EventsResponse
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

		private val eventName = view.findViewById<TextView>(R.id.tvEventName)
		private val eventLocation = view.findViewById<TextView>(R.id.tvEventLocation)
		private val eventDate = view.findViewById<TextView>(R.id.tvEventDate)
		private val eventTime = view.findViewById<TextView>(R.id.tvEventTime)
		private val eventImage = view.findViewById<ImageView>(R.id.ivEventImage)

		private lateinit var event: EventsResponse.Event

		init {
			view.rootView.setOnClickListener {
				listener.onEventClick(
					event
				)
			}
		}

		fun bind(event: EventsResponse.Event) {
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
		}

	}

}
