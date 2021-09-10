package it.bz.noi.community.ui.today

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.text.bold
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.card.MaterialCardView
import it.bz.noi.community.R
import it.bz.noi.community.data.models.EventParsed
import it.bz.noi.community.data.models.EventsResponse
import it.bz.noi.community.utils.Constants.getServerDatetimeParser
import java.text.SimpleDateFormat
import java.util.*

interface EventClickListener {
    fun onEventClick(cardEvent: MaterialCardView, event: EventParsed)
}

class EventsAdapter(
    private val events: List<EventsResponse.Event>,
    private val listener: EventClickListener
) :
    RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_holder_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount() = events.size

    inner class EventViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private val cardEvent = view.findViewById<MaterialCardView>(R.id.cardViewEvent)
        private val eventName = view.findViewById<TextView>(R.id.tvEventName)
        private val eventLocation = view.findViewById<TextView>(R.id.tvEventLocation)
        private val eventDate = view.findViewById<TextView>(R.id.tvEventDate)
        private val eventTime = view.findViewById<TextView>(R.id.tvEventTime)
        private val eventImage = view.findViewById<ImageView>(R.id.ivEventImage)

        private lateinit var eventParsed: EventParsed
        private lateinit var days: String

        init {
            view.rootView.setOnClickListener {
                listener.onEventClick(cardEvent, eventParsed)
            }
        }

        fun bind(event: EventsResponse.Event) {
            eventName.text = event.name
            eventLocation.text = event.location

            cardEvent.transitionName = "cardEvent_${event.eventId}"

            val startDate = dateFormatter.format(getServerDatetimeParser().parse(event.startDate))
            val endDate = dateFormatter.format(getServerDatetimeParser().parse(event.endDate))
            val month = "${getMonthCode(getServerDatetimeParser().parse(event.startDate).month)}"
            val eventDateString = if (startDate == endDate) {
                days = "${getServerDatetimeParser().parse(event.startDate).date}\n"
                SpannableStringBuilder()
                    .append(days)
                    .bold {
                        append(month)
                    }
            } else {
                days = "${getServerDatetimeParser().parse(event.startDate).date} - ${
                    getServerDatetimeParser().parse(
                        event.endDate
                    ).date
                }\n"
                SpannableStringBuilder()
                    .append(days)
                    .bold {
                        append(month)
                    }
            }
            eventDate.text = eventDateString

            val startHour = timeFormatter.format(getServerDatetimeParser().parse(event.startDate))
            val endHour = timeFormatter.format(getServerDatetimeParser().parse(event.endDate))
            eventTime.text = "$startHour - $endHour"

            val eventImageUrl = event.imageGallery.firstOrNull()?.imageUrl
            if (eventImageUrl != null) {
                Glide
                    .with(view.context)
                    .load(eventImageUrl)
                    .centerCrop()
                    .into(eventImage)
            }

            eventParsed = EventParsed(
                event.eventId,
                event.name,
                event.location,
                days, month,
                eventTime.text.toString()
            )
        }

        // i mesi partono da 0
        private fun getMonthCode(month: Int): String {
            return when (month) {
                0 -> "JAN"
                1 -> "FEB"
                2 -> "MAR"
                3 -> "APR"
                4 -> "MAY"
                5 -> "JUN"
                6 -> "JUL"
                7 -> "AUG"
                8 -> "SEP"
                9 -> "OCT"
                10 -> "NOV"
                11 -> "DEC"
                else -> throw IllegalArgumentException("Month does not exisr")
            }
        }
    }
}