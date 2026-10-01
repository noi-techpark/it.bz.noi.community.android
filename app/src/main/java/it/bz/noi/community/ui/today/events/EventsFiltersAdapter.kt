// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.today.events

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.data.models.EventsFilterType
import it.bz.noi.community.data.models.FilterValue
import it.bz.noi.community.databinding.VhHeaderBinding
import it.bz.noi.community.databinding.VhSwitchBinding
import it.bz.noi.community.ui.FilterViewHolder
import it.bz.noi.community.ui.HeaderViewHolder
import it.bz.noi.community.ui.UpdateResultsListener

class EventsFiltersAdapter(private val eventTypeHeader: String,
						   private val technlogySectorHeader: String,
						   private val updateResultsListener: UpdateResultsListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val HEADER = 0
        private const val EVENT_TYPE_FILTER = 1
		private const val TECHNOLOGY_SECTOR_FILTER = 2
    }

    sealed class Item {
        data class Header(val text: String): Item()

		sealed class Filter {
			data class EventType(val filter: FilterValue) : Item()
			data class TechnologySector(val filter: FilterValue) : Item()
		}
    }

	var filters: List<FilterValue> = emptyList()
		set(value) {
			if (value != field) {
				field = value
				items = toItems()
				notifyDataSetChanged()
			}
		}
	private var items: List<Item> = toItems()

	private fun toItems(): List<Item> {

		val filterItems = arrayListOf<Item>()

		val eventTypeFilters = filters.filter { it.type == EventsFilterType.EVENT_TYPE.category }
		val technologySectorFilters = filters.filter { it.type == EventsFilterType.TECHNOLOGY_SECTOR.category }

		if (eventTypeFilters.isNotEmpty()) {
			filterItems.add(Item.Header(eventTypeHeader))
			filterItems.addAll(eventTypeFilters.map {
				Item.Filter.EventType(it)
			})
		}

		if (technologySectorFilters.isNotEmpty()) {
			filterItems.add(Item.Header(technlogySectorHeader))
			filterItems.addAll(technologySectorFilters.map {
				Item.Filter.TechnologySector(it)
			})
		}

		return filterItems
	}

	/**
	 * EVENT_TYPE filters are mutually exclusive: checking one must uncheck every
	 * other EVENT_TYPE filter in [filters] itself, not just sibling views -
	 * with up to ~24 event-type tags in the new /v1/Tag catalog (was a fixed 3
	 * before this migration), most siblings won't even be laid out on screen at
	 * the same time, so a view-position-based approach can't see them.
	 */
	private val eventTypeUpdateResultsListener = object : UpdateResultsListener {
		override fun updateResults(filter: FilterValue) {
			if (filter.type == EventsFilterType.EVENT_TYPE.category && filter.checked) {
				filters.forEach {
					if (it.type == EventsFilterType.EVENT_TYPE.category && it.key != filter.key)
						it.checked = false
				}
				notifyDataSetChanged()
			}
			updateResultsListener.updateResults(filter)
		}
	}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER -> HeaderViewHolder(VhHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            EVENT_TYPE_FILTER -> FilterViewHolder(VhSwitchBinding.inflate(LayoutInflater.from(parent.context), parent, false), eventTypeUpdateResultsListener)
			TECHNOLOGY_SECTOR_FILTER -> FilterViewHolder(VhSwitchBinding.inflate(LayoutInflater.from(parent.context), parent, false), updateResultsListener)
            else -> throw RuntimeException("Unsupported viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                (getItem(position) as Item.Header).let {
                    holder.bind(it.text)
                }
            }
            is FilterViewHolder -> {
				when (val filterItem = getItem(position)) {
					is Item.Filter.EventType -> holder.bind(filterItem.filter)
					is Item.Filter.TechnologySector -> holder.bind(filterItem.filter)
					else -> throw RuntimeException("Invalid filter item $filterItem")
				}
            }
            else -> throw RuntimeException("Unsupported holder $holder")
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)) {
            is Item.Header -> HEADER
            is Item.Filter.EventType -> EVENT_TYPE_FILTER
			is Item.Filter.TechnologySector -> TECHNOLOGY_SECTOR_FILTER
        }
    }

    private fun getItem(position: Int): Item {
        return items[position]
    }

}
