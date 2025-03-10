package it.bz.noi.community.ui.today.news

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.data.models.FilterValue
import it.bz.noi.community.databinding.VhSwitchBinding
import it.bz.noi.community.ui.FilterViewHolder
import it.bz.noi.community.ui.UpdateResultsListener

class NewsFilterAdapter(private val updateResultsListener: UpdateResultsListener) :
	RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	sealed class Item {
		data class Filter(val filter: FilterValue): Item()
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
		filterItems.addAll(filters.map {
			Item.Filter(it)
		})
		return filterItems
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
		return FilterViewHolder(VhSwitchBinding.inflate(LayoutInflater.from(parent.context), parent, false), updateResultsListener,exclusive = false)
	}

	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		when (holder) {
			is FilterViewHolder -> {
				val filterItem = getItem(position) as Item.Filter
				holder.bind(filterItem.filter)
			}
			else -> throw RuntimeException("Unsupported holder $holder")
		}
	}

	override fun getItemCount(): Int {
		return items.size
	}

	private fun getItem(position: Int): Item {
		return items[position]
	}
}
