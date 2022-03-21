package it.bz.noi.community.ui.today

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.transition.TransitionInflater
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import it.bz.noi.community.R
import it.bz.noi.community.data.api.ApiHelper
import it.bz.noi.community.data.api.RetrofitBuilder
import it.bz.noi.community.data.models.EventsResponse
import it.bz.noi.community.data.models.TimeFilter
import it.bz.noi.community.data.models.TimeRange
import it.bz.noi.community.data.repository.JsonFilterRepository
import it.bz.noi.community.databinding.FragmentNewsBinding
import it.bz.noi.community.databinding.FragmentTodayBinding
import it.bz.noi.community.ui.MainViewModel
import it.bz.noi.community.ui.ViewModelFactory
import it.bz.noi.community.utils.Status

class TodayFragment : Fragment() {

	private var _binding: FragmentTodayBinding? = null
	private val binding get() = _binding!!

	private val fragments = arrayListOf<Fragment>()
	private lateinit var todayTabsAdapter: TodayTabsAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		fragments.add(NewsFragment())
		fragments.add(EventsFragment())

		todayTabsAdapter = TodayTabsAdapter(
			childFragmentManager,
			lifecycle,
			fragments
		)
	}

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
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		createTabs()
	}

	private fun createTabs() {
		val tabLayout: TabLayout = binding.tabs
		val viewPager: ViewPager2 = binding.todayViewPager
		viewPager.apply {
			adapter = todayTabsAdapter
			offscreenPageLimit = 1
		}

		val tabsNames = listOf(
			"News", "Events" // FIXME export resources
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
	private val fragments: List<Fragment>
) : FragmentStateAdapter(fm, lifecycle) {

	override fun getItemCount() = fragments.size

	override fun createFragment(position: Int): Fragment {
		return fragments[position]
	}
}
