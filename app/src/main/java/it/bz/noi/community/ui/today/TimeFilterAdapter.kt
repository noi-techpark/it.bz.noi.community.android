package it.bz.noi.community.ui.today

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.R
import it.bz.noi.community.data.models.TimeFilter

interface TimeFilterClickListener {
    fun onTimeFilterClick(position: Int)
}

class TimeFilterAdapter(
    private val timeFilters: List<TimeFilter>,
    private val listener: TimeFilterClickListener
) :
    RecyclerView.Adapter<TimeFilterAdapter.TimeFilterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeFilterViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_holder_time_filter, parent, false)
        return TimeFilterViewHolder(view)
    }

    override fun onBindViewHolder(holder: TimeFilterViewHolder, position: Int) {
        holder.bind(timeFilters[position])
    }

    override fun getItemCount() = timeFilters.size

    inner class TimeFilterViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

        private val filterName = view.findViewById<TextView>(R.id.tvTimeFilter)
        private val filterSelected = view.findViewById<View>(R.id.viewFilterSelected)

        init {
            view.rootView.setOnClickListener {
                listener.onTimeFilterClick(adapterPosition)
            }
        }

        fun bind(timeFilter: TimeFilter) {
            if (timeFilter.filterSelected) {
                filterName.apply {
					setTextColor(ContextCompat.getColor(context, R.color.secondary_color))
                    text = timeFilter.filterName
                }
                filterSelected.apply {
					setBackgroundColor(ContextCompat.getColor(context, R.color.background_color))
                }
            } else {
                filterName.apply {
                    text = timeFilter.filterName
                    setTextColor(ContextCompat.getColor(context, R.color.disabled1_color))
                }
                filterSelected.apply {
					// disabled1_color with alpha 85%
					setBackgroundColor(ColorUtils.setAlphaComponent(ContextCompat.getColor(context, R.color.disabled1_color), 217))
				}
            }
        }
    }
}
