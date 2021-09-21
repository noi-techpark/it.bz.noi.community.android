package it.bz.noi.community.ui.today

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
                    setTextColor(view.resources.getColor(R.color.label_on_background_color))
                    text = timeFilter.filterName
                }
                filterSelected.setBackgroundColor(view.resources.getColor(R.color.fill_color))
            } else {
                filterName.apply {
                    text = timeFilter.filterName
                    setTextColor(view.resources.getColor(R.color.label_disabled_color))
                }
                filterSelected.setBackgroundColor(view.resources.getColor(R.color.unselected_time_filter))
            }
        }
    }
}
