// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui

import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.data.models.FilterValue
import it.bz.noi.community.databinding.VhHeaderBinding
import it.bz.noi.community.databinding.VhSwitchBinding

class HeaderViewHolder(private val binding: VhHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

	fun bind(header: String) {
		binding.headerTextView.text = header
	}
}

class FilterViewHolder(private val binding: VhSwitchBinding, updateResultsListener: UpdateResultsListener) : RecyclerView.ViewHolder(binding.root) {

	private lateinit var filter: FilterValue

	init {
		binding.switchVH.setOnClickListener {
			filter.checked = binding.switchVH.isChecked
			updateResultsListener.updateResults(filter)
		}
	}

	fun bind(f: FilterValue) {
		filter = f
		binding.switchVH.text = filter.desc
		binding.switchVH.isChecked = filter.checked
	}

}

interface UpdateResultsListener {
	fun updateResults(filter: FilterValue)
}
