// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

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
			val pos = eatRecyclerView.getChildAdapterPosition(it.parent as View)
			getMenuUrlByPos(pos)?.let { menuUrl ->
				val action = EatFragmentDirections.actionNavigationEatToWebViewFragment()
				action.title = resources.getString(R.string.title_menu_format, getRestaurantNameByPos(pos))
				action.url = menuUrl
				findNavController().navigate(action)
			}
		}

	}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

		val rest1 = Restaurant(
			resources.getString(R.string.community_bar_a1_name),
			listOf(
				R.drawable.community_bar_01,
				R.drawable.community_bar_02,
				R.drawable.community_bar_03,
				R.drawable.community_bar_04,
				R.drawable.community_bar_05,
			),
			resources.getString(R.string.community_bar_a1_openings),
			resources.getString(R.string.url_community_bar_a1_menu)
		)
		val rest2 = Restaurant(
			resources.getString(R.string.community_bar_d2_name),
			listOf(
				R.drawable.community_bar_05,
				R.drawable.community_bar_04,
				R.drawable.community_bar_03,
				R.drawable.community_bar_02,
			),
			resources.getString(R.string.community_bar_d2_openings)
		)
        val rest3 = Restaurant(
            resources.getString(R.string.noisteria_name),
            listOf(
                R.drawable.noisteria_aussen,
                R.drawable.noisteria_bar,
                R.drawable.noisteria_innen,
				R.drawable.noisteria_innen2,
				R.drawable.noisteria_salad
            ),
            resources.getString(R.string.noisteria_openings),
            resources.getString(R.string.url_noisteria_menu)
        )
        val rest4 = Restaurant(
			resources.getString(R.string.alumix_name),
            listOf(
				R.drawable.alumix,
				R.drawable.alumix_frittura,
				R.drawable.alumix_pizza,
				R.drawable.alumix_sala_garden),
            resources.getString(R.string.alumix_openings),
			resources.getString(R.string.url_alumix_menu)
        )

        restaurants = listOf(rest1, rest2, rest3, rest4)
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

	private fun getMenuUrlByPos(pos: Int): String? {
		return restaurants[pos].menuUrl
	}

	private fun getRestaurantNameByPos(pos: Int): String {
		return restaurants[pos].name
	}

}
