package it.bz.noi.community.ui.today

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import it.bz.noi.community.R

class FiltersAdapter(private val items: List<Item>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val HEADER = 0
        private const val FILTER = 1
    }

    enum class FilterType {
        EVENT_TYPE,
        TECHNOLOGY_SECTOR
    }

    sealed class Item {
        data class Header(val text: String): Item()
        data class Filter(val filter: String, var checked: Boolean, val type: FilterType) : Item()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            HEADER -> HeaderViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.vh_header, parent, false))
            FILTER -> FilterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.vh_switch, parent, false))
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
                    holder.bind(it.filter, it.checked)
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

class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val headerTV: TextView = itemView.findViewById(R.id.headerTextView)

    fun bind(header: String) {
        headerTV.text = header
    }

}

class FilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val switchVH: SwitchMaterial = itemView.findViewById(R.id.switchVH)

    fun bind(filter: String, checked: Boolean) {
        switchVH.text = filter
        switchVH.isChecked = checked
    }

}

