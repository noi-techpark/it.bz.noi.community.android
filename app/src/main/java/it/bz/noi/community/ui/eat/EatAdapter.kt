package it.bz.noi.community.ui.eat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.R

data class Restaurant(val name: String, val pictureIds: List<Int>, val openingDays: String, val openingTime: String, val menuUrl: String)

class EatAdapter(private val items: List<Restaurant>) : RecyclerView.Adapter<EatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.vh_eat, parent, false)
        return EatViewHolder(view)
    }

    override fun onBindViewHolder(holder: EatViewHolder, position: Int) {
        val restaurant = items[position]
        holder.bind(restaurant.name, restaurant.pictureIds, restaurant.openingDays, restaurant.openingTime, restaurant.menuUrl)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}

class EatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val nameTV: TextView = itemView.findViewById(R.id.tvRestName)
    private val openingDaysTV: TextView = itemView.findViewById(R.id.tvRestOpeningDays)
    private val openingTimeTV: TextView = itemView.findViewById(R.id.tvRestOpeningTime)
    private val picturesRV: RecyclerView = itemView.findViewById(R.id.picturesRecyclerView)

    fun bind(name: String, pictureIds: List<Int>, openingDays: String, openingTime: String, menuUrl: String) {
        nameTV.text = name
        openingDaysTV.text = openingDays
        openingTimeTV.text = openingTime

        picturesRV.layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
        picturesRV.adapter = PictureAdapter(pictureIds)
    }

}

class PictureAdapter(private val pictures: List<Int>) : RecyclerView.Adapter<PictureViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.vh_image, parent, false)
        return PictureViewHolder(view)
    }

    override fun onBindViewHolder(holder: PictureViewHolder, position: Int) {
        holder.bind(pictures[position])
    }

    override fun getItemCount(): Int {
        return pictures.size
    }
}

class PictureViewHolder(pictureItemView: View) : RecyclerView.ViewHolder(pictureItemView) {

    private val restImageView: ImageView = itemView.findViewById(R.id.restImage)

    fun bind(pictureId: Int) {
        restImageView.setImageResource(pictureId)
    }

}


