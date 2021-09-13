package it.bz.noi.community.ui.eat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.R

class EatFragment: Fragment() {

    private lateinit var restaurants: List<Restaurant>
    private lateinit var eatAdapter: EatAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rest1 = Restaurant("Noisteria", listOf(R.drawable.restaurant_placeholder, R.drawable.restaurant_placeholder, R.drawable.restaurant_placeholder), "Mo - Sa", "19:00 - 20:00", "")
        val rest2 = Restaurant("NOI Community Bar", listOf(R.drawable.restaurant_placeholder, R.drawable.restaurant_placeholder), "Mo - Sa", "19:00 - 20:00", "")
        val rest3 = Restaurant("Alumix", listOf(R.drawable.restaurant_placeholder), "Mo - Sa", "19:00 - 20:00", "")

        restaurants = listOf(rest1, rest2, rest3)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        eatAdapter = EatAdapter(restaurants)
        val root = inflater.inflate(R.layout.fragment_eat, container, false)
        root.findViewById<RecyclerView>(R.id.eatRecyclerView).adapter = eatAdapter
        return root
    }
}