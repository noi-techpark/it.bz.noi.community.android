package it.bz.noi.community.ui.today

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.R
import it.bz.noi.community.models.Event

interface EventClickListener {
    fun onEventClick(eventId: Long)
}

class EventsAdapter(val events: List<Event>) :
    RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_holder_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount() = events.size

    class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val eventName = view.findViewById<TextView>(R.id.tvEventName)
        val eventLocation = view.findViewById<TextView>(R.id.tvEventLocation)
        val eventDate = view.findViewById<TextView>(R.id.tvEventDate)
        val eventTime = view.findViewById<TextView>(R.id.tvEventTime)

        fun bind(event: Event) {
            eventName.text = event.name
            eventLocation.text = event.location
            eventDate.text = event.startDate
            eventTime.text = event.endDate
        }
    }
}