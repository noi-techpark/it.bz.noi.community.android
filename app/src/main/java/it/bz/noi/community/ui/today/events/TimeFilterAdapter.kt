// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.today.events

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.data.models.TimeFilter
import it.bz.noi.community.databinding.VhTimeFilterBinding

interface TimeFilterClickListener {
    fun onTimeFilterClick(position: Int)
}

class TimeFilterAdapter(
	timeFilters: List<TimeFilter>,
	private val listener: TimeFilterClickListener
) :
    RecyclerView.Adapter<TimeFilterAdapter.TimeFilterViewHolder>() {

	var timeFilters: List<TimeFilter> = timeFilters
		@SuppressLint("NotifyDataSetChanged")
		set(value) {
			field = value
			notifyDataSetChanged()
		}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeFilterViewHolder {
        return TimeFilterViewHolder(VhTimeFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: TimeFilterViewHolder, position: Int) {
        holder.bind(timeFilters[position])
    }

    override fun getItemCount() = timeFilters.size

    inner class TimeFilterViewHolder(private val binding: VhTimeFilterBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.timeFilter.setOnClickListener {
                listener.onTimeFilterClick(adapterPosition)
            }
        }

        fun bind(timeFilter: TimeFilter) {
			binding.timeFilter.text = timeFilter.filterName
			binding.timeFilter.isChecked = timeFilter.filterSelected
        }
    }
}
