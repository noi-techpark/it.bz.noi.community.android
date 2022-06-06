package it.bz.noi.community.ui.today.events

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import it.bz.noi.community.R
import it.bz.noi.community.data.models.FilterType
import it.bz.noi.community.data.models.FilterValue
import it.bz.noi.community.databinding.VhHeaderBinding
import it.bz.noi.community.databinding.VhSwitchBinding

class FiltersAdapter(private val eventTypeHeader: String,
					 private val technlogySectorHeader: String,
					 private val updateResultsListener: UpdateResultsListener) :
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

		val eventTypeFilters = filters.filter { it.type == FilterType.EVENT_TYPE.typeDesc }
		val technologySectorFilters = filters.filter { it.type == FilterType.TECHNOLOGY_SECTOR.typeDesc }

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER -> HeaderViewHolder(VhHeaderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            EVENT_TYPE_FILTER -> FilterViewHolder(VhSwitchBinding.inflate(LayoutInflater.from(parent.context), parent, false), updateResultsListener, exclusive = true)
			TECHNOLOGY_SECTOR_FILTER -> FilterViewHolder(VhSwitchBinding.inflate(LayoutInflater.from(parent.context), parent, false), updateResultsListener,exclusive = false)
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
            is Item.Filter.EventType -> EVENT_TYPE_FILTER
			is Item.Filter.TechnologySector -> TECHNOLOGY_SECTOR_FILTER
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

class FilterViewHolder(private val binding: VhSwitchBinding, updateResultsListener: UpdateResultsListener, exclusive: Boolean = false) : RecyclerView.ViewHolder(binding.root) {

	private lateinit var filter: FilterValue

    init {
        binding.switchVH.setOnClickListener {
        	filter.checked = binding.switchVH.isChecked

			if (exclusive && filter.checked)
				turnOffOtherSwitch()

			updateResultsListener.updateResults()
		}
    }

	private fun turnOffOtherSwitch() {
		val parent = binding.root.parent
		if (parent != null && parent is RecyclerView) {
			parent.apply {
				for (i in 1 until 3) {
					val childView = getChildAt(i)
					childView.findViewById<SwitchMaterial>(R.id.switchVH)?.let { switch ->
						if (!switch.text.equals(filter.desc)) {
							switch.isChecked = false
							switch.callOnClick()
						}
					}

				}
			}
		}
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

