package it.bz.noi.community.ui.today

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.bold
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.R
import it.bz.noi.community.data.models.EventsResponse
import java.text.SimpleDateFormat
import java.util.*

interface EventClickListener {
    fun onEventClick(eventId: Long)
}

class EventsAdapter(val events: List<EventsResponse.Event>) :
    RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    private val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val timeParser = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateParser = SimpleDateFormat("yyyy-MM-dd")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_holder_event, parent, false)
        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(events[position])
    }

    override fun getItemCount() = events.size

    inner class EventViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val eventName = view.findViewById<TextView>(R.id.tvEventName)
        val eventLocation = view.findViewById<TextView>(R.id.tvEventLocation)
        val eventDate = view.findViewById<TextView>(R.id.tvEventDate)
        val eventTime = view.findViewById<TextView>(R.id.tvEventTime)

        fun bind(event: EventsResponse.Event) {
            eventName.text = event.name
            eventLocation.text = event.location

            val startDate = dateParser.format(sdf.parse(event.startDate))
            val endDate = dateParser.format(sdf.parse(event.endDate))
            if (startDate == endDate) {
                eventDate.text = SpannableStringBuilder()
                    .append("${sdf.parse(event.startDate).date}\n")
                    .bold {
                        append("${getMonthCode(sdf.parse(event.startDate).month)}")
                    }
            } else {
                eventDate.text = SpannableStringBuilder()
                    .append("${sdf.parse(event.startDate).date} - ${sdf.parse(event.endDate).date}\n")
                    .bold {
                        append("${getMonthCode(sdf.parse(event.startDate).month)}")
                    }
            }

            val startHour = timeParser.format(sdf.parse(event.startDate))
            val endHour = timeParser.format(sdf.parse(event.endDate))
            eventTime.text = "$startHour - $endHour"
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