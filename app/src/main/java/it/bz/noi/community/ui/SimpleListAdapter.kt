package it.bz.noi.community.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.R

class SimpleListAdapter(private val items: List<String>, private val onClickListener: View.OnClickListener? = null) : RecyclerView.Adapter<ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.vh_link_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position], onClickListener)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val itemTextView: TextView = itemView.findViewById(R.id.itemTextView)
    private lateinit var linkImageView: ImageView

    fun bind(text: String, onClickListener: View.OnClickListener?) {
        itemTextView.text = text

        if (onClickListener != null) {
            linkImageView = itemView.findViewById(R.id.linkArrow)
            linkImageView.setOnClickListener(onClickListener)
        }
    }

}