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

class MeetFiltersAdapter(private val headers: Map<AccountType, String>,
						 private val updateResultsListener: UpdateResultsListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val HEADER = 0
		private const val COMPANY_FILTER = 1
		private const val STARTUP_FILTER = 2
		private const val RESEARCH_INSTITUTION_FILTER = 3
		private const val EMPTY = 4
    }

    sealed class Item {
        data class Header(val text: String): Item()

		sealed class Filter(val filter: FilterValue) : Item() {
			data class Company(val f: FilterValue) : Filter(f)
			data class StartUp(val f: FilterValue) : Filter(f)
			data class ResearchInstitutions(val f: FilterValue) : Filter(f)
		}

		object Empty: Item()
    }

	var filters: Map<AccountType, List<FilterValue>> = emptyMap()
		set(value) {
			if (value != field) {
				field = value
				items = toItems()
				notifyDataSetChanged()
			}
		}
	private var items: List<Item> = emptyList()

	private fun toItems(): List<Item> {

		val filterItems = arrayListOf<Item>()

		val companyFilters = filters[AccountType.COMPANY]
		val startupFilters = filters[AccountType.STARTUP]
		val researchInstitutionsFilters = filters[AccountType.RESEARCH_INSTITUTION]

		if (companyFilters?.isNotEmpty() == true) {
			filterItems.add(Item.Header(headers.getOrDefault(AccountType.COMPANY, "")))
			filterItems.addAll(companyFilters.map {
				Item.Filter.Company(it)
			})
		}

		if (startupFilters?.isNotEmpty() == true) {
			filterItems.add(Item.Header(headers.getOrDefault(AccountType.STARTUP, "")))
			filterItems.addAll(startupFilters.map {
				Item.Filter.StartUp(it)
			})
		}

		if (researchInstitutionsFilters?.isNotEmpty() == true) {
			filterItems.add(Item.Header(headers.getOrDefault(AccountType.RESEARCH_INSTITUTION, "")))
			filterItems.addAll(researchInstitutionsFilters.map {
				Item.Filter.ResearchInstitutions(it)
			})
		}

		if (filterItems.isEmpty()) {
			filterItems.add(Item.Empty)
		}

		return filterItems
	}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER -> HeaderViewHolder(VhHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            COMPANY_FILTER -> FilterViewHolder(VhSwitchBinding.inflate(LayoutInflater.from(parent.context), parent, false), updateResultsListener, exclusive = false)
			STARTUP_FILTER -> FilterViewHolder(VhSwitchBinding.inflate(LayoutInflater.from(parent.context), parent, false), updateResultsListener, exclusive = false)
			RESEARCH_INSTITUTION_FILTER -> FilterViewHolder(VhSwitchBinding.inflate(LayoutInflater.from(parent.context), parent, false), updateResultsListener, exclusive = false)
            EMPTY -> EmptyViewHolder(
				VhEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false).apply {
					subtitle.text = parent.resources.getString(R.string.label_filters_empty_state_subtitle)
				}
			)
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
				(getItem(position) as Item.Filter).let {
					holder.bind(it.filter)
				}
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is Item.Header -> HEADER
            is Item.Filter.Company -> COMPANY_FILTER
			is Item.Filter.StartUp -> STARTUP_FILTER
			is Item.Filter.ResearchInstitutions -> RESEARCH_INSTITUTION_FILTER
			Item.Empty -> EMPTY
        }
    }

    private fun getItem(position: Int): Item {
        return items.get(position)
    }

}



