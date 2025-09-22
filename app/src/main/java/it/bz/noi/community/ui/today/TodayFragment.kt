// SPDX-FileCopyrightText: NOI Techpark <digital@noi.bz.it>
//
// SPDX-License-Identifier: AGPL-3.0-or-later

package it.bz.noi.community.ui.today

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import it.bz.noi.community.R
import it.bz.noi.community.databinding.FragmentTodayBinding
import it.bz.noi.community.ui.today.events.EventsFragment
import it.bz.noi.community.ui.today.news.NewsFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi

class TodayFragment : Fragment() {

	private var _binding: FragmentTodayBinding? = null
	private val binding get() = _binding!!

	private lateinit var tabLayout: TabLayout

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		_binding = FragmentTodayBinding.inflate(inflater)
		createTabs()
		return binding.root
	}

	@OptIn(ExperimentalCoroutinesApi::class)
	private fun createTabs() {
		tabLayout = binding.tabs

		val viewPager: ViewPager2 = binding.todayViewPager
		viewPager.apply {
			adapter = TodayTabsAdapter(
				childFragmentManager,
				lifecycle,
				listOf(
					TodayTabsAdapter.FragmentProvider {
						NewsFragment()
					},
					TodayTabsAdapter.FragmentProvider {
						EventsFragment()
					}
				)
			)
			isUserInputEnabled = false
		}

		val tabsNames = listOf(
			getString(R.string.news_top_tab),
			getString(R.string.events_top_tab)
		)

		TabLayoutMediator(tabLayout, viewPager) { tab, position ->
			tab.contentDescription = tabsNames[position]
			tab.text = tabsNames[position]
		}.attach()
	}
}

class TodayTabsAdapter(
	fm: FragmentManager,
	lifecycle: Lifecycle,
	private val fragments: List<FragmentProvider>
) : FragmentStateAdapter(fm, lifecycle) {

	fun interface FragmentProvider {
		fun getFragment(): Fragment
	}

	override fun getItemCount() = fragments.size

	override fun createFragment(position: Int): Fragment = fragments[position].getFragment()
}
