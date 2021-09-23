package it.bz.noi.community.ui.eat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import it.bz.noi.community.R

/**
 * Eat tab fragment. Contains the available restaurants
 */
class EatFragment : Fragment() {

    private lateinit var restaurants: List<Restaurant>
    private lateinit var eatAdapter: EatAdapter
    private lateinit var eatRecyclerView: RecyclerView

    private val onMenuClickListener = View.OnClickListener {

		it?.let {
			val pos = eatRecyclerView.getChildLayoutPosition(it.parent?.parent as View)

			val action = EatFragmentDirections.actionNavigationEatToWebViewFragment()
			action.title = resources.getString(R.string.title_menu, getRestaurantNameByPos(pos))
			action.url = getMenuUrlByPos(pos)
			findNavController().navigate(action)
		}

	}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val rest1 = Restaurant(
            "Noisteria",
            listOf(
                R.drawable.restaurant_placeholder,
                R.drawable.restaurant_placeholder,
                R.drawable.restaurant_placeholder
            ),
            "Mo - Sa",
            "19:00 - 20:00",
            resources.getString(R.string.url_noisteria_menu)
        )
        val rest2 = Restaurant(
            "NOI Community Bar",
            listOf(R.drawable.restaurant_placeholder, R.drawable.restaurant_placeholder),
            "Mo - Sa",
            "19:00 - 20:00",
			resources.getString(R.string.url_noi_bar_menu)
        )
        val rest3 = Restaurant(
            "Alumix",
            listOf(R.drawable.restaurant_placeholder),
            "Mo - Sa",
            "19:00 - 20:00",
			resources.getString(R.string.url_alumix_menu)
        )

        restaurants = listOf(rest1, rest2, rest3)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        eatAdapter = EatAdapter(restaurants, onMenuClickListener)
        val root = inflater.inflate(R.layout.fragment_eat, container, false)
        eatRecyclerView = root.findViewById(R.id.eatRecyclerView)
		eatRecyclerView.adapter = eatAdapter
        return root
    }

	private fun getMenuUrlByPos(pos: Int): String {
		return restaurants[pos].menuUrl
	}

	private fun getRestaurantNameByPos(pos: Int): String {
		return restaurants[pos].name
	}
}
