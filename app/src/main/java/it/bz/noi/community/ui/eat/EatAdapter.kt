package it.bz.noi.community.ui.eat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import it.bz.noi.community.R

/**
 * Class that represents a restaurant in the NOI community application
 */
data class Restaurant(
    val name: String,
    val pictureIds: List<Int>,
    val openingTime: String,
    val menuUrl: String
)

/**
 * Adapter used to populate the list of restaurants in the Eat tab
 */
class EatAdapter(private val items: List<Restaurant>, private val onMenuClickListener: View.OnClickListener) : RecyclerView.Adapter<EatAdapter.EatViewHolder>() {

	/**
	 * View holder of the Restaurant
	 */
	inner class EatViewHolder(itemView: View, onMenuClickListener: View.OnClickListener) : RecyclerView.ViewHolder(itemView) {

		private val nameTV: TextView = itemView.findViewById(R.id.tvRestName)
		private val openingTimeTV: TextView = itemView.findViewById(R.id.tvRestOpeningTime)
		private val picturesRV: RecyclerView = itemView.findViewById(R.id.picturesRecyclerView)
		private val menuBtn: MaterialButton = itemView.findViewById(R.id.menuBtn)

		init {
			menuBtn.setOnClickListener(onMenuClickListener)
		}

		fun bind(
			name: String,
			pictureIds: List<Int>,
			openingTime: String
		) {
			nameTV.text = name
			openingTimeTV.text = openingTime

			picturesRV.layoutManager =
				LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
			picturesRV.adapter = PictureAdapter(pictureIds)
		}

	}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.vh_eat, parent, false)
        return EatViewHolder(view, onMenuClickListener)
    }

    override fun onBindViewHolder(holder: EatViewHolder, position: Int) {
        val restaurant = items[position]
        holder.bind(
            restaurant.name,
            restaurant.pictureIds,
            restaurant.openingTime
        )
    }

    override fun getItemCount(): Int {
        return items.size
    }
}

/**
 * Adapter used to populate the list of pictures of each restaurant
 */
class PictureAdapter(private val pictures: List<Int>) : RecyclerView.Adapter<PictureAdapter.PictureViewHolder>() {

	/**
	 * View holder of a single picture of the restaurant
	 */
	inner class PictureViewHolder(pictureItemView: View) : RecyclerView.ViewHolder(pictureItemView) {

		private val restImageView: ImageView = itemView.findViewById(R.id.restImage)

		fun bind(pictureId: Int) {
			restImageView.setImageResource(pictureId)
		}
	}

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


