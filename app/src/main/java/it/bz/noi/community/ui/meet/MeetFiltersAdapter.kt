// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.meet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.R
import it.bz.noi.community.data.models.AccountType
import it.bz.noi.community.data.models.FilterValue
import it.bz.noi.community.databinding.VhEmptyBinding
import it.bz.noi.community.databinding.VhHeaderBinding
import it.bz.noi.community.databinding.VhSwitchBinding
import it.bz.noi.community.ui.FilterViewHolder
import it.bz.noi.community.ui.HeaderViewHolder
import it.bz.noi.community.ui.UpdateResultsListener

private const val SHOW_HEADERS = false
private const val VIEWTYPE_HEADER = 0
private const val VIEWTYPE_FILTER = 1
private const val VIEWTYPE_EMPTY = 4

class MeetFiltersAdapter(
	private val headers: Map<AccountType, String>,
	private val updateResultsListener: UpdateResultsListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	private sealed class Item {
		data class Header(val title: String) : Item()
		data class Filter(val accountType: AccountType, val filter: FilterValue) : Item()
		data object Empty : Item()
	}

	var filters: Map<AccountType, List<FilterValue>> = emptyMap()
		set(value) {
			if (value != field) {
				field = value
				items = value.toItems(SHOW_HEADERS) { type ->
					headers.getOrDefault(type, "")
				}
				notifyDataSetChanged()
			}
		}
	private var items: List<Item> = emptyList()

	private fun Map<AccountType, List<FilterValue>>.toItems(showHeaders: Boolean, getHeaderTitle: (AccountType) -> String): List<Item> {

		val filterItems = mutableListOf<Item>()

		if (showHeaders) {
			// Segmented list with headers, with each section sorted by desc.
			forEach { (type, filters) ->
				if (filters.isNotEmpty()) {
					filterItems += Item.Header(getHeaderTitle(type))
					filterItems.addAll(filters.sortedBy { it.desc }. map {
						Item.Filter(type, it)
					})
				}
			}
		} else {
			// Flat list without headers, sorted by desc.
			val sortedFilterItems = buildList {
				this@toItems.forEach { (type, filters) ->
					filters.forEach { filter ->
						add(Item.Filter(type, filter))
					}
				}
			}.sortedBy { it.filter.desc }
			filterItems.addAll(sortedFilterItems)
		}

		if (filterItems.isEmpty()) {
			filterItems.add(Item.Empty)
		}

		return filterItems
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return when (viewType) {
			VIEWTYPE_HEADER -> HeaderViewHolder(
				VhHeaderBinding.inflate(
					LayoutInflater.from(parent.context),
					parent,
					false
				)
			)

			VIEWTYPE_FILTER -> FilterViewHolder(
				VhSwitchBinding.inflate(
					LayoutInflater.from(parent.context),
					parent,
					false
				), updateResultsListener, exclusive = false
			)

			VIEWTYPE_EMPTY -> EmptyViewHolder(
				VhEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
					subtitle.text =
						parent.resources.getString(R.string.label_filters_empty_state_subtitle)
				}
			)

			else -> throw RuntimeException("Unsupported viewType $viewType")
		}
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		when (holder) {
			is HeaderViewHolder -> {
				(getItem(position) as Item.Header).let {
					holder.bind(it.title)
				}
			}

			is FilterViewHolder -> {
				(getItem(position) as Item.Filter).let {
					holder.bind(it.filter)
				}
			}
		}
	}

	override fun getItemCount(): Int = items.size

	override fun getItemViewType(position: Int) = when (getItem(position)) {
			is Item.Header -> VIEWTYPE_HEADER
			is Item.Filter -> VIEWTYPE_FILTER
			Item.Empty -> VIEWTYPE_EMPTY
	}

	private fun getItem(position: Int): Item = items[position]
}
