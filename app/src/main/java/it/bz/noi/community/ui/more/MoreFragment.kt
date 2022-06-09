package it.bz.noi.community.ui.more

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import it.bz.noi.community.R
import it.bz.noi.community.databinding.FragmentMoreBinding
import it.bz.noi.community.ui.SimpleListAdapter
import it.bz.noi.community.ui.WebViewFragmentDirections

class MoreFragment : Fragment() {

	private lateinit var binding: FragmentMoreBinding

	private val openLinkClickListener = View.OnClickListener {
		it?.let {
			val pos = binding.moreRecyclerView.getChildAdapterPosition(it)

			if (pos == PROFILE_ITEM) {
				findNavController().navigate(MoreFragmentDirections.actionMoreToProfile())
			} else {
				val action = WebViewFragmentDirections.actionGlobalWebViewFragment().apply {
					title = items[pos]
					url = getUrlByItemPosition(pos)
				}
				findNavController().navigate(action)
			}
		}
	}

	private lateinit var items: List<String>
	private lateinit var moreInfoAdapter: SimpleListAdapter
	private val appVersionAdapter = AppVersionAdapter()
	private val moreAdapter: ConcatAdapter by lazy {
		ConcatAdapter(moreInfoAdapter, appVersionAdapter)
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		items = listOf(
			resources.getString(R.string.room_booking),
			resources.getString(R.string.more_item_onboarding),
			resources.getString(R.string.more_item_feedback),
			resources.getString(R.string.more_item_account)
		)
	}

	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View {
		binding = FragmentMoreBinding.inflate(inflater)
		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		moreInfoAdapter = SimpleListAdapter(items, openLinkClickListener)
		binding.moreRecyclerView.adapter = moreAdapter
	}

	private fun getUrlByItemPosition(pos: Int): String {
		return when (pos) {
			0 -> resources.getString(R.string.url_room_booking)
			1 -> resources.getString(R.string.url_onboarding)
			2 -> resources.getString(R.string.url_provide_feedback)
			else -> throw Exception("Link not found")
		}
	}

	companion object {
		private const val PROFILE_ITEM = 3
	}

}
