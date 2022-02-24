package it.bz.noi.community.ui.today

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.data.models.FilterType
import it.bz.noi.community.data.models.FilterValue
import it.bz.noi.community.databinding.VhHeaderBinding
import it.bz.noi.community.databinding.VhSwitchBinding

class FiltersAdapter(private val filters: List<FilterValue>,
					 private val eventTypeHeader: String,
					 private val technlogySectorHeader: String,
					 private val updateResultsListener: UpdateResultsListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val HEADER = 0
        private const val FILTER = 1
    }

    sealed class Item {
        data class Header(val text: String): Item()
        data class Filter(val filter: FilterValue) : Item()
    }

	private val items: List<Item> = toItems()

	private fun toItems(): List<Item> {

		val filterItems = arrayListOf<Item>()

		val eventTypeFilters = filters.filter { it.type == FilterType.EVENT_TYPE.typeDesc }
		val technlogySectorFilters = filters.filter { it.type == FilterType.TECHNOLOGY_SECTOR.typeDesc }

		if (eventTypeFilters.isNotEmpty()) {
			filterItems.add(Item.Header(eventTypeHeader))
			filterItems.addAll(eventTypeFilters.map {
				Item.Filter(it)
			})
		}

		if (technlogySectorFilters.isNotEmpty()) {
			filterItems.add(Item.Header(technlogySectorHeader))
			filterItems.addAll(technlogySectorFilters.map {
				Item.Filter(it)
			})
		}

		return filterItems
	}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER -> HeaderViewHolder(VhHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            FILTER -> FilterViewHolder(VhSwitchBinding.inflate(LayoutInflater.from(parent.context), parent, false), updateResultsListener)
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
            else -> throw RuntimeException("Unsupported holder $holder")
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)){
            is Item.Header -> HEADER
            is Item.Filter -> FILTER
        }
    }

    private fun getItem(position: Int): Item {
        return items.get(position)
    }

}

class HeaderViewHolder(private val binding: VhHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(header: String) {
        binding.headerTextView.text = header
    }

}

class FilterViewHolder(private val binding: VhSwitchBinding, updateResultsListener: UpdateResultsListener) : RecyclerView.ViewHolder(binding.root) {

	private lateinit var filter: FilterValue

    init {
        binding.switchVH.setOnClickListener(View.OnClickListener{
        	filter.checked = binding.switchVH.isChecked
			updateResultsListener.updateResults()
		})
    }

    fun bind(f: FilterValue) {
		filter = f
        binding.switchVH.text = filter.desc
		binding.switchVH.isChecked = filter.checked
    }

}

interface UpdateResultsListener {
	fun updateResults()
}

